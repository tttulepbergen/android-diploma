package com.example.scanfit.mainNavigation.scan

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.scanfit.R
import com.example.scanfit.databinding.FragmentScanBinding
import com.example.scanfit.network.AnalysisResponse
import com.example.scanfit.network.NetworkClient
import com.example.scanfit.utils.SessionManager
import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class ScanFragment : Fragment() {

    private enum class ScanMode {
        FOOD,
        COMPOUND
    }

    private var _binding: FragmentScanBinding? = null
    private val binding get() = _binding!!

    private var imageCapture: ImageCapture? = null
    private lateinit var cameraExecutor: ExecutorService
    private lateinit var sessionManager: SessionManager
    private var isAnalyzing = false
    private val gson = GsonBuilder().serializeNulls().create()

    private val galleryLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            lifecycleScope.launch(Dispatchers.IO) {
                try {
                    val file = uriToFile(it)
                    val compressed = getCompressedFile(file)
                    withContext(Dispatchers.Main) {
                        promptForScanMode(compressed)
                        file.delete()
                    }
                } catch (e: Exception) {
                    Log.e("SCAN_DEBUG", "Gallery processing failed", e)
                    withContext(Dispatchers.Main) {
                        setLoadingState(false)
                        if (isAdded) {
                            Toast.makeText(requireContext(), "Failed to open image", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentScanBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        cameraExecutor = Executors.newSingleThreadExecutor()
        sessionManager = SessionManager(requireContext())

        if (allPermissionsGranted()) {
            startCamera()
        } else {
            @Suppress("DEPRECATION")
            requestPermissions(arrayOf(Manifest.permission.CAMERA), 10)
        }

        binding.captureButton.setOnClickListener { takePhoto() }
        binding.btnBrowse.setOnClickListener {
            if (!isAnalyzing) findNavController().navigate(R.id.action_nav_scan_to_categoriesFragment)
        }
        binding.btnSearch.setOnClickListener {
            if (!isAnalyzing) findNavController().navigate(R.id.action_nav_scan_to_searchFragment)
        }
        binding.btnUpload.setOnClickListener {
            if (!isAnalyzing) openGallery()
        }
    }

    private fun startCamera() {
        val context = context ?: return
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)

        cameraProviderFuture.addListener({
            val currentBinding = _binding ?: return@addListener
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(currentBinding.viewFinder.surfaceProvider)
            }

            imageCapture = ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                .build()

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    viewLifecycleOwner,
                    CameraSelector.DEFAULT_BACK_CAMERA,
                    preview,
                    imageCapture
                )
            } catch (exc: Exception) {
                Log.e("SCAN_DEBUG", "Use case binding failed", exc)
            }
        }, ContextCompat.getMainExecutor(context))
    }

    private fun takePhoto() {
        if (isAnalyzing) return

        val currentContext = context ?: return
        val useCase = imageCapture
        if (useCase == null) {
            Log.e("SCAN_DEBUG", "ImageCapture is null")
            Toast.makeText(currentContext, "Camera is not ready", Toast.LENGTH_SHORT).show()
            return
        }

        val photoFile = File(currentContext.cacheDir, "scan_${System.currentTimeMillis()}.jpg")
        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        useCase.takePicture(
            outputOptions,
            cameraExecutor,
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    lifecycleScope.launch(Dispatchers.Main) {
                        try {
                            val compressedFile = withContext(Dispatchers.IO) { getCompressedFile(photoFile) }
                            promptForScanMode(compressedFile)
                        } catch (e: Exception) {
                            setLoadingState(false)
                            Log.e("SCAN_DEBUG", "Post-save processing failed", e)
                            Toast.makeText(currentContext, "Failed to process photo", Toast.LENGTH_SHORT).show()
                        }
                    }
                }

                override fun onError(exception: ImageCaptureException) {
                    Log.e("SCAN_DEBUG", "Camera capture failed", exception)
                    viewLifecycleOwner.lifecycleScope.launch(Dispatchers.Main) {
                        setLoadingState(false)
                        Toast.makeText(currentContext, "Failed to take photo", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        )
    }

    private fun getCompressedFile(file: File): File {
        val bitmap = BitmapFactory.decodeFile(file.path)
        val compressedFile = File(requireContext().cacheDir, "compressed_${file.name}")
        val out = FileOutputStream(compressedFile)
        bitmap.compress(Bitmap.CompressFormat.JPEG, 60, out)
        out.flush()
        out.close()
        return compressedFile
    }

    private fun promptForScanMode(file: File) {
        if (!isAdded || _binding == null) {
            file.delete()
            return
        }

        AlertDialog.Builder(requireContext())
            .setTitle("Choose analysis type")
            .setMessage("How should we analyze this image?")
            .setPositiveButton("Analyze food") { _, _ ->
                analyzeImageWithAi(file, ScanMode.FOOD)
            }
            .setNegativeButton("Analyze compound") { _, _ ->
                analyzeImageWithAi(file, ScanMode.COMPOUND)
            }
            .setNeutralButton("Compare") { _, _ ->
                runCatching {
                    findNavController().navigate(
                        R.id.action_nav_scan_to_compareProductsFragment,
                        androidx.core.os.bundleOf("captured_image_path" to file.absolutePath)
                    )
                }
            }
            .setOnCancelListener {
                file.delete()
            }
            .show()
    }

    private fun analyzeImageWithAi(file: File, scanMode: ScanMode) {
        setLoadingState(true)
        binding.tvLoadingMessage.text = when (scanMode) {
            ScanMode.FOOD -> "Analyzing food..."
            ScanMode.COMPOUND -> "Analyzing compound..."
        }

        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            try {
                val token = sessionManager.fetchAuthToken()
                if (token.isNullOrBlank()) {
                    withContext(Dispatchers.Main) {
                        setLoadingState(false)
                        if (isAdded) {
                            Toast.makeText(requireContext(), "User token not found", Toast.LENGTH_SHORT).show()
                        }
                    }
                    return@launch
                }

                if (!hasAvailableProductScanLimit(token)) {
                    withContext(Dispatchers.Main) {
                        setLoadingState(false)
                    }
                    return@launch
                }

                val requestFile = file.asRequestBody("image/jpeg".toMediaTypeOrNull())
                val body = MultipartBody.Part.createFormData("file", file.name, requestFile)

                val healthInfoBody = "".toRequestBody("text/plain".toMediaTypeOrNull())
                val userContext = loadUserContextParts()
                logAiRequestPayload(
                    file = file,
                    healthInfo = "",
                    userContext = userContext
                )

                val response = when (scanMode) {
                    ScanMode.COMPOUND -> NetworkClient.aiApiService.analyzeScan(
                        file = body,
                        healthInfo = healthInfoBody,
                        userInformation = userContext.userInformation
                    )
                    ScanMode.FOOD -> NetworkClient.aiApiService.analyzeDish(
                        file = body,
                        healthInfo = healthInfoBody,
                        userInformation = userContext.userInformation
                    )
                }
                Log.d("SCAN_DEBUG", "AI response received: $response")

                withContext(Dispatchers.Main) {
                    if (isAdded && _binding != null) {
                        navigateToAnalysis(response)
                    }
                }
                decreaseProductScanLimit(token)
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    setLoadingState(false)
                    Log.e("SCAN_DEBUG", "AI analyze failed", e)
                    if (isAdded) {
                        Toast.makeText(requireContext(), "Network error: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            } finally {
                file.delete()
            }
        }
    }

    private suspend fun hasAvailableProductScanLimit(token: String): Boolean {
        val limitResponse = NetworkClient.userApiService.getProductScanLimit(token)
        if (limitResponse.success == false && limitResponse.data == null) {
            withContext(Dispatchers.Main) {
                if (isAdded) {
                    Toast.makeText(
                        requireContext(),
                        limitResponse.message ?: "Failed to load scan limit",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            return false
        }

        val limitData = limitResponse.data
        val isUnlimited = limitData?.isUnlimited == true
        val isExceeded = limitData?.isExceeded == true
        val remainingLimit = limitData?.remaining ?: 0

        if (!isUnlimited && (isExceeded || remainingLimit <= 0)) {
            withContext(Dispatchers.Main) {
                if (isAdded) {
                    Toast.makeText(
                        requireContext(),
                        limitResponse.message ?: "You have no scans left",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            return false
        }

        return true
    }

    private suspend fun decreaseProductScanLimit(token: String) {
        val decreaseResponse = NetworkClient.userApiService.decreaseProductScanLimit(token)
        if (decreaseResponse.success == false) {
            Log.w(
                "PRODUCT_SCAN_LIMIT",
                "Failed to decrease scan limit after successful AI analysis: ${decreaseResponse.message}"
            )
        }
    }

    private suspend fun loadUserContextParts(): ScanUserContextParts = coroutineScope {
        val token = sessionManager.fetchAuthToken()
        if (token.isNullOrBlank()) return@coroutineScope ScanUserContextParts()

        val detailsDeferred = async {
            runCatching { NetworkClient.userApiService.getUserDetails(token).data }
                .onFailure { Log.e("SCAN_DEBUG", "Failed to load user details for AI scan", it) }
                .getOrNull()
        }
        val caloriesDeferred = async {
            runCatching { NetworkClient.userApiService.getTodayUserCalories(token).data }
                .onFailure { Log.e("SCAN_DEBUG", "Failed to load user calories for AI scan", it) }
                .getOrNull()
        }
        val waterDeferred = async {
            runCatching { NetworkClient.userApiService.getTodayUserWater(token).data }
                .onFailure { Log.e("SCAN_DEBUG", "Failed to load user water for AI scan", it) }
                .getOrNull()
        }

        val results = awaitAll(detailsDeferred, caloriesDeferred, waterDeferred)
        val userInformationJson = JsonObject().apply {
            add("user", results[0]?.let { gson.toJsonTree(it) })
            add("user_calories", results[1]?.let { gson.toJsonTree(it) })
            add("user_water", results[2]?.let { gson.toJsonTree(it) })
            add("user_calories_today", results[1]?.let { gson.toJsonTree(it) })
            add("user_water_today", results[2]?.let { gson.toJsonTree(it) })
        }.toString()

        ScanUserContextParts(
            userInformation = userInformationJson.toRequestBody("application/json".toMediaTypeOrNull()),
            userInformationJson = userInformationJson
        )
    }

    private fun logAiRequestPayload(
        file: File,
        healthInfo: String,
        userContext: ScanUserContextParts
    ) {
        Log.d(
            "AI_REQUEST",
            "Sending scan to AI: file=${file.name}, sizeBytes=${file.length()}, path=${file.absolutePath}"
        )
        Log.d("AI_REQUEST", "health_info=$healthInfo")
        Log.d("AI_REQUEST", "user_information=${userContext.userInformationJson ?: "null"}")
    }

    private fun navigateToAnalysis(response: AnalysisResponse) {
        setLoadingState(false)

        val bundle = Bundle().apply {
            putSerializable("ai_analysis", response)
        }

        runCatching {
            findNavController().navigate(R.id.productDetailFragment, bundle)
        }.onFailure { error ->
            Log.e("SCAN_DEBUG", "Navigation to product detail failed", error)
            Toast.makeText(requireContext(), "Failed to open analysis result", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setLoadingState(loading: Boolean) {
        isAnalyzing = loading
        val currentBinding = _binding ?: return

        currentBinding.loadingOverlay.visibility = if (loading) View.VISIBLE else View.GONE
        currentBinding.captureButton.isEnabled = !loading
        currentBinding.btnUpload.isEnabled = !loading
        currentBinding.btnBrowse.isEnabled = !loading
        currentBinding.btnSearch.isEnabled = !loading
        if (!loading) {
            currentBinding.tvLoadingMessage.text = "Analyzing photo..."
        }
    }

    private fun openGallery() {
        galleryLauncher.launch("image/*")
    }

    private fun uriToFile(uri: Uri): File {
        val inputStream = requireContext().contentResolver.openInputStream(uri)
        val file = File(requireContext().cacheDir, "gallery_${System.currentTimeMillis()}.jpg")
        val outputStream = FileOutputStream(file)
        inputStream?.copyTo(outputStream)
        inputStream?.close()
        outputStream.close()
        return file
    }

    private fun allPermissionsGranted() = arrayOf(Manifest.permission.CAMERA).all {
        ContextCompat.checkSelfPermission(requireContext(), it) == PackageManager.PERMISSION_GRANTED
    }

    override fun onDestroyView() {
        super.onDestroyView()
        cameraExecutor.shutdown()
        _binding = null
    }

    private data class ScanUserContextParts(
        val userInformation: okhttp3.RequestBody? = null,
        val userInformationJson: String? = null
    )
}

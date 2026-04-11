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
import kotlinx.coroutines.Dispatchers
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

    private var _binding: FragmentScanBinding? = null
    private val binding get() = _binding!!

    private var imageCapture: ImageCapture? = null
    private lateinit var cameraExecutor: ExecutorService
    private var isAnalyzing = false

    private val galleryLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            lifecycleScope.launch(Dispatchers.IO) {
                try {
                    val file = uriToFile(it)
                    val compressed = getCompressedFile(file)
                    withContext(Dispatchers.Main) {
                        analyzeImageWithAi(compressed)
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
                            analyzeImageWithAi(compressedFile)
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

    private fun analyzeImageWithAi(file: File) {
        setLoadingState(true)

        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            try {
                val requestFile = file.asRequestBody("image/jpeg".toMediaTypeOrNull())
                val body = MultipartBody.Part.createFormData("file", file.name, requestFile)

                val prefs = requireContext().getSharedPreferences("user_settings", Context.MODE_PRIVATE)
                val diseasesText = prefs.getStringSet("user_diseases", emptySet())?.joinToString(", ") ?: "None"
                val healthInfoBody = diseasesText.toRequestBody("text/plain".toMediaTypeOrNull())

                val response = NetworkClient.aiApiService.analyzeScan(body, healthInfoBody)
                Log.d("SCAN_DEBUG", "AI response received: $response")

                withContext(Dispatchers.Main) {
                    if (isAdded && _binding != null) {
                        navigateToAnalysis(response)
                    }
                }
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
}

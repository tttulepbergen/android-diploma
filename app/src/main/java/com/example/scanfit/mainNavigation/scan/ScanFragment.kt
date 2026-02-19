package com.example.scanfit.mainNavigation.scan

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.scanfit.databinding.FragmentScanBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import okhttp3.RequestBody.Companion.toRequestBody
import android.net.Uri
import androidx.activity.result.contract.ActivityResultContracts
import com.example.scanfit.R
import com.example.scanfit.network.NetworkClient

class ScanFragment : Fragment() {

    private var _binding: FragmentScanBinding? = null
    private val binding get() = _binding!!

    private var imageCapture: ImageCapture? = null
    private lateinit var cameraExecutor: ExecutorService

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
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
            requestPermissions(arrayOf(Manifest.permission.CAMERA), 10)
        }

        binding.captureButton.setOnClickListener { takePhoto() }
        binding.btnBrowse.setOnClickListener { findNavController().navigate(R.id.action_nav_scan_to_categoriesFragment) }
        binding.btnSearch.setOnClickListener { findNavController().navigate(R.id.action_nav_scan_to_searchFragment) }
        binding.btnUpload.setOnClickListener { openGallery() }
    }


    private fun startCamera() {
        val context = context ?: return // Проверка, что фрагмент еще прикреплен к контексту
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)

        cameraProviderFuture.addListener({
            // Используем безопасную проверку _binding
            val currentBinding = _binding ?: return@addListener

            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(currentBinding.viewFinder.surfaceProvider)
            }

            imageCapture = ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                .build()

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider.unbindAll()
                // Используем viewLifecycleOwner только если он доступен
                cameraProvider.bindToLifecycle(viewLifecycleOwner, cameraSelector, preview, imageCapture)
            } catch (exc: Exception) {
                Log.e("SCAN_DEBUG", "Use case binding failed", exc)
            }
        }, ContextCompat.getMainExecutor(context))
    }



    private fun takePhoto() {
        val currentContext = context ?: return
        val imageCapture = imageCapture ?: return
        val photoFile = File(currentContext.cacheDir, "scan_${System.currentTimeMillis()}.jpg")
        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(currentContext),
            object : ImageCapture.OnImageSavedCallback {

                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    // Проверяем, жив ли фрагмент и binding, прежде чем идти дальше
                    if (_binding != null && isAdded) {
                        lifecycleScope.launch(Dispatchers.IO) {
                            try {
                                val compressedFile = getCompressedFile(photoFile)
                                withContext(Dispatchers.Main) {
                                    analyzeImageWithAi(compressedFile)
                                }
                            } catch (e: Exception) {
                                Log.e("SCAN_DEBUG", "Compression failed", e)
                            }
                        }
                    }
                }

                // Тот самый метод, который требовал компилятор:
                override fun onError(exception: ImageCaptureException) {
                    Log.e("SCAN_DEBUG", "Photo capture failed: ${exception.message}", exception)
                    if (isAdded) {
                        Toast.makeText(currentContext, "Ошибка камеры", Toast.LENGTH_SHORT).show()
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
        val prefs = requireContext().getSharedPreferences("user_settings", Context.MODE_PRIVATE)
        // Достаем список
        val diseasesSet = prefs.getStringSet("user_diseases", emptySet())
        val diseasesText = diseasesSet?.joinToString(", ") ?: "Ограничений нет"

        Log.d("SCAN_DEBUG", "=== ПОДГОТОВКА ЗАПРОСА ===")
        Log.d("SCAN_DEBUG", "Файл: ${file.name} (${file.length() / 1024} KB)")
        Log.d("SCAN_DEBUG", "Данные здоровья: $diseasesText")

        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            try {
                val requestFile = file.asRequestBody("image/jpeg".toMediaTypeOrNull())
                val body = MultipartBody.Part.createFormData("file", file.name, requestFile)
                val healthInfoBody = diseasesText.toRequestBody("text/plain".toMediaTypeOrNull())

                Log.d("SCAN_DEBUG", "Отправка на сервер...")
                val response = NetworkClient.aiApiService.analyzeScan(body, healthInfoBody)
                Log.d("SCAN_DEBUG", "Ответ получен: ${response.verdict}")

                withContext(Dispatchers.Main) {
                    if (isAdded && _binding != null) {
                        val bundle = Bundle().apply { putSerializable("ai_analysis", response) }
                        findNavController().navigate(R.id.action_nav_scan_to_productDetailFragment, bundle)
                    }
                }
            } catch (e: Exception) {
                Log.e("SCAN_DEBUG", "ОШИБКА ПРИ ОТПРАВКЕ: ${e.message}")
                e.printStackTrace()
            }
        }
    }

    private fun allPermissionsGranted() = arrayOf(Manifest.permission.CAMERA).all {
        ContextCompat.checkSelfPermission(requireContext(), it) == PackageManager.PERMISSION_GRANTED
    }

    override fun onDestroyView() {
        super.onDestroyView()
        cameraExecutor.shutdown()
        _binding = null
    }

    private val galleryLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val file = uriToFile(it)
            val compressed = getCompressedFile(file)
            analyzeImageWithAi(compressed)
            file.delete()
        }
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
    private fun openGallery() {
        galleryLauncher.launch("image/*")
    }

}
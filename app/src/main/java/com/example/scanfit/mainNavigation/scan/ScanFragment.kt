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
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.scanfit.R
import com.example.scanfit.databinding.FragmentScanBinding
import com.example.scanfit.network.NetworkClient // Импорт от Sunbekova
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

    // Регистрация выбора из галереи (от Sunbekova)
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
                }
            }
        }
    }

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
            @Suppress("DEPRECATION")
            requestPermissions(arrayOf(Manifest.permission.CAMERA), 10)
        }

        binding.captureButton.setOnClickListener {
            Log.d("SCAN_DEBUG", "КНОПКА НАЖАТА!")
            Toast.makeText(requireContext(), "Нажатие зафиксировано", Toast.LENGTH_SHORT).show()
            takePhoto()
        }
        binding.btnBrowse.setOnClickListener { findNavController().navigate(R.id.action_nav_scan_to_categoriesFragment) }
        binding.btnSearch.setOnClickListener { findNavController().navigate(R.id.action_nav_scan_to_searchFragment) }

        // Кнопка загрузки из галереи (от Sunbekova)
        binding.btnUpload.setOnClickListener { openGallery() }
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

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(viewLifecycleOwner, cameraSelector, preview, imageCapture)
            } catch (exc: Exception) {
                Log.e("SCAN_DEBUG", "Use case binding failed", exc)
            }
        }, ContextCompat.getMainExecutor(context))
    }


    private fun takePhoto() {
        Log.d("SCAN_DEBUG", "Функция takePhoto зазвана")
        val currentContext = context ?: return

        // 1. Проверяем, инициализирован ли imageCapture
        val useCase = imageCapture
        if (useCase == null) {
            Log.e("SCAN_DEBUG", "ОШИБКА: imageCapture равен null!")
            Toast.makeText(currentContext, "Камера не готова", Toast.LENGTH_SHORT).show()
            return
        }

        // 2. Создаем файл для фото
        val photoFile = File(currentContext.cacheDir, "scan_${System.currentTimeMillis()}.jpg")
        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        Log.d("SCAN_DEBUG", "Начинаю takePicture...")

        // 3. Делаем снимок
        useCase.takePicture(
            outputOptions,
            cameraExecutor, // Используем фоновый поток, чтобы не тормозил интерфейс
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    Log.d("SCAN_DEBUG", "ФОТО СОХРАНЕНО: ${photoFile.absolutePath}")

                    // Переходим в главный поток для сжатия и отправки
                    lifecycleScope.launch(Dispatchers.Main) {
                        try {
                            val compressedFile = withContext(Dispatchers.IO) { getCompressedFile(photoFile) }
                            analyzeImageWithAi(compressedFile)
                        } catch (e: Exception) {
                            Log.e("SCAN_DEBUG", "Ошибка после сохранения: ${e.message}")
                        }
                    }
                }

                override fun onError(exception: ImageCaptureException) {
                    Log.e("SCAN_DEBUG", "ОШИБКА КАМЕРЫ: ${exception.message}", exception)
                    viewLifecycleOwner.lifecycleScope.launch(Dispatchers.Main) {
                        Toast.makeText(currentContext, "Ошибка при снимке", Toast.LENGTH_SHORT).show()
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
        // Показываем прогресс-бар, если он есть в макете, или Toast
        binding.captureButton.isEnabled = false // Блокируем кнопку, чтобы не спамили
        Toast.makeText(requireContext(), "Анализ пошел, подождите 30-40 сек...", Toast.LENGTH_LONG).show()

        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            try {
                val requestFile = file.asRequestBody("image/jpeg".toMediaTypeOrNull())
                val body = MultipartBody.Part.createFormData("file", file.name, requestFile)

                val prefs = requireContext().getSharedPreferences("user_settings", Context.MODE_PRIVATE)
                val diseasesText = prefs.getStringSet("user_diseases", emptySet())?.joinToString(", ") ?: "None"
                val healthInfoBody = diseasesText.toRequestBody("text/plain".toMediaTypeOrNull())

                val response = NetworkClient.aiApiService.analyzeScan(body, healthInfoBody)

                Log.d("SCAN_DEBUG", "Ответ получен!")

                withContext(Dispatchers.Main) {
                    // Проверяем, жив ли еще фрагмент перед навигацией
                    if (isAdded && _binding != null) {
                        val bundle = Bundle().apply {
                            putSerializable("ai_analysis", response)
                        }

                        // БЕЗОПАСНАЯ НАВИГАЦИЯ
                        val navController = findNavController()
                        if (navController.currentDestination?.id == R.id.nav_scan) {
                            navController.navigate(R.id.action_nav_scan_to_productDetailFragment, bundle)
                        }
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    binding.captureButton.isEnabled = true
                    Log.e("SCAN_DEBUG", "Ошибка: ${e.message}")
                    Toast.makeText(requireContext(), "Ошибка сети: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            } finally {
                file.delete() // Чистим кеш в любом случае
            }
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
}
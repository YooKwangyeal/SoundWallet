package com.example.soundwallet

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.soundwallet.ui.theme.SoundWalletTheme
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import java.util.Locale

class MainActivity : ComponentActivity(), TextToSpeech.OnInitListener {

    private lateinit var previewView: PreviewView
    private lateinit var tts: TextToSpeech
    private var tflite: Interpreter? = null
    private var imageCapture: ImageCapture? = null

    private val selectImageLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val uri = result.data?.data
            uri?.let {
                val inputStream = contentResolver.openInputStream(it)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                inputStream?.close()
                bitmap?.let { runInferenceAndSpeak(it) }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        tts = TextToSpeech(this, this)
        tflite = loadModelFile("best_float16.tflite")

        // 입력 shape 확인
        tflite?.let {
            val inputShape = it.getInputTensor(0).shape()
            Log.d("TFLite", "모델 입력 shape: ${inputShape.joinToString()}")
        }

        if (!allPermissionsGranted()) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.CAMERA),
                10
            )
        }

        setContent {
            SoundWalletTheme {
                CameraPreviewComposable()
            }
        }
    }

    private fun loadModelFile(modelName: String): Interpreter? {
        return try {
            val fileDescriptor = assets.openFd(modelName)
            val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
            val fileChannel = inputStream.channel
            val startOffset = fileDescriptor.startOffset
            val declaredLength = fileDescriptor.declaredLength
            val mappedByteBuffer: MappedByteBuffer =
                fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
            Interpreter(mappedByteBuffer)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts.language = Locale.KOREAN
        }
    }

    private fun runInferenceAndSpeak(bitmap: Bitmap) {
        val inputSize = 640
        val inputBuffer = ByteBuffer.allocateDirect(1 * inputSize * inputSize * 3 * 4)
        inputBuffer.order(ByteOrder.nativeOrder())

        val resized = Bitmap.createScaledBitmap(bitmap, inputSize, inputSize, true)
        val pixels = IntArray(inputSize * inputSize)
        resized.getPixels(pixels, 0, inputSize, 0, 0, inputSize, inputSize)

        for (pixel in pixels) {
            val r = (pixel shr 16 and 0xFF) / 255.0f
            val g = (pixel shr 8 and 0xFF) / 255.0f
            val b = (pixel and 0xFF) / 255.0f
            inputBuffer.putFloat(r)
            inputBuffer.putFloat(g)
            inputBuffer.putFloat(b)
        }
        inputBuffer.rewind()

        // YOLOv8 TFLite 기본 출력: [1, 100, 6] → x, y, w, h, conf, class_id
        val output = Array(1) { Array(300) { FloatArray(6) } }
        tflite?.run(inputBuffer, output)

        val classToAmount = mapOf(
            0 to "10원",
            1 to "50원",
            2 to "100원",
            3 to "500원",
            4 to "1000원",
            5 to "5000원",
            6 to "10000원",
            7 to "50000원"
        )

        val detectedAmounts = mutableListOf<Int>()  // 💡 Int로 변경 (합산용)
        for (i in output[0].indices) {
            val conf = output[0][i][4]
            val classId = output[0][i][5].toInt()

            if (conf > 0.5f && classId in classToAmount.keys) {
                val amount = when (classId) {
                    0 -> 10
                    1 -> 50
                    2 -> 100
                    3 -> 500
                    4 -> 1000
                    5 -> 5000
                    6 -> 10000
                    7 -> 50000
                    else -> 0
                }
                detectedAmounts.add(amount)
            }
        }

        val total = detectedAmounts.sum()
        val message = if (total > 0)
            "감지된 금액의 총합은 ${total}원 입니다."
        else
            "감지된 금액이 없습니다."

        tts.speak(message, TextToSpeech.QUEUE_FLUSH, null, null)

    }

    private fun takePictureAndRunInference() {
        val imageCapture = imageCapture ?: return

        imageCapture.takePicture(
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageCapturedCallback() {
                override fun onCaptureSuccess(image: ImageProxy) {
                    val bitmap = image.toBitmap()
                    image.close()
                    bitmap?.let { runInferenceAndSpeak(it) }
                }

                override fun onError(exception: ImageCaptureException) {
                    Log.e("Camera", "사진 캡처 실패", exception)
                }
            }
        )
    }

    private fun startCamera(previewView: PreviewView) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }

            imageCapture = ImageCapture.Builder().build()

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageCapture
                )
            } catch (e: Exception) {
                Log.e("CameraX", "카메라 바인딩 실패", e)
            }
        }, ContextCompat.getMainExecutor(this))
    }

    @Composable
    fun CameraPreviewComposable() {
        val context = LocalContext.current

        AndroidView(factory = { ctx ->
            PreviewView(ctx).apply {
                previewView = this
                post { startCamera(this) }
            }
        },
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { _: Offset ->
                        takePictureAndRunInference() // ✅ 터치 시 직접 카메라 캡처
                    }
                )
            }
        )
    }

    private fun allPermissionsGranted(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    override fun onDestroy() {
        tts.shutdown()
        tflite?.close()
        super.onDestroy()
    }
}

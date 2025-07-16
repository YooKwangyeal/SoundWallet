package com.example.soundwallet

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.clickable
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.soundwallet.ui.theme.SoundWalletTheme
import kotlinx.coroutines.launch
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import java.util.Locale

class MainActivity() : ComponentActivity(), TextToSpeech.OnInitListener, Parcelable {

    private lateinit var previewView: PreviewView
    private lateinit var tfliteGeneral: Interpreter
    private lateinit var tfliteCoin: Interpreter
    private lateinit var tts: TextToSpeech
    private var imageCapture: ImageCapture? = null

    private val selectImageLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
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

    constructor(parcel: Parcel) : this() {

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        tts = TextToSpeech(this, this)

        tfliteGeneral = loadModelFile("best_float16.tflite")!!
        tfliteCoin = loadModelFile("best_coin_float16.tflite")!!

        setContent {
            SoundWalletTheme {
                MainScreenWithDrawer()
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
        val resultsGeneral = runInference(bitmap, tfliteGeneral)
        val resultsCoin = runInference(bitmap, tfliteCoin)

        // 두 모델 결과 합치고 박스 중복만 제거
        val allResults = resultsGeneral + resultsCoin
        val filteredResults = removeDuplicates(allResults)

        Log.d("RESULT", "Model: General → ${resultsGeneral.size}개 감지")
        Log.d("RESULT", "Model: Coin → ${resultsCoin.size}개 감지")
        Log.d("RESULT", "Merged → ${filteredResults.size}개 최종 반영")

        if (filteredResults.isNotEmpty()) {
            val amounts = filteredResults.map { it.amount }
            val spoken = filteredResults.map { "${it.amount}원" }
            val total = amounts.sum()
            val message = "감지된 금액은 ${spoken.joinToString(", ")}이며, 총합은 ${total}원 입니다."
            tts.speak(message, TextToSpeech.QUEUE_FLUSH, null, null)
        } else {
            tts.speak("감지된 금액이 없습니다.", TextToSpeech.QUEUE_FLUSH, null, null)
        }
    }

    data class DetectionResult(
        val classId: Int,
        val conf: Float,
        val amount: Int,
        val box: FloatArray // [x1, y1, x2, y2]
    )

    private fun runInference(bitmap: Bitmap, interpreter: Interpreter): List<DetectionResult> {
        val inputSize = 832
        val inputBuffer = ByteBuffer.allocateDirect(1 * inputSize * inputSize * 3 * 4).apply {
            order(ByteOrder.nativeOrder())
        }

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

        val output = Array(1) { Array(300) { FloatArray(6) } }
        interpreter.run(inputBuffer, output)

        val results = mutableListOf<DetectionResult>()

        for (i in output[0].indices) {
            val conf = output[0][i][4]
            val classId = output[0][i][5].toInt()
            val x1 = output[0][i][0]
            val y1 = output[0][i][1]
            val x2 = output[0][i][2]
            val y2 = output[0][i][3]

            if (conf > 0.5f) {
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
                if (amount > 0) {
                    results.add(
                        DetectionResult(
                            classId,
                            conf,
                            amount,
                            floatArrayOf(x1, y1, x2, y2)
                        )
                    )
                }
            }
        }

        return results
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

        AndroidView(
            factory = { ctx ->
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

    private fun calculateIoU(boxA: FloatArray, boxB: FloatArray): Float {
        val xA = maxOf(boxA[0], boxB[0])
        val yA = maxOf(boxA[1], boxB[1])
        val xB = minOf(boxA[2], boxB[2])
        val yB = minOf(boxA[3], boxB[3])

        val interArea = maxOf(0f, xB - xA) * maxOf(0f, yB - yA)
        if (interArea <= 0f) return 0f

        val boxAArea = (boxA[2] - boxA[0]) * (boxA[3] - boxA[1])
        val boxBArea = (boxB[2] - boxB[0]) * (boxB[3] - boxB[1])
        val unionArea = boxAArea + boxBArea - interArea

        return if (unionArea > 0f) interArea / unionArea else 0f
    }

    private fun removeDuplicates(
        detections: List<DetectionResult>,
        iouThreshold: Float = 0.3f
    ): List<DetectionResult> {
        val sorted = detections.sortedByDescending { it.conf }
        val result = mutableListOf<DetectionResult>()
        val used = BooleanArray(sorted.size)

        for (i in sorted.indices) {
            if (used[i]) continue
            val detA = sorted[i]
            result.add(detA)
            for (j in i + 1 until sorted.size) {
                if (used[j]) continue
                val detB = sorted[j]
                if (detA.classId == detB.classId && calculateIoU(
                        detA.box,
                        detB.box
                    ) > iouThreshold
                ) {
                    used[j] = true
                }
            }
        }
        return result
    }

    override fun onDestroy() {
        tts.shutdown()
        tfliteGeneral?.close()
        tfliteCoin?.close()
        super.onDestroy()
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {

    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<MainActivity> {
        override fun createFromParcel(parcel: Parcel): MainActivity {
            return MainActivity(parcel)
        }

        override fun newArray(size: Int): Array<MainActivity?> {
            return arrayOfNulls(size)
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun MainScreenWithDrawer() {
        val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
        val scope = rememberCoroutineScope()
        val context = LocalContext.current // 여기에서 선언
        var languageExpanded by remember { mutableStateOf(false) }
        var selectedLanguage by remember { mutableStateOf("한국어") }

        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                ModalDrawerSheet {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "Settings",
                            style = MaterialTheme.typography.titleLarge,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                        Divider()
                        // 언어 설정 메뉴
                        Text(
                            "언어 설정",
                            modifier = Modifier
                                .padding(vertical = 8.dp)
                                .clickable { languageExpanded = !languageExpanded }
                        )
                        // 언어 선택 하위 항목
                        if (languageExpanded) {
                            Column(modifier = Modifier.padding(start = 16.dp)) {
                                val languages = listOf("한국어", "영어", "일본어", "중국어")
                                languages.forEach { lang ->
                                    Text(
                                        lang,
                                        modifier = Modifier
                                            .padding(vertical = 4.dp)
                                            .clickable {
                                                selectedLanguage = lang
                                                languageExpanded = false
                                                // 여기서 실제 언어 변경 로직 추가 가능
                                            },
                                        color = if (selectedLanguage == lang) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                        Text(
                            "tts 연결",
                            modifier = Modifier
                                .padding(vertical = 8.dp)
                                .clickable {
                                    val url = "http://172.30.1.57:8000/common/tts/tts_view/"
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                    context.startActivity(intent) // context 사용
                                }
                        )
                    }
                }
            }
        ) {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text("SoundWallet") },
                        navigationIcon = {
                            IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                Icon(Icons.Default.Menu, contentDescription = "메뉴")
                            }
                        }
                    )
                }
            ) { innerPadding ->
                CameraPreviewComposable()
            }
        }
    }
}
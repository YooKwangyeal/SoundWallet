# Developer Documentation 🔧

## Project Structure

```
SoundWallet/
├── app/
│   ├── src/main/
│   │   ├── java/com/example/soundwallet/
│   │   │   ├── MainActivity.kt          # Main application logic
│   │   │   ├── Utils.kt                 # Image processing utilities
│   │   │   ├── sum.kt                   # Currency detection logic
│   │   │   └── ui/theme/                # UI theme components
│   │   ├── assets/
│   │   │   └── best_float16.tflite      # TensorFlow Lite model
│   │   └── AndroidManifest.xml          # App permissions & config
│   └── build.gradle.kts                 # App-level build configuration
├── gradle/libs.versions.toml            # Dependency versions
├── pt_to_tflite.ipynb                   # Model conversion notebook
└── README.md                            # User documentation
```

## Key Dependencies

### Core Android Libraries
- **CameraX**: Camera functionality with lifecycle management
- **Jetpack Compose**: Modern UI toolkit
- **TensorFlow Lite**: Machine learning inference
- **Text-to-Speech**: Audio feedback system

### Version Configuration
```kotlin
// gradle/libs.versions.toml
agp = "8.4.0"                    // Android Gradle Plugin
kotlin = "2.0.21"                // Kotlin compiler
compileSdk = 35                  // Target Android API
minSdk = 24                      // Minimum Android API
```

## Core Components

### 1. MainActivity.kt
Primary application controller that handles:
- Camera initialization and lifecycle
- TensorFlow Lite model loading
- Image capture and processing
- TTS initialization and audio feedback
- UI composition and event handling

Key methods:
- `onCreate()`: App initialization
- `loadModelFile()`: Loads TFLite model from assets
- `runInferenceAndSpeak()`: Processes image and provides audio feedback
- `takePictureAndRunInference()`: Captures and analyzes camera frame
- `startCamera()`: Initializes CameraX

### 2. Utils.kt
Image processing utilities:
- `ImageProxy.toBitmap()`: Converts camera frame to bitmap
- Handles YUV to RGB conversion
- Manages image format transformations

### 3. sum.kt
Currency detection and calculation:
- Processes model output
- Maps class IDs to currency values
- Calculates total sum of detected currencies
- Filters detections by confidence threshold

## Machine Learning Pipeline

### Model Architecture
- **Base Model**: YOLOv8 object detection
- **Input Format**: 640x640x3 RGB images
- **Output Format**: [1, 300, 6] tensor (x, y, w, h, confidence, class_id)
- **Quantization**: Float16 for mobile optimization

### Inference Process
1. **Image Preprocessing**:
   ```kotlin
   val resized = Bitmap.createScaledBitmap(bitmap, 640, 640, true)
   val pixels = IntArray(640 * 640)
   resized.getPixels(pixels, 0, 640, 0, 0, 640, 640)
   ```

2. **Normalization**:
   ```kotlin
   val r = (pixel shr 16 and 0xFF) / 255.0f
   val g = (pixel shr 8 and 0xFF) / 255.0f
   val b = (pixel and 0xFF) / 255.0f
   ```

3. **Model Inference**:
   ```kotlin
   val output = Array(1) { Array(300) { FloatArray(6) } }
   tflite?.run(inputBuffer, output)
   ```

4. **Post-processing**:
   ```kotlin
   val conf = output[0][i][4]  // Confidence score
   val classId = output[0][i][5].toInt()  // Class ID
   if (conf > 0.5f && classId in classToAmount.keys) {
       // Process detection
   }
   ```

### Currency Classes
```kotlin
val classToAmount = mapOf(
    0 to "10원",      // 10 won coin
    1 to "50원",      // 50 won coin
    2 to "100원",     // 100 won coin
    3 to "500원",     // 500 won coin
    4 to "1000원",    // 1,000 won banknote
    5 to "5000원",    // 5,000 won banknote
    6 to "10000원",   // 10,000 won banknote
    7 to "50000원"    // 50,000 won banknote
)
```

## Camera Integration

### CameraX Configuration
```kotlin
val preview = Preview.Builder().build()
val imageCapture = ImageCapture.Builder().build()
val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

cameraProvider.bindToLifecycle(
    this, cameraSelector, preview, imageCapture
)
```

### Image Capture Flow
1. User taps screen → `detectTapGestures()`
2. Triggers `takePictureAndRunInference()`
3. Captures current camera frame
4. Converts to bitmap via `toBitmap()`
5. Processes through ML model
6. Announces results via TTS

## Audio System

### TTS Configuration
```kotlin
private lateinit var tts: TextToSpeech
tts = TextToSpeech(this, this)
tts.language = Locale.KOREAN
```

### Audio Feedback
- **Success**: "감지된 금액의 총합은 {amount}원 입니다."
- **No Detection**: "감지된 금액이 없습니다."
- **Queue Management**: `TextToSpeech.QUEUE_FLUSH` for immediate playback

## Permissions

### Required Permissions
```xml
<uses-permission android:name="android.permission.CAMERA" />
<uses-permission android:name="android.permission.RECORD_AUDIO" />
```

### Runtime Permission Handling
```kotlin
if (!allPermissionsGranted()) {
    ActivityCompat.requestPermissions(
        this, arrayOf(Manifest.permission.CAMERA), 10
    )
}
```

## Build Configuration

### Gradle Dependencies
```kotlin
dependencies {
    // TensorFlow Lite
    implementation("org.tensorflow:tensorflow-lite:2.13.0")
    implementation("org.tensorflow:tensorflow-lite-support:0.4.3")
    implementation("org.tensorflow:tensorflow-lite-gpu-delegate-plugin:0.4.3")
    
    // CameraX
    implementation("androidx.camera:camera-core:1.3.1")
    implementation("androidx.camera:camera-camera2:1.3.1")
    implementation("androidx.camera:camera-lifecycle:1.3.1")
    implementation("androidx.camera:camera-view:1.3.1")
    
    // Jetpack Compose
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.material3)
}
```

## Model Training (pt_to_tflite.ipynb)

The Jupyter notebook contains the pipeline for:
1. **Data Preparation**: Currency dataset preparation
2. **Model Training**: YOLOv8 training configuration
3. **Model Conversion**: PyTorch to TensorFlow Lite conversion
4. **Optimization**: Float16 quantization for mobile deployment

## Performance Optimization

### Model Optimization
- **Quantization**: Float16 reduces model size by ~50%
- **Input Resolution**: 640x640 balances accuracy vs. speed
- **Confidence Threshold**: 0.5 filters false positives

### Memory Management
- **Bitmap Recycling**: Proper cleanup of image resources
- **Model Caching**: Single model instance across app lifecycle
- **Buffer Reuse**: Efficient ByteBuffer management

## Testing Strategies

### Unit Testing
- Test currency detection logic
- Validate confidence threshold filtering
- Verify sum calculation accuracy

### Integration Testing
- Camera capture functionality
- TTS audio feedback
- Model inference pipeline

### Accessibility Testing
- Screen reader compatibility
- Voice control support
- High contrast mode testing

## Future Enhancements

### Potential Features
1. **Multi-currency Support**: Extend to other currencies
2. **Batch Processing**: Process multiple images simultaneously
3. **Real-time Detection**: Continuous detection without tap
4. **Denomination Counting**: Count specific denominations
5. **History Tracking**: Save detection results
6. **Voice Commands**: Voice-controlled operation

### Technical Improvements
1. **Model Optimization**: Further quantization and pruning
2. **Edge Computing**: On-device model training
3. **Offline Support**: Cached model improvements
4. **Performance Monitoring**: Real-time performance analytics

---

*This documentation provides developers with comprehensive information to understand, modify, and extend the SoundWallet application.*
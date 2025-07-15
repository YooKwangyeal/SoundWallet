# SoundWallet 💰🔊

**SoundWallet** is an accessibility-focused Android application that helps visually impaired users identify Korean currency (coins and banknotes) through computer vision and audio feedback.

## 🎯 What Can SoundWallet Do?

### Core Features
- **📱 Real-time Currency Recognition**: Point your camera at Korean currency and get instant audio feedback
- **🔊 Audio Feedback**: Hear the detected currency amounts in Korean through Text-to-Speech (TTS)
- **💰 Sum Calculation**: Automatically calculates and announces the total value of multiple detected currencies
- **👆 Simple Touch Interface**: Just tap the camera preview to capture and analyze currency

### Supported Currency Types
SoundWallet can detect and identify the following Korean currencies:
- **Coins**: 10원, 50원, 100원, 500원
- **Banknotes**: 1,000원, 5,000원, 10,000원, 50,000원

### Accessibility Features
- **Audio-First Design**: All feedback is provided through clear Korean audio announcements
- **Simple Interaction**: Single tap to capture and analyze - no complex gestures required
- **Real-time Camera Preview**: Live camera feed for easy positioning
- **High Accuracy**: Uses advanced YOLOv8 object detection model for reliable currency recognition

## 🛠️ Technical Capabilities

### Technology Stack
- **Machine Learning**: TensorFlow Lite with YOLOv8 object detection model
- **Computer Vision**: Real-time image processing and currency recognition
- **Audio Processing**: Android Text-to-Speech (TTS) with Korean language support
- **Camera Integration**: CameraX for smooth camera operations
- **UI Framework**: Jetpack Compose for modern Android UI

### Performance Features
- **Optimized Model**: Uses float16 quantized model for efficient mobile performance
- **Fast Processing**: Real-time inference with 640x640 input resolution
- **Low Latency**: Immediate audio feedback after capture
- **Confidence Threshold**: 50% confidence threshold for reliable detection

## 🚀 How It Works

1. **Camera Setup**: Opens rear camera with live preview
2. **Currency Detection**: Tap anywhere on the screen to capture current camera frame
3. **AI Processing**: Runs inference using the trained YOLOv8 TensorFlow Lite model
4. **Result Analysis**: Identifies currency types and calculates total value
5. **Audio Feedback**: Announces results in Korean through TTS

### Example Usage Scenarios
- **Shopping**: Quickly identify bills and coins when making purchases
- **Counting Money**: Count and sum up multiple currencies at once
- **Learning**: Help users learn to recognize different Korean currency denominations
- **Accessibility**: Enable visually impaired users to independently handle money

## 🔧 Technical Details

### Model Specifications
- **Input Size**: 640x640 pixels
- **Model Format**: TensorFlow Lite (.tflite)
- **Quantization**: Float16 for optimal mobile performance
- **Detection Classes**: 8 currency types (coins: 4, banknotes: 4)

### System Requirements
- **Android Version**: API 24 (Android 7.0) or higher
- **Target SDK**: API 35 (Android 15)
- **Permissions**: Camera and Audio recording
- **Language**: Korean TTS support

### Key Components
- **MainActivity.kt**: Main application logic and camera handling
- **Utils.kt**: Image processing utilities for camera frames
- **sum.kt**: Currency detection and sum calculation logic
- **best_float16.tflite**: Pre-trained currency detection model

## 🌟 Benefits for Users

### For Visually Impaired Users
- **Independence**: Handle money transactions without assistance
- **Confidence**: Verify currency amounts with audio confirmation
- **Learning**: Familiarize with different currency denominations
- **Privacy**: Discreetly check money without asking others

### For General Users
- **Quick Verification**: Rapidly verify currency amounts
- **Educational**: Learn about Korean currency features
- **Convenience**: Hands-free currency identification
- **Accessibility Support**: Assist visually impaired family/friends

## 📱 User Interface

- **Fullscreen Camera**: Maximizes camera preview for easy positioning
- **Touch Anywhere**: Simple tap-anywhere interface for capturing
- **Audio Feedback**: Clear Korean audio announcements
- **No Visual Complexity**: Minimal UI to focus on functionality

## 🔬 Machine Learning Model

The app uses a custom-trained YOLOv8 model specifically optimized for Korean currency detection:
- **Training Data**: Specialized dataset of Korean coins and banknotes
- **Preprocessing**: Automatic image scaling and normalization
- **Postprocessing**: Confidence filtering and duplicate removal
- **Optimization**: Mobile-optimized inference pipeline

## 🎓 Educational Value

SoundWallet can serve as:
- **Accessibility Tool**: Primary tool for visually impaired users
- **Learning Aid**: Educational resource for currency recognition
- **Demo Application**: Example of mobile AI implementation
- **Research Platform**: Base for further accessibility research

---

*SoundWallet empowers users to independently identify and count Korean currency through the power of computer vision and audio feedback, making money management more accessible for everyone.*
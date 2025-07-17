# 💸 SoundWallet

스마트폰 카메라를 활용하여 지폐와 동전을 인식하고, 총 금액을 계산해 음성으로 안내해주는 시각장애인 보조 어플리케이션입니다.

## 📱 주요 기능

- 스마트폰 카메라로 지폐 및 동전 실시간 인식
- YOLOv8 및 TensorFlow 기반 정확한 객체 탐지
- 동전 중복 감지 제거 로직 포함 (Dual Model Architecture)
- 총 금액 계산 및 시각화 표시
- TTS(Text-To-Speech)를 통한 음성 안내

## 사용 기술

- Jetpack Compose (Material3)
- CameraX
- TensorFlow Lite
- Android TTS (TextToSpeech)

## 실행 방법

1. Android Studio에서 프로젝트를 엽니다.
2. 실제 기기 또는 에뮬레이터에서 실행합니다.
3. 앱 실행 후 카메라 화면에서 터치하면 인식 및 음성 안내가 동작합니다.
4. 좌측 상단 메뉴(햄버거 버튼)를 눌러 사이드 메뉴를 열 수 있습니다.

## 사이드 메뉴

- **언어 설정**: 원하는 언어를 선택하면 앱 내 안내 언어가 변경됩니다.
- **tts 연결**: TTS 테스트용 웹페이지로 이동합니다.

## 참고

- TensorFlow Lite 모델 파일(`best_float16.tflite`, `best_coin_float16.tflite`)은 `assets` 폴더에 위치해야 합니다.
- 카메라 및 오디오 권한이 필요합니다.

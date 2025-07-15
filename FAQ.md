# Frequently Asked Questions (FAQ) ❓

## General Questions

### What is SoundWallet?
SoundWallet is an Android application that uses artificial intelligence to identify Korean currency (coins and banknotes) and provides audio feedback in Korean. It's designed to help visually impaired users and anyone who needs quick currency verification.

### How does SoundWallet work?
The app uses your phone's camera to capture images of Korean currency, processes them using a trained AI model (YOLOv8), identifies the currency types, calculates the total value, and announces the results in Korean through text-to-speech.

### Who can use SoundWallet?
- Visually impaired individuals who need to identify currency independently
- Anyone learning about Korean currency
- People who need quick currency verification
- Retail workers handling cash
- Educators teaching about Korean money

### Is SoundWallet free to use?
Yes, SoundWallet is an open-source project available for free use.

## Technical Questions

### What currencies does SoundWallet support?
SoundWallet supports all current Korean currency denominations:
- **Coins**: 10원, 50원, 100원, 500원
- **Banknotes**: 1,000원, 5,000원, 10,000원, 50,000원

### What Android version do I need?
SoundWallet requires Android 7.0 (API level 24) or higher.

### Does SoundWallet work offline?
Yes! SoundWallet works completely offline. No internet connection is required for currency recognition.

### How accurate is the currency recognition?
The app uses a confidence threshold of 50% to filter out uncertain detections. In good lighting conditions with clear currency images, accuracy is very high.

### How fast is the processing?
Currency recognition typically takes less than 1 second from capture to audio feedback.

## Privacy and Security

### Does SoundWallet store my images?
No. SoundWallet does not store any images or personal data. All processing happens locally on your device and images are discarded after processing.

### Does SoundWallet send data to servers?
No. SoundWallet operates completely offline and does not transmit any data to external servers.

### What permissions does SoundWallet need?
- **Camera**: To capture images of currency
- **Audio/Microphone**: For text-to-speech functionality

### Is my privacy protected?
Yes. SoundWallet is designed with privacy-by-design principles. No data collection, no tracking, and complete offline operation ensure your privacy.

## Usage Questions

### How do I use SoundWallet?
1. Open the app (camera preview appears)
2. Point your phone camera at Korean currency
3. Tap anywhere on the screen to capture
4. Listen to the Korean audio feedback

### Can I detect multiple currencies at once?
Yes! SoundWallet can detect multiple currencies in a single image and will calculate the total sum.

### What if the app doesn't detect my currency?
Try these tips:
- Ensure good lighting
- Clean the camera lens
- Position currency clearly in view
- Flatten wrinkled banknotes
- Try different angles

### Why is the audio in Korean only?
SoundWallet is specifically designed for Korean currency, so Korean text-to-speech provides the most natural and accurate pronunciation of currency amounts.

## Accessibility Questions

### Is SoundWallet accessible for blind users?
Yes! SoundWallet is specifically designed with accessibility in mind:
- Audio-first interface
- Simple tap-anywhere interaction
- Clear Korean audio feedback
- Compatible with screen readers

### Can I use SoundWallet with TalkBack?
Yes, SoundWallet is compatible with Android's TalkBack and other accessibility services.

### How can I adjust the speech volume?
Use your device's volume controls to adjust the text-to-speech volume.

### Does SoundWallet work with hearing aids?
Yes, the audio output works through your device's standard audio system and is compatible with hearing aids and other assistive listening devices.

## Troubleshooting

### The app says "No currency detected" - what should I do?
- Check lighting conditions (avoid shadows and glare)
- Ensure the currency is fully visible in the camera view
- Clean your camera lens
- Try holding the phone at different distances
- Make sure the currency is flat and unfolded

### The camera won't start - what's wrong?
- Check if camera permissions are granted
- Ensure no other apps are using the camera
- Restart the SoundWallet app
- Restart your phone if the issue persists

### The audio isn't working - how can I fix it?
- Check your device's volume settings
- Ensure Korean TTS is installed on your device
- Try restarting the app
- Check if other apps can use text-to-speech

### The app is running slowly - what can I do?
- Close other apps to free up memory
- Restart the SoundWallet app
- Ensure you have sufficient storage space
- Consider restarting your device

### Can I use SoundWallet on tablets?
Yes, SoundWallet works on Android tablets as well as phones.

## Development Questions

### Is SoundWallet open source?
Yes, SoundWallet is an open-source project. You can find the source code on GitHub.

### Can I contribute to SoundWallet?
Yes! Contributions are welcome. You can report bugs, suggest features, or contribute code through the GitHub repository.

### How was the AI model trained?
The model was trained using YOLOv8 on a dataset of Korean currency images. The training process is documented in the `pt_to_tflite.ipynb` notebook.

### Can SoundWallet be extended to other currencies?
Yes, the architecture is designed to be extensible. New currency support would require training data and model updates.

## Support

### Where can I get help?
- Check this FAQ first
- Review the documentation files (README.md, USAGE.md, etc.)
- Report issues on the GitHub repository
- Join community discussions

### How do I report bugs?
Please report bugs on the GitHub repository with:
- Description of the issue
- Steps to reproduce
- Device information
- Android version

### Can I request new features?
Yes! Feature requests are welcome on the GitHub repository. Please describe the feature and explain how it would benefit users.

### Is there a user manual?
Yes, comprehensive documentation is available:
- **README.md**: Overview and features
- **USAGE.md**: Installation and usage guide
- **CAPABILITIES.md**: Detailed capabilities
- **DEVELOPER.md**: Technical documentation

---

## Still Have Questions?

If you have questions not covered in this FAQ, please:
1. Check the other documentation files
2. Search existing GitHub issues
3. Create a new issue on GitHub
4. Join the community discussions

---

*SoundWallet is designed to be intuitive and accessible. Most users can start using it immediately without extensive setup or learning.*
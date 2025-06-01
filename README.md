<!--
Keywords: Filipino Braille recognition app, Tagalog Braille OCR, YOLOv8 Braille detection, Android Braille to Text app, Braille alphabet translator, Text-to-Speech for Braille, accessible reading app Philippines, Filipino Tagalog Braille Application, Grade 1 Braille, Grade 2 Braille, Filipino Braille OCR
-->

# 📱 Braille-lens: Filipino-Tagalog Braille Recognition App

![Android](https://img.shields.io/badge/Android-3DDC84?style=for-the-badge&logo=android&logoColor=white)
![Kotlin](https://img.shields.io/badge/kotlin-%237F52FF.svg?style=for-the-badge&logo=kotlin&logoColor=white)
![PyTorch](https://img.shields.io/badge/PyTorch-%23EE4C2C.svg?style=for-the-badge&logo=PyTorch&logoColor=white)

---

**Braille-lens** is a mobile application developed to recognize and translate Filipino (Tagalog) Braille characters and contractions into text with Text-to-Speech.  

Developed as a thesis under the **Department of Computer Studies**, **Cavite State University – CCAT Campus**.

---

## 🎓 Thesis Title

**_Braille-Lens: Tagalog Braille Alphabet and Contractions Recognition with Text-to-Speech Using YOLOv8_**  
**Researchers**: Jyra Mae Celajes, Crestalyn Luardo, Louie Jenn Jaspe  
**Course**: Bachelor of Science in Computer Science

---

## 🧠 Features

- 🇵🇭 **Filipino Braille Recognition**  
  Detects Grade 1 and Grade 2 Braille characters using a YOLOv8-based object detection model.

- 📷 **Capture / Import / Sample**  
  Capture Braille images using the camera, import from gallery, or test with sample images.

- 🔄 **Grade Options**  
  Toggle between Grade 1, Grade 2, or both models for Braille detection.

- 🧠 **Supported Braille Patterns**
  - English Grade 1 alphabet  
  - Filipino alphabet (includes **ñ** and **ng**)  
  - Capital sign, number sign  
  - Digits (1–9)  
  - Filipino-Tagalog One-cell contractions (alphabet and non-alphabet)  
  - Filipino-Tagalog One-cell part words  
  - Filipino-Tagalog Two-cell contractions (alphabet and non-alphabet)

- 🟤 **Real and Simulated Braille Support**  
  Works with both:
  - Simulated Braille (black dot representations)
  - Written/embedded Braille (captured with lighting that shows dot shadows)
_See samples here:_ [Sample Input Images](https://drive.google.com/drive/folders/1EsSBbHq_mlTkhx3lyY38UYMZQOD6b178?usp=drive_link)

- 📝 **Braille to Text + TTS**  
  Converts detected Braille into Tagalog text and reads it aloud using Text-to-Speech.

- 📖 **Dictionary**  
  Built-in searchable reference for Filipino Grade 1 and Grade 2 Braille symbols.

- 🎬 **User Guide**  
  “How to Use Braille-lens” section includes a demo video and instructions.

- ⚙️ **Settings**  
  - Adjust TTS pitch and rate  
  - Preview speech output  
  - Switch between light and dark theme

- 📊 **Result Screen**  
  Displays detection results, confidence score, and an information sheet.

- ℹ️ **About Page**  
  Includes background on Braille, Filipino Braille rules, Braille grades, app purpose, and resources.

---

## 🎥 Demo Video

Watch the demo of Braille-lens in action:

[Demo Video (English)](https://drive.google.com/file/d/1z8Z42ei2bMmutK8rciqKvnvAoepmRwfd/view?usp=drive_link)

---

## 📈 Model Performance (YOLOv8)

| Model Type        | Dataset                       | Precision | Recall | mAP50 | mAP50-95 |
|-------------------|-------------------------------|-----------|--------|--------|-----------|
| Grade 1 Braille   | Custom Filipino Braille Dataset | 0.97      | 0.97   | 0.99   | 0.84      |
| Grade 2 Braille   | Custom Filipino Braille Dataset | 0.98      | 0.98   | 0.99   | 0.80      |

> Evaluated on over 21,000 annotated Braille cell images with 247,050 annotations, covering 67 classes for Grade 1 and 89 classes for Grade 2. Trained using YOLOv8-small (YOLOv8s) and deployed via TFLite for Android inference.

---

## 📦 APK Info

- 🔖 **Latest Release**: [v1.0](https://github.com/aemiio/braille-lens-kotlin/releases)
- 📁 **APK**: `braille-lens-v1.0.apk`
- 📱 **Platform**: Android
- ⚙️ **Built with**: Kotlin, Jetpack Compose, YOLOv8, PyTorch, TFLite

---

## 📚 Academic Information

> **University**: Cavite State University – CCAT Campus  
> **Department**: Department of Computer Studies  
> **Thesis Members**:
> - Jyra Mae Celajes  
> - Crestalyn Luardo  
> - Louie Jenn Jaspe

---

## 🛠 Tech Stack

- 🧠 YOLOv8 object detection (PyTorch)
- 📱 Kotlin + Jetpack Compose
- 🗣️ Android Text-to-Speech
- 🗂️ TFLite integration
- 🖼️ Roboflow, Canva, and Braille dataset curation

---

## 📄 License

This project is for academic and educational use only.  
Please contact the authors for permission regarding reuse or publication.

---

## 🙌 Acknowledgements

- Ultralytics YOLOv8  
- Android Jetpack Compose & TTS APIs  
- Instruction Manual for Filipino Braille Transcription  
- Roboflow and Canva  
- Thesis panel, advisors, and all testers

---

## 🐛 Issues & Feedback

Found a bug or have suggestions?  
Please submit an issue here → [GitHub Issues](https://github.com/aemiio/braille-lens-kotlin/issues)

---


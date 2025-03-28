
# TeamLounge — A Teamwork Management App

**TeamLounge** is an Android application designed for managing teams, tasks, and communication efficiently. It was developed as part of a Mobile Application Development course lab (Lab 5), featuring persistent storage using **Google Firestore** and **Firebase Authentication**.

---

## 📲 Features

- 🔐 **Login**: Secure login and user authentication with Firebase.
- 👥 **Team Management**:
  - View team list
  - Create new teams
  - Modify existing teams
  - View team details
- 💬 **Chat**: Built-in team chat feature.
- 📋 **Task Management**:
  - Task list and detailed task view
  - Add, edit, and delete tasks
- 🧑 **User Profile**:
  - View and edit your profile

---

## 🛠 Requirements

- Android SDK
- Java/Kotlin
- Gradle (wrapper included)
- Firebase project (Firestore and Authentication enabled)

---

## 🚀 Getting Started

1. **Clone the repository**
```bash
git clone https://github.com/yourusername/teamlounge.git
cd teamlounge
```

2. **Build the project**
```bash
./gradlew build       # Linux/macOS
gradlew.bat build     # Windows
```

3. **Run the app**
- Open in Android Studio **or**
- Install generated APK to a device or emulator

> Ensure you have your `google-services.json` file properly configured in `app/`.

---

## 📁 Project Structure

```
teamlounge/
├── app/
│   ├── src/                  # App source code
│   ├── build.gradle.kts
│   └── google-services.json  # Firebase configuration
├── gradle/                   # Gradle wrapper
├── build.gradle.kts
├── settings.gradle.kts
├── gradle.properties
├── gradlew / gradlew.bat     # Gradle scripts
└── .gitignore
```

---

## 👨‍💻 Authors

Developed by:
- Almeida João
- Bor Marco
- Conti Giampaolo
- Serafini Stefano

---

## 🔐 Note

**Do not commit** your `keystore.jks` or `local.properties` to version control — they may contain sensitive or machine-specific data.

---

Also in the repository I have included the pdf showing some pages of the App, so you can see it without having to download it!

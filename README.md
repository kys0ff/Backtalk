# Backtalk 🗨️

![Kotlin](https://img.shields.io/badge/Kotlin-100%25-7F52FF?logo=kotlin)
![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-UI-4285F4?logo=android)
![Platform](https://img.shields.io/badge/Platform-Android-brightgreen)
![Min SDK](https://img.shields.io/badge/Min%20SDK-24-green)
![License](https://img.shields.io/badge/License-MIT-blue)
![Architecture](https://img.shields.io/badge/Architecture-MVVM-orange)
![UI](https://img.shields.io/badge/UI-Declarative-blueviolet)
![Version](https://img.shields.io/badge/Version-0.1.6-informational)
![Maintained](https://img.shields.io/badge/Maintained-Yes-2ea44f)
[![Liberapay](https://img.shields.io/badge/Liberapay-Support%20me-F6C915)](https://liberapay.com/kys0ff)

**Backtalk** is a private, self-chat notes app built with **Kotlin** and **Jetpack Compose**.  
It lets you talk to yourself — reply, reflect, organize thoughts — all in a clean chat-style interface with strong privacy features.

> *Backtalk — because sometimes the most important conversation is the one where you talk back to yourself.*

---

## ✨ Features

- 📝 **Self-chat notes** — write messages and reply to yourself
- 💬 **Chat-style UI** built with Jetpack Compose
- 🧵 **Reply-based conversations** (message + your responses)
- 📋 **Copy messages**
- 🗑️ **Delete messages**
- 🔒 **Biometric lock** (fingerprint / face unlock)
- ⏱️ **Auto-lock after timeout**
- 💾 **Offline-first** with Room database
- 🧩 **Clean architecture** using Koin for dependency injection
- 📱 **Modern Android UI** with Material 3
- 🔄 **Backup & Restore** — manual and automatic periodic backups with encryption
- 📜 **Open Source Licenses** — view project dependencies and licenses
- 👈 **Swipe actions** — swipe to edit or reply to messages

## 🚀 Recent Changes (v0.1.6)

- ⚙️ **Settings Redesign** — cleaner, sectioned layout for better navigation.
- 🔐 **Secure Auto-Export** — encryption support for background backups.
- 📜 **Licenses Screen** — dedicated view for open source attributions.
- 🛡️ **Improved Privacy** — removed internet permission from F-Droid flavor.
- 🎨 **UX Refinement** — updated dialogs and theme selection flow.

See the full [CHANGELOG.md](./CHANGELOG.md) for more details.

## 🚀 Planned Features

- 🔍 Message **filtering & search**
- 🧵 **Threaded view** (original message in the middle, replies shown above/below)
- 🖼️ **Image messages**
- 🎙️ **Voice messages**
- ☁️ ~~**Optional encrypted backup**~~

---

## 🛠 Tech Stack

- **Language:** Kotlin
- **UI:** Jetpack Compose
- **Database:** Room
- **Dependency Injection:** Koin
- **Architecture:** MVVM
- **Biometrics:** AndroidX Biometric
- **Min SDK:** 24 (Android 7.0)

---

## 📦 Download

👉 **[Download the latest release](../../releases/latest)**

---

## 🔐 Privacy

Backtalk is designed to be **private by default**:

- 🏠 **Local Storage:** All data is stored locally on your device.
- 🚫 **No Analytics:** No tracking or usage data is collected.
- 📴 **No Internet:** No network access is required to use the app.
- 🛡️ **Secure:** Protected by biometric authentication and auto-lock.

---

## 🧑‍💻 Building the Project

1. **Clone the repository:**

   ```bash
   git clone https://github.com/kys0ff/Backtalk.git
   cd Backtalk

2. **Open in Android Studio:** Open the project in Android Studio (Hedgehog or newer recommended).

3. **Run:** Sync Gradle and run the app on a device or emulator.

---

## 💖 Support Development

If Backtalk helps you, your support makes ongoing development possible.

- 💸 **Liberapay**: https://liberapay.com/kys0ff  
  (one-time or recurring)

Thanks for supporting open source 💖

## 📄 License

Backtalk is licensed under the **MIT License**.

Copyright © 2026 **kys0ff**

See the [LICENSE](./LICENSE) file for full license details.

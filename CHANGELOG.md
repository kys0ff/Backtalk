# Changelog

All notable changes to this project will be documented in this file.

## [0.1.6] - 2026-05-01

### Added
- **Open Source Licenses**: New screen to display project dependencies and their licenses.

### Changed
- **Settings Redesign**: Completely overhauled the Settings screen with a sectioned layout, custom components, and better UX.
- **License UI**: Redesigned the License screen and externalized library metadata for better maintainability.
- **Dialogs Refactoring**: Refactored settings dialogs to include confirmation buttons and externalized all strings.
- **Theme Selection**: Integrated theme selection directly into the settings flow.

### Refactored
- Added an initial delay to `AutoExportWorker` to avoid immediate execution on app start.
- Cleaned up and removed unused UI components from the Settings screen.

### Build
- **F-Droid Privacy**: Removed `INTERNET` permission from the f-droid build variant.
- **Maintenance**: Bumped `versionCode` to 11.

---

## [0.1.5] - 2026-04-28

### Added
- **Backup & Restore**: Comprehensive backup and restore functionality, including manual exports and imports with password protection.
- **Auto-Export**: Periodic automatic backups in the background using WorkManager.
- **Swipe Gestures**: Implemented swipe-to-edit and bidirectional swipe gestures for message items.
- **SmartText**: New component for enhanced text rendering in messages with `maxLines` support.
- **F-Droid Flavor**: Added a dedicated F-Droid build flavor with update checking disabled.
- **Custom Theming**: Custom Material 3 color schemes and a comprehensive typography scale.
- **KDoc Documentation**: Added extensive KDoc to domain use cases, preference constants, and Koin modules.
- **Manual Update Check**: Added settings to manually check for updates (non-F-Droid builds).

### Changed
- **UI Improvements**:
    - Backup progress indicator moved to the top bar in Settings.
    - Backup and restore dialogs refactored for better UX, including visibility toggles for passwords.
    - Updated authentication locked title string.
- **Refactoring**:
    - Standardized and reorganized string resource naming.
    - Renamed `lockOnCreateEnabled` to `isAuthRequired` for clarity.
    - Extracted `SettingsUiEvent` handling in `SettingsViewModel`.
    - Cleaned up WorkManager imports and used KTX `toUri`.
- **Build & Maintenance**:
    - Bumped `versionCode` to 10.
    - Removed custom sourceSet for GitHub variant.
    - Disabled dependency information in APK and Bundle for privacy.

### Fixed
- Cleaned up document and URI handling when cancelling export dialogs.
- Sorted copied messages by timestamp in domain layer.

### Removed
- Unused UI and state components.
- `foojay-resolver-convention` plugin from build scripts.

---

## [0.1.4] - 2026-04-28

### Changed
- Downgraded version name to 0.1.4 from 0.1.5 in a previous build correction.
- Added Android Fastlane metadata and assets.

---

## [0.1.3] - 2026-04-20

### Added
- **Message Editing**: Users can now edit their previous messages.
- **Swipe-to-Reply**: Enhanced `SwipeToReplyWrapper` with directional support and dynamic animations.
- **Message Context Menu**: Long-press on messages to access more options.
- **Back Navigation**: Added back navigation support in Settings.

### Changed
- **Settings UI**: Significant refactoring and styling updates for the Settings screen.
- **Dependency Updates**: Updated core project dependencies and Gradle configurations.

---

## [0.1.1] - 2026-04-10

### Added
- **Preferences Module**: Introduced a dedicated module for app settings with a custom DSL.
- **Security Features**: Added biometric app lock, secure screen (prevent screenshots), and auto-lock timeout.
- **Theming**: Added support for dynamic colors and theme switching.
- **In-App Updates**: Integrated GitHub-based update checking and dialogs.

### Changed
- **Refactoring**: Restructured preferences module packages and introduced `mainUiState`.

---

## [0.1.0] - 2026-04-01

### Added
- **Initial Release**: Core self-chat functionality with a clean Material 3 interface.
- **Local Storage**: Offline-first experience using Room database.
- **Architecture**: MVVM architecture with Koin for dependency injection.
- **Reply System**: Ability to reply to messages and create threads.
- **Privacy**: Local-only storage with no analytics or internet requirements.
- **MIT License**: Project officially licensed under MIT.

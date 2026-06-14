# Changelog

All notable changes to this project will be documented in this file.

## [0.3.5] - 2026-06-14

### Added
- **External Sharing**: Support for receiving shared images and text from external apps with a preview and caption dialog.
- **Rich Media**:
    - Added GIF support to messages and media picker.
    - New shared media sheet with tabs for media, voice notes, and links.
    - Rich link preview component with metadata scraping and caching.
- **Localization**: Added full Spanish localization and expanded Arabic translations for troubleshooting and media.
- **Voice Messages**:
    - Real-time elapsed time display and improved recording indicator.
    - RTL support for voice message waveform rendering and recording gestures.
- **Settings & Maintenance**:
    - Added "Clear cache" option and cache size display in troubleshooting settings.
    - Background update checks and optimized WorkManager scheduling.
    - New toggle for link previews and hashtag bar visibility.
- **UX Improvements**:
    - Tooltips for send and voice record buttons in the input bar.
    - Enhanced message bubble selection visuals and animated background transitions.
    - Support for restricted message captions via `CaptionWordsRegistry`.

### Changed
- **Input Architecture**: Migrated InputBar to `TextFieldState` for better rich content support and performance.
- **UI Refinement**: Updated icons for attachments/links and refined the media selection overlay styling.
- **Background Work**: Centralized WorkManager scheduling in `WorkScheduler` for more reliable reminders and updates.

### Fixed
- **Message Scheduling**: Resolved "today" scheduling mismatches and improved timezone handling.
- **Voice Playback**: Fixed voice message playback issues and migrated notes to persistent storage.
- **Authentication**: Updated app lock logic to be lifecycle-aware, preventing premature lockouts.
- **Stability**: Resolved startup crashes related to update checks and refined reminder scheduling logic.

### Refactored
- **Modularization**: Decoupled InputBar components and moved custom Gradle tasks to `buildSrc`.
- **UI Hierarchy**: Cleaned up `SettingsItem` and `TagFilterBar` layouts for better RTL consistency and overflow handling.

### Build
- **Automation**: Refactored changelog generation to use version tags and dynamically fetch git tags.
- **Maintenance**: Bumped version to 0.3.5.

---

## [0.3.2] - 2026-06-04

### Added
- **Changelog Screen**: Dedicated screen to track app updates with a detailed version history and automatic tracking.
- **Security & Privacy**:
  - New "Lock on screen off" setting and customizable lock timeouts.
  - Biometric verification for sensitive actions (e.g., exports, security settings).
  - Time-limited deletion for messages to enhance privacy.
- **Enhanced Statistics**: Added an app usage heatmap and activity streaks for better insights.
- **Media Features**:
  - Support for multi-image selection and deletion.
  - Added caption support for media messages and SVG support.
  - Implemented a staggered image grid for better media layout in chat.
  - Immersive mode and smooth animations for the image preview screen.
- **Reminders**: Introduced journaling reminders with customizable intervals notifications.
- **UX Improvements**:
  - "Send with Enter" preference and keyboard search action support.
  - Per-app language selection (Android 13+) and customizable date/time formats.
  - Double-click gesture to quickly toggle message pinning.
  - Scroll-to-top functionality in the Changelog screen.
- **Maintenance**: Custom bug reporting screen and global crash handler for improved stability.

### Changed
- **Statistics Redesign**: Overhauled the Statistics screen with Material 3 `LargeTopAppBar` and improved layouts.
- **Pinned Messages UI**: Redesigned the vertical pinned indicator with scrolling and dynamic scaling for better navigation.
- **Settings Architecture**: Migrated to a structured `PreferenceItem` system and property delegates for better maintainability.
- **Backup & Restore**: Backups now include images, ensuring a complete restoration of your chat history.
- **Localization**: Comprehensive updates to Arabic translations and localized theme/settings strings.

### Fixed
- **Authentication**: Refined biometric flow and improved splash screen behavior.
- **App Updates**: Fixed update checking issues caused by ProGuard minification and redesigned the update dialog.
- **Animations**: Improved search scrolling and message blink animations.
- **Onboarding**: Fixed various logic and navigation issues in the onboarding flow.

### Refactored
- **UI Architecture**: Decoupled message components to improve Preview support and extracted stateless screen contents.
- **Background Work**: Moved usage tracking to background workers for better performance.
- **RTL Support**: Added proper logical directions to swipe gestures for better RTL layout consistency.

### Build
- **Automation**: Integrated changelog generation directly into the build process using AGP variant APIs.
- **Maintenance**: Bumped version to 0.3.2.

---

## [0.2.5] - 2026-05-22

### Added
- **Media Attachments**: Support for attaching images from gallery or capturing directly via a new full-screen camera with flash and lens switching.
- **Message Pinning**: Ability to pin important messages with a dedicated pinning bar for quick navigation.
- **Reminders**: New management screen for creating and tracking message-based reminders.
- **Hashtags**: Support for clickable hashtags and filtering messages by tags.
- **Media Statistics**: Enhanced statistics screen with media counts and dynamic pie chart slices.
- **Animations**: Added expressive scale animations to the send button and smooth transitions for attachment controls.
- **Quoted Threads**: Support for quoted threads in both the main threads and detail views.

### Changed
- **Improved Scrolling**: Implemented state-driven auto-scroll and directional visibility logic for the "scroll to bottom" button.
- **Input Bar**: Optimized the input bar layout and integration with the message list.
- **Onboarding**: Integrated camera and media permissions into the onboarding flow.

### Fixed
- **State Sync**: Improved preference state synchronization and parsing safety.
- **Message Actions**: Fixed issues with copying edited messages and handling voice message previews in replies.
- **Thread UI**: Added fallback logic for thread reconstruction and improved message grouping consistency.

### Refactored
- **Architecture**: Decoupled biometric authentication into a standalone manager with Compose support.
- **Modernization**: Transitioned message scheduling and filtering logic to stage-based state management in ViewModels.
- **Localization**: Updated and expanded Arabic translations for Camera, Statistics, and Reminders.

### Build
- **Dependencies**: Bumped various library versions and updated `LibraryProvider.kt`.
- **Maintenance**: Incremented version to 0.2.5.

---

## [0.1.8] - 2026-05-11

### Added
- **Voice Messages**: Full support for recording and playback with real-time waveform visualization.
- **Message Search**: Powerful search functionality with result navigation and text highlighting.
- **Device Synchronization**: Multi-device sync support with automated discovery and re-pairing.
- **Markdown & Formatting**: Rich text support with Markdown and a dedicated message formatting toolbar.
- **SmartText Enhancements**: Added support for interactive links, @mentions, and annotation handling.
- **Thread View Mode**: Browse your messages organized as interactive threads for a cleaner overview.
- **Onboarding Flow**: Comprehensive new user onboarding experience with parallax effects.
- **Statistics Screen**: Detailed insights into chat habits with animated charts and tooltips.
- **Message Scheduling**: Ability to schedule messages and set reminders.
- **Arabic Localization**: Full support for Arabic language and RTL layouts.
- **Developer Options**: New hidden menu for debugging, testing, and data management.
- **UI Improvements**: Added "scroll to bottom" FAB and pull-to-refresh in threads.

### Changed
- **Backup Format**: Introduced new `.bkt` ZIP-based format to support media backups while maintaining backward compatibility with `.json`.
- **Delete Confirmation**: Enhanced dialogs with icons and plural support for multiple message deletion.
- **App Startup**: Integrated official SplashScreen API and optimized startup logic.
- **Settings & Sync UI**: Modularized and redesigned UI components for better maintainability and UX.

### Refactored
- **Modularization**: Extracted `MainView`, `AppLifecycleHandler`, and onboarding components to simplify `MainActivity`.
- **Reactive Preferences**: Implemented reactive preference states for more efficient UI updates.
- **Thread Logic**: Optimized thread grouping and retrieval logic with nested thread support.
- **Security**: Centralized authentication state handling and improved security check robustness.

### Build
- **Android Support**: Enabled JDK desugaring to support Android 6 (API 23).
- **Maintenance**: Bumped AGP to 9.2.1 and updated various dependencies (Compose BOM, Mockk, etc.).
- **Version**: Bumped `versionCode` to 18.

---

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

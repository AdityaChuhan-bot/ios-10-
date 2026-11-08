# iPhone 6 iOS 10 Launcher for Android

A native, high-performance Android launcher recreating the authentic visual hierarchy, tactile motion, and system interactions of an Apple iPhone 6 running iOS 10. Built to run fluidly on low-memory Android devices (including Android 11 / Moto G4 Play with 2 GB RAM) with zero internet dependency and strictly local persistence.

---

### User Review & Critical Decisions

> [!IMPORTANT]
> The launcher is structured into modular stages adhering strictly to the user prompt. Based on the Phase 1 questionnaire:
> - **Visual Theme**: Bundles the iconic iOS 10 ocean wave wallpaper with authentic iOS-proportioned squircles, typography, and frosted glass dock styling.
> - **Icon Pack System**: System defaults (Phone, Messages, Safari/Browser, Camera, Photos, Settings, Music, Mail, Clock, Calendar, Notes, Calculator) feature authentic iOS 10 vector/canvas styling, while installed Android applications are dynamically framed with iOS squircles.
> - **Execution Strategy**: The complete modular architecture will be established immediately with functional Stage 1 (Home Screen Grid, Translucent Dock, Wallpaper, Launcher Intent Registration) and Stage 2 (Folders, Jiggle Mode Drag-and-Drop, Horizontal Paging) active, followed by the integrated iOS 10 Status Bar, Control Center, Notification Center, AssistiveTouch, Spotlight Search, and Task Switcher.

---

### 1. Overview & Core Concept

- **What It Does**: Replaces the standard Android home screen with a pixel-accurate iOS 10 experience. It dynamically loads all installed Android applications, arranges them across a 4-column grid with authentic iconography and page indicator dots, provides a frosted 4-slot dock, and offers iOS 10 system overlays (Notification Center, Control Center, Spotlight search, AssistiveTouch, and App Switcher).
- **Target Audience & Devices**: Users desiring the nostalgic, elegant simplicity of iOS 10 on Android, optimized specifically for low-RAM (2GB) devices like Moto G4 Play running Android 11 (LineageOS 18.1), without background telemetry, ads, or heavy web-view dependencies.
- **Key Value**: 100% native Android execution, zero mock data, genuine system integration (live battery and network, real app launching, genuine volume/brightness and system settings intents, optional NotificationListenerService), with sub-millisecond local touch response.

---

### 2. User Experience & Visual Design

#### Key User Flows
1. **Home Screen Navigation**: Swipe horizontally between home screen pages with spring-damped page indicators. Tap any app icon to launch the real Android activity.
2. **Jiggle / Edit Mode**: Long-press any app icon or empty home area to enter iOS 10 edit mode (subtle icon oscillation, 'x' delete/remove badge, drag & drop to rearrange or hover over another icon to automatically create and expand an iOS 10 folder).
3. **Spotlight Search**: Swipe down on any home screen page to transition the screen into a frosted blur with an iOS 10 search bar, recent app suggestions, and instant keystroke filtering.
4. **Control Center**: Swipe up from the bottom edge (or tap AssistiveTouch) to slide up the iOS 10 dual-tone Control Center with genuine toggles for Wi-Fi, Bluetooth, Airplane mode, Flashlight, Brightness, Volume, and Music controls.
5. **Notification Center**: Swipe down from the top edge to view iOS 10 style notification banners grouped by app with timestamps, clear-all action, and dismiss gestures.
6. **AssistiveTouch**: An optional movable floating button that stays docked to screen edges. Tapping reveals a radial menu providing instant access to Home, Control Center, Notifications, Recent Apps, and Device controls.
7. **App Switcher**: Double-press home or select Recent Apps from AssistiveTouch to view the horizontal card stack of recent activities.

#### Visual Identity & Theme Tokens
- **Iconic Wallpaper**: High-resolution embedded iOS 10 ocean wave breaking onto turquoise water and white sand.
- **Dock & Card Blur**: Translucent frosted blur effect (`#80FFFFFF` / `#B3EFEFEF` with layered alpha compositing for low-overhead rendering on older GPUs).
- **Icon Squircles**: Authentic continuous curvature rounded rect (`cornerRadius = 22.5%` of icon dimension) with subtle 1px border highlights.
- **Typography**: Clean Apple San Francisco-inspired clean sans-serif type hierarchy (`12sp` app labels, `17sp` modal headers, `14sp` subtext).
- **Color Palette**:
  - Dock / Card Frosted Surface: `rgba(255, 255, 255, 0.72)`
  - Active Blue: `#007AFF`
  - Active Green: `#4CD964`
  - Status & Icon Shadows: `rgba(0, 0, 0, 0.35)`
  - Text Primary: `#000000` (on cards) and `#FFFFFF` with drop shadow (on wallpaper)

---

### 3. Key Product Decisions & Trade-Offs

1. **Native Jetpack Compose UI vs. Legacy XML Layouts**:
   - *Chosen Approach*: Modern Jetpack Compose for high-speed dynamic state management (drag-and-drop animations, folder expansions, fluid gestural sheets for Control Center and Spotlight) while maintaining Android 11+ compatibility and lightweight footprint.
   - *Why*: Compose allows zero-reflection, GPU-accelerated drawing without heavy fragment transactions, significantly reducing memory consumption on 2GB RAM devices.
2. **Real System Controls vs. Platform Sandbox**:
   - *Chosen Approach*: Real system APIs are called directly where Android 11 permissions permit (Audio Manager for volume, Settings System for brightness with write settings permission flow, Camera2 flash for flashlight). Where Android 11 restricts background toggles (Airplane mode, secure Bluetooth/Wi-Fi toggles), the launcher seamlessly opens the exact native Android settings panel (`Settings.ACTION_AIRPLANE_MODE_SETTINGS`, `Settings.Panel.ACTION_WIFI`).
   - *Why*: Complies with Android security guidelines while maintaining 100% functional utility without dead-end buttons.
3. **Local State Persistence**:
   - *Chosen Approach*: Lightweight JSON file in private app storage (`filesDir/launcher_layout.json`) storing page indices, folder groupings, and dock slots.
   - *Why*: Instant startup without SQLite database overhead or external daemon services.

---

### 4. Technical Architecture & Data Strategy

```
┌────────────────────────────────────────────────────────────────────────┐
│                        MainActivity (HOME Launcher)                    │
│   • Intent: android.intent.action.MAIN / category.HOME / DEFAULT       │
│   • Edge-to-Edge display with custom iOS 10 Status Bar                 │
└───────────────────────────────────┬────────────────────────────────────┘
                                    │
       ┌────────────────────────────┼────────────────────────────┐
       ▼                            ▼                            ▼
┌──────────────────┐      ┌──────────────────┐       ┌──────────────────┐
│ iOS 10 Status Bar│      │ Notification Ctr │       │ Control Center   │
│ • Live Time      │      │ • Swipe down top │       │ • Swipe up btm   │
│ • Battery %/Icon │      │ • ListenerService│       │ • Real sliders   │
│ • Wi-Fi / Cell   │      │ • Clear / Dismiss│       │ • Real shortcuts │
└──────────────────┘      └──────────────────┘       └──────────────────┘
       │                            │                            │
       └────────────────────────────┼────────────────────────────┘
                                    ▼
┌────────────────────────────────────────────────────────────────────────┐
│                    HomeScreenContent (Paging Grid)                     │
│  • Wallpaper Background (iOS 10 Ocean Wave)                            │
│  • Horizontal Pager (4x6 Grid: 24 items per page)                      │
│  • App Icon Squircles & Dynamic Android App Resolver                   │
│  • Folder Dialog & Expanding Folder Grid                               │
│  • Jiggle / Edit Mode Drag & Drop Rearrangement                        │
│  • Frosted Translucent 4-App Dock at Bottom                            │
│  • Page Indicator Dots                                                 │
└───────────────────────────────────┬────────────────────────────────────┘
                                    │
       ┌────────────────────────────┴────────────────────────────┐
       ▼                                                         ▼
┌───────────────────────────┐               ┌───────────────────────────┐
│     Spotlight Search      │               │       AssistiveTouch      │
│ • Swipe-down gesture      │               │ • Floating movable bubble │
│ • Instant app filter      │               │ • Radial iOS 10 menu      │
│ • Recent app history      │               │ • Home, Control, Recents  │
└───────────────────────────┘               └───────────────────────────┘
```

#### Core Components & Responsibilities
- `LauncherViewModel`: Manages installed apps list from `PackageManager`, persists layout arrangements, handles drag-and-drop state, folder creation/renaming, and edit mode toggle.
- `SystemStatusMonitor`: Broadcast receiver and listeners for real battery percentage, charging state, Wi-Fi connectivity, and signal strength.
- `NotificationListener`: Optional Android `NotificationListenerService` that streams active notification status bar notices to the iOS 10 Notification Center sheet.
- `ControlCenterController`: Interacts with `AudioManager`, `CameraManager` (Torch), `Settings.System`, and system intent panels.
- `AssistiveTouchOverlay`: Floating draggable view providing the classic iOS AssistiveTouch experience.

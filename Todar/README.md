# 🎯 Todar - Your Personal Habit Tracker

  **Build lasting habits, one day at a time**
  
  [![Expo](https://img.shields.io/badge/Expo-000020?style=for-the-badge&logo=expo&logoColor=white)](https://expo.dev)
  [![React Native](https://img.shields.io/badge/React_Native-20232A?style=for-the-badge&logo=react&logoColor=61DAFB)](https://reactnative.dev)
  [![TypeScript](https://img.shields.io/badge/TypeScript-007ACC?style=for-the-badge&logo=typescript&logoColor=white)](https://www.typescriptlang.org)

---

## ✨ Features

### 🏃‍♂️ **Habit Management**
- **Create Custom Habits** - Build personalized routines with custom names and icons
- **Visual Progress Tracking** - See your streaks and progress at a glance
- **Smart Notifications** - Get reminded at the perfect time for each habit
- **Habit Templates** - Quick-start with pre-built popular habits

### 📊 **Progress Analytics**
- **Streak Tracking** - Monitor your consistency with visual streak counters
- **Daily Completion** - Mark habits as done, skipped, or pending
- **Statistics Dashboard** - View total habits, combined streaks, and best performance
- **Progress History** - Track your journey over time

### 🔔 **Smart Notifications**
- **Customizable Reminders** - Set multiple notification times per habit
- **Intelligent Scheduling** - Never miss a habit with smart notification management
- **Sound & Visual Alerts** - Custom notification sounds and visual indicators

### 🎨 **Beautiful Design**
- **Modern UI** - Clean, intuitive interface with smooth animations
- **Dark/Light Theme** - Switch between themes for comfortable viewing
- **Responsive Design** - Optimized for all screen sizes
- **Accessibility** - Built with accessibility in mind

### 📱 **Cross-Platform**
- **iOS & Android** - Native performance on both platforms
- **Web Support** - Access your habits from any browser
- **Offline First** - Works without internet connection
- **Data Persistence** - Your progress is always saved locally

---

## 🚀 Quick Start

### Prerequisites
- Node.js (v18 or higher)
- npm or yarn
- Expo CLI
- iOS Simulator (for iOS development)
- Android Studio (for Android development)

### Installation

1. **Clone the repository**
   ```bash
   git clone https://github.com/aryan2621/Todar.git
   cd Todar
   ```

2. **Install dependencies**
   ```bash
   npm install
   ```

3. **Start the development server**
   ```bash
   npx expo start
   ```

4. **Run on your preferred platform**
   ```bash
   # iOS Simulator
   npx expo run:ios
   
   # Android Emulator
   npx expo run:android
   
   # Web Browser
   npx expo start --web
   ```

---

## 📱 App Structure

```
📁 Todar/
├── 📁 app/                    # Main application screens
│   ├── 🏠 home.tsx           # Dashboard with habit overview
│   ├── ➕ create-habit.tsx    # Create new habits
│   ├── ⚙️ settings.tsx       # App settings and preferences
│   ├── 🔔 configure-notifications.tsx # Notification setup
│   └── ✅ task-completion.tsx # Mark habits as complete
├── 📁 components/             # Reusable UI components
│   ├── 🎴 HabitCard.tsx      # Individual habit display
│   ├── 🧭 BottomNavigation.tsx # Navigation component
│   ├── ⏰ TimePickerModal.tsx # Time selection modal
│   └── 💬 CustomDialog.tsx   # Custom dialog component
├── 📁 contexts/              # React contexts
│   └── 🎨 ThemeContext.tsx   # Theme management
├── 📁 utils/                 # Utility functions
│   ├── 💾 storage.ts         # Data persistence
│   ├── 🔔 notifications.ts   # Notification management
│   ├── 📈 streaks.ts         # Streak calculations
│   └── ⏰ timeFormat.ts      # Time formatting utilities
└── 📁 types/                 # TypeScript type definitions
    └── 📝 index.ts           # App-wide type definitions
```

---

## 🛠️ Technology Stack

### **Frontend Framework**
- **React Native** - Cross-platform mobile development
- **Expo** - Development platform and toolchain
- **TypeScript** - Type-safe JavaScript development

### **Navigation & Routing**
- **Expo Router** - File-based routing system
- **React Navigation** - Navigation library for React Native

### **State Management**
- **React Context** - Global state management
- **AsyncStorage** - Local data persistence

### **Notifications**
- **Expo Notifications** - Push notification system
- **Expo Haptics** - Tactile feedback

### **UI/UX**
- **Custom Components** - Tailored UI components
- **Animated API** - Smooth animations and transitions
- **Responsive Design** - Adaptive layouts

---

## 🎯 Core Features Deep Dive

### **Habit Creation**
- **Custom Naming** - Personalize your habits with descriptive names
- **Icon Selection** - Choose from 16+ emoji icons for visual identification
- **Time Scheduling** - Set multiple notification times per habit
- **Validation** - Smart form validation with helpful error messages

### **Progress Tracking**
- **Streak Counter** - Visual representation of consecutive days
- **Completion Status** - Mark habits as done, skipped, or pending
- **Statistics** - View total habits, combined streaks, and best performance
- **History** - Track your progress over time

### **Notifications**
- **Smart Scheduling** - Intelligent notification management
- **Custom Sounds** - Personalized notification audio
- **Multiple Times** - Set different reminder times for each habit
- **Background Sync** - Notifications work even when app is closed

### **Data Management**
- **Local Storage** - All data stored securely on device
- **Offline Support** - Full functionality without internet
- **Data Export** - Easy data backup and restore
- **Privacy First** - No data sent to external servers

---

## 🎨 Customization

### **Themes**
- **Light Theme** - Clean, bright interface for daytime use
- **Dark Theme** - Easy on the eyes for nighttime use
- **Auto Switch** - Automatically adapt to system preferences

### **Habit Templates**
Pre-built templates for common habits:
- 🏋️ **Gym** - Fitness and exercise routines
- 📚 **Learning** - Educational and skill development
- 💧 **Drink Water** - Hydration reminders
- 🧘 **Meditate** - Mindfulness and meditation
- 📖 **Read** - Reading and literature
- ✍️ **Journal** - Daily reflection and writing

---

## 📊 Data & Privacy

### **Data Storage**
- **Local First** - All data stored on your device
- **No Cloud Sync** - Your data stays private
- **Secure Storage** - Encrypted local storage
- **Backup Options** - Export/import functionality

### **Privacy Policy**
- **No Tracking** - We don't track your usage
- **No Ads** - Clean, ad-free experience
- **No Data Collection** - Your habits are yours alone
- **Open Source** - Transparent codebase

---

## 🚀 Deployment

### **Development Build**
```bash
# Create development build
npx expo build:android
npx expo build:ios
```

### **Production Build**
```bash
# Build for production
npx expo build:android --type apk
npx expo build:ios --type archive
```

### **App Store Deployment**
- **Google Play Store** - Android app distribution
- **Apple App Store** - iOS app distribution
- **Web Deployment** - PWA for web browsers

---

## 🤝 Contributing

We welcome contributions! Here's how you can help:

### **Getting Started**
1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

### **Development Guidelines**
- Follow TypeScript best practices
- Write meaningful commit messages
- Add tests for new features
- Update documentation as needed
- Follow the existing code style

### **Areas for Contribution**
- 🐛 **Bug Fixes** - Help us squash bugs
- ✨ **New Features** - Add exciting new functionality
- 📚 **Documentation** - Improve our docs
- 🎨 **UI/UX** - Enhance the user experience
- 🧪 **Testing** - Add comprehensive tests

---

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---

## 🙏 Acknowledgments

- **Expo Team** - For the amazing development platform
- **React Native Community** - For the robust mobile framework
- **Open Source Contributors** - For the libraries that make this possible
- **Beta Testers** - For valuable feedback and suggestions

---

## 📞 Support

### **Getting Help**
- 📖 **Documentation** - Check our comprehensive docs
- 🐛 **Bug Reports** - Report issues on [GitHub](https://github.com/aryan2621/Todar/issues)
- 💡 **Feature Requests** - Suggest new features via [GitHub Issues](https://github.com/aryan2621/Todar/issues)
- 💬 **Community** - Join discussions on [GitHub Discussions](https://github.com/aryan2621/Todar/discussions)

### **Contact**
- **GitHub** - [@aryan2621](https://github.com/aryan2621)
- **Repository** - [Todar](https://github.com/aryan2621/Todar)

---

<div align="center">
  <p><strong>Made with ❤️ by the Todar Team</strong></p>
  <p>Start building better habits today!</p>
</div>
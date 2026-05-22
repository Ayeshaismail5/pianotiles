# Tile Tap: A High-Performance Rhythm Arcade Game

**Tile Tap** is a fast-paced, reflex-based arcade game for Android. Originally based on a basic prototype by Atilla Türkmen, this project has been significantly overhauled and modernized into a polished, feature-rich gaming experience.

## 🚀 Key Enhancements & My Contributions
This version of the game features major technical and visual upgrades over the original source:

*   **Space-Neon UI Overhaul:** Transformed the basic UI into a futuristic aesthetic using custom **LinearGradient Shaders** for a glowing tile effect.
*   **Custom Canvas Engine:** Optimized the rendering logic to achieve a flawless **90 FPS**, ensuring buttery-smooth gameplay even at high speeds.
*   **Dynamic HUD:** Implemented an intelligent Heads-Up Display with a glowing **Progress Bar** that tracks real-time milestones (100, 200, 300+).
*   **Smart Pause System:** Added a **BroadcastReceiver** that automatically pauses the game during system interrupts (Screen Off or Power Disconnected).
*   **Advanced Game Over Analytics:** Redesigned the results screen with dynamic, emoji-based appreciation messages based on player performance.
*   **Modern Architecture:** Fully migrated and optimized the codebase using **Kotlin**, Fragments, and the Jetpack Navigation Component.

## 🛠️ Technical Features
*   **Multi-Threading:** Utilizes a dedicated `GameThread` for high-frequency rendering independent of the UI thread.
*   **Data Persistence:** Uses `SharedPreferences` to store and manage high scores for different difficulty levels.
*   **Haptic Feedback:** Leverages the Android Vibrator service to provide tactile sensory feedback on every successful tap.
*   **Screen Scaling:** Implemented `DisplayMetrics` logic to normalize gameplay speed across different device resolutions.
*   **Explicit Intents:** Robust data passing between the Main Menu and the Game Engine for user preferences.

## 🎮 Gameplay
The goal is simple: tap the scrolling tiles as they move down the screen.
*   Don't miss a tile!
*   Don't tap the wrong area!
*   Difficulty increases progressively as your score climbs.

## 🛠️ Built With
*   **Language:** Kotlin & Java
*   **Graphics:** Android Canvas API
*   **Navigation:** Android Jetpack Navigation
*   **Storage:** SharedPreferences

## 🤝 Contributing
Contributions are welcome! Feel free to fork the repository and submit a pull request.

## 📄 License
This project is distributed under the GNU General Public License. See the `LICENSE` file for details.

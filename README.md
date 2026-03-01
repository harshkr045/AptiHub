# AptiHub
An Android application for aptitude test preparation. It allows users to take quizzes on various topics, track their progress with streaks, and manage their profiles.

## ✨ Features
​
 *   **Firebase Authentication:** Secure sign-up and login for users.
 *   **Web Admin Panel:** Includes a companion website for administrators to easily add, update, and delete quiz questions.
 *   **Topic-Based Quizzes:** Choose from a variety of aptitude topics to test your skills.
 *   **Quiz Levels:** Select different difficulty levels for each topic.
 *   **Interactive Q&A:** A user-friendly interface for answering quiz questions.
 *   **Performance Tracking:** View your score and results immediately after each quiz.
 *   **Profile Management:** View and manage your user profile details.
 *   **Streak Calendar:** Keep track of your daily study consistency with an integrated calendar view.
 *   **Engaging Animations:** Utilizes Lottie for a richer, more dynamic user experience.
​
 ## 🛠️ Technologies & Libraries Used
​
 *   **Language:** [Kotlin](https://kotlinlang.org/)
 *   **Architecture:** Standard Android architecture with Activities and ViewBinding.
 *   **Backend & Authentication:** [Firebase](https://firebase.google.com/) (Authentication and Firestore)
 *   **UI Components:**
     *   [Material Components for Android](https://material.io/develop/android)
     *   [ViewBinding](https://developer.android.com/topic/libraries/view-binding)
 *   **Libraries:**
     *   [Lottie for Android](https://airbnb.io/lottie/#/android) - For rich animations.
     *   [Material Calendar View](https://github.com/prolificinteractive/material-calendarview) - To display the user's activity streak.
​
 ## 🚀 Getting Started
​
 To get a local copy up and running, follow these simple steps.
​
 ### Prerequisites
​
 *   Android Studio Hedgehog or later.
 *   A Firebase account.
​
 ### Installation
​
 1.  **Clone the repository:**
     '''bash
     git clone https://github.com/<your-username>/AptiHub.git
     '''
 2.  **Navigate to the project directory:**
     '''bash
     cd AptiHub
     '''
 3.  **Firebase Setup:**
     *   Go to the [Firebase Console](https://console.firebase.google.com/) and create a new project.
     *   Add an Android app to your Firebase project with the package name `com.example.aptihub` (or your specific package name found in `app/build.gradle.kts`).
     *   Download the `google-services.json` file and place it in the `app/` directory of the project.
 4.  **Build and Run:**
     *   Open the project in Android Studio.
     *   Let Gradle sync the dependencies.
     *   Build and run the application on an emulator or a physical device.
​

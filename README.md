# My Android App

Welcome! 
This Android application is built using Java in Android Studio.
The app communicates with the ReqRes API to perform CRUD operations.
Manipulate a user list and save locally to the device.

## Table of Contents

- [Main Features](#main-features)
- [Deeper Details](#deeper-details)
- [Installation](#installation)
- [Screenshots](#screenshots)

## Main Features

- **Create User**: Add new users to the list.
- **Update User**: Modify existing user information.
- **Delete User**: Remove users from the list.
- **Local Storage**: Saves user list locally on the device.
- **Pagination**: Navigate the users through pagination buttons or side swiping.
- **Searchbar**: Filter user data live in a functional searchbar.
- **Device Gallery**: Avatars can be editted and uploaded from device photo gallery.

## Deeper Details

- **API**: All CRUD operations will go through REQRES API and be dependant on the responses, user and devs will be notified of any server errors.
- **Gesture Detector**: Multiple gestures and listeners applied over RecyclerView and its' children, scrolling, on click and on fling.
- **Catlogs**: Most classes include detailed debugging logs with corresponding class names as TAGS.
- **Validations**: New users will go through basic real life scenario validations on names and email that must be unique as well. 

## Installation

Follow these steps to install and run the app:

1. **Clone the repository:**
    ```sh
    git clone https://github.com/ItsAlexanderPopov/android-app.git
    ```
2. **Open the project in Android Studio:**
    - Open Android Studio.
    - Click on `File -> Open`.
    - Navigate to the cloned repository folder and select it.

3. **Build the project:**
    - Click on `Build -> Make Project` or use the `Ctrl+F9` shortcut.

4. **Run the app:**
    - Connect an Android device or start an emulator.
    - Click on `Run -> Run 'app'` or use the `Shift+F10` shortcut.

## Screenshots

<img src="https://github.com/user-attachments/assets/514325fa-1265-4f3e-a59f-b61fe167f687" width="300" />
<img src="https://github.com/user-attachments/assets/9037233d-6995-4f4b-9f0a-0150b3be5a23" width="300" />

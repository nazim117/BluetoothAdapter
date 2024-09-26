# BluetoothAdapter


## Project Overview
The idea of the project is to mimic the Bluetooth feature in a phone. 
It is able to connect to an audio device through Bluetooth and automatically play an audio file after connecting.
Moreover, you are also able to disconnect from that device and the audio file will stop immediately.

The applications target audience is people who want to test if their phone is capable of pairing with another audio device.

## Tech Stack

### Front-end - Jetpack Compose 
The decision was behind the fact that Google (the company behind the creation of the technology)
wants developers to use it for development instead of XML.

### Back-end - Kotlin
- It is used because the developer wants to learn about android development. 
- He believes that android is simpler, more flexible and has a bigger open ecosystem than IOS.
- Moreover, it has a larger user base because of market share.
- It is also a cheaper option because you do not need specific hardware to work with it.
- Android has a bigger job market as well.
- Has a bigger developer community.
- Android performs better than Flutter because it is optimized for the OS.

## Key features
You as a user are able to connect and disconnect to and from other audio devices through Bluetooth. 

## Project structure

### MainActivity class
It serves as the controller receiving requests from the UI and sending the appropriate response back.

### BluetoothManager class
Its purpose is to handle every action related to Bluetooth from initializing it to retrieving all of the available devices.

### PermissionManager class
It is responsible for handling all the permission given and not given by the user for the application to work properly.

### AudioFileManager class
Handles playing and stopping audio.

### DeviceListAdapter
Serves the purpose of displaying the found devices to the user. It is mostly a class responsible for UI/UX.

## Setup instructions

### Step 1: Clone the Project
Clone the project using Git.
```bash
git clone <repository-url>
```

### Step 2: Open the project in Android Studio
Once cloned, you can open the project in Android Studio:
1. Launch Android Studio.
2. Select **File > Open**.
3. Navigate to the folder where the project was cloned and select it.

### Step 3: Install Dependencies
Go to **build.gradle.kts** and click **Sync Now** that is at the top of the screen

### Step 4: Running the project
1. Connect your Android device via USB
2. Click the **Run** button in Android Studio or press **Shift + F10** to build and run the app.
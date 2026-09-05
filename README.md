# AI Mobile Chat App

This is a modern Android application that demonstrates on-device LLM inference using MediaPipe GenAI
and Google AI Edge Local Agents. It features a conversational interface with support for native tool
triggering (skills) like web search and calendar viewing.

The app uses Skill.md to declare AI Tools: `view_calendar` and `web_search`.
SKILLs are placed in `feature/chat/src/main/assets`
`chat/util/SkillLoader.kt` is used to find and parse Skills.

## Getting Started for Developers

Follow these steps to set up the project on your local machine.

### 1. Install Android Studio

Download and install the latest stable version `Android Studio Quail 1`
of [Android Studio](https://developer.android.com/studio) (Ladybug or newer recommended).

### 2. Clone or Unzip the Project

Clone the repository from GitHub or unzip the provided archive into your preferred workspace
directory.

### 3. Configure Local Properties

The project requires a **Hugging Face User Access Token** to download models.

1. Generate a token at [huggingface.co/settings/tokens](https://huggingface.co/settings/tokens) (
   Read access is sufficient).
2. Open (or create) the `local.properties` file in the root directory of the project.
3. Add the following line:
   ```properties
   HUGGING_FACE_TOKEN=your_token_here
   ```

### 4. Open and Sync

1. Launch Android Studio.
2. Select **Open** and navigate to the project root folder.
3. Wait for the Gradle sync to complete.

## Project Structure

The project follows a modular architecture:

### Core Modules (`:core`)

- **`:core:coroutines`**: Base classes and utilities for Kotlin Coroutines and Flow-based UseCases.
- **`:core:database`**: Room database implementation for storing chat history.
- **`:core:di`**: Koin dependency injection configuration and extensions.
- **`:core:filestore`**: Manages file operations in the app's internal and cache directories.
- **`:core:navigation`**: Centralized navigation logic using Jetpack Compose Navigation.
- **`:core:ui`**: Shared UI components, themes (Material 3), and base ViewModel.
- **`:core:util`**: General-purpose utility classes and extensions.

### Feature Modules (`:feature`)

- **`:feature:home`**: The main landing screen.
- **`:feature:chat`**: The conversational interface, including LLM inference logic and skill
  parsing.
- **`:feature:download`**: Background worker implementation for downloading LLM models from Hugging
  Face.
- **`:feature:settings`**: Application settings and configuration.
- **`:feature:tooling`**: Native tool implementations (e.g., Jsoup-based web search, Calendar
  provider).

### App Module (`:app`)

- The main entry point that wires all modules together and initializes Koin.

## Key Technologies

- **Jetpack Compose**: For a modern, declarative UI.
- **MediaPipe GenAI**: On-device LLM inference.
- **Google AI Edge Local Agents**: Structured function calling (skills) protocol.
- **Koin**: Lightweight dependency injection.
- **WorkManager**: Reliable background model downloading.
- **Jsoup**: Web scraping for the search tool.
- **OkHttp**: For network requests.

## How to Run

1. Connect a physical Android device (recommended) or an emulator.
   - **Note**: LLM inference is resource-intensive and works best on modern devices (e.g., Pixel 7
     or newer).
2. Select the `app` configuration in Android Studio.
3. Click **Run**.
4. Upon opening the Chat, the app will automatically start downloading the LLM model (~700MB). You
   can track the progress on the screen.

## Issues:

1. A new Gemma 4 LLM doesn't work with MediaPipe engine. It requires LiteRT engine (more advanced
   implementation).
2. Sometimes LLM hallucinates when asking about agenda, view calendar instead of using '
   view_calendar' tool



##
## Author:

<a href="https://github.com/dmitriy-chernysh" target="_blank">
  <img src="https://s.gravatar.com/avatar/72c649d298a8f0f088fd0850e19b9147?s=400" width="70" align="left">
</a>

### Dmitri Chernysh

**Senior Android Engineer & Tech Partner (10+ yrs)**

Specialized in Native Android (Kotlin + Jetpack Compose) and on-device AI/LLM / Google AI Edge.

Need to integrate AI capabilities, local LLMs, or buіld an Android app from 0 to launch?

<a href="https://www.upwork.com/services/product/development-it-dmitriy-2014048029086121797"><img src="https://img.shields.io/badge/book%201:1%20consultation-14a800?style=for-the-badge" />

<a href="https://mobile-dev.pro/?utm_source=gh_aichat&utm_medium=social&utm_campaign=portfolio"><img src="https://img.shields.io/badge/mobile--dev.pro-029791?style=for-the-badge" />
[![Linkedin](https://img.shields.io/badge/-linkedin-0A66C2?logo=linkedin&logoColor=white&style=for-the-badge)](https://www.linkedin.com/in/dmitriychernysh/)
[![Youtube](https://img.shields.io/badge/-youtube-red?logo=youtube&message=Youtube&style=for-the-badge)](https://www.youtube.com/@mobiledevpro?sub_confirmation=1&utm_source=github_main_profile)
[![Email](https://img.shields.io/badge/-Email-17273D?style=for-the-badge&logo=gmail&logoColor=white)](mailto:dmitriy.chernysh@mobile-dev.pro)



# CityHistory

A minimal, production-quality Android app that uses the [Groq API](https://groq.com/) to deliver concise historical summaries of any city in the world.

---

## Features

- Enter your Groq API key and a city name to get an AI-generated historical overview
- API key masked by default with a reveal toggle
- Loading indicator while the request is in progress
- Scrollable result card for longer responses
- Meaningful inline error messages for validation and network failures
- In-memory cache — repeated queries for the same city skip the network
- Automatic retry (up to 2 attempts) on network failures
- Button disabled during loading to prevent duplicate requests
- **Model Settings accordion** — expand to tune the LLM before sending a request:
  - **Max Tokens** — limits the response length (default: 250; clamped to 1–32 768)
  - **Stop Sequences** — comma-separated strings at which the model stops generating (e.g. `END, \n\n`)

---

## Screenshots

| Idle | Loading | Result | Error |
|------|---------|--------|-------|
| _(enter key + city)_ | _(spinner shown)_ | _(history in card)_ | _(red error text)_ |

---

## Tech Stack

| Concern | Library |
|---|---|
| Language | Kotlin |
| UI | Jetpack Compose + Material 3 |
| Architecture | MVVM + Clean Architecture |
| State | `StateFlow` / `MutableStateFlow` |
| DI | Hilt (KSP) |
| Networking | Retrofit 2 + OkHttp 4 |
| JSON | Kotlinx Serialization |
| Concurrency | Coroutines + `viewModelScope` |
| Lifecycle | `collectAsStateWithLifecycle` |

---

## Architecture

The project follows Clean Architecture with three layers:

```
presentation/   ← Compose UI + ViewModel
domain/         ← UseCase + Repository interface
data/           ← Repository impl + Retrofit service + DTOs
```

```
CityHistoryScreen
      │  collectAsStateWithLifecycle
      ▼
CityHistoryViewModel  (@HiltViewModel)
      │
      ▼
GetCityHistoryUseCase
      │
      ▼
CityHistoryRepository  (interface)
      │  @Binds
      ▼
CityHistoryRepositoryImpl  (in-memory cache + retry)
      │
      ▼
GroqApiService  (Retrofit)
      │
      ▼
https://api.groq.com/openai/v1/chat/completions
```

### UI State machine

```kotlin
sealed class CityHistoryUiState {
    data object Idle    : CityHistoryUiState()
    data object Loading : CityHistoryUiState()
    data class  Success(val history: String) : CityHistoryUiState()
    data class  Error(val message: String)   : CityHistoryUiState()
}
```

---

## Project Structure

```
app/src/main/java/com/example/cityhistory/
├── CityHistoryApplication.kt
├── MainActivity.kt
├── di/
│   └── NetworkModule.kt
├── data/
│   ├── api/
│   │   ├── GroqApiService.kt
│   │   └── dto/
│   │       ├── ChatRequest.kt
│   │       └── ChatResponse.kt
│   └── repository/
│       └── CityHistoryRepositoryImpl.kt
├── domain/
│   ├── repository/
│   │   └── CityHistoryRepository.kt
│   └── usecase/
│       └── GetCityHistoryUseCase.kt
└── presentation/
    ├── CityHistoryUiState.kt
    ├── CityHistoryViewModel.kt
    └── CityHistoryScreen.kt
```

---

## Getting Started

### Prerequisites

- Android Studio Narwhal (or newer — requires AGP 9.x support)
- Android SDK 24+
- A free [Groq API key](https://console.groq.com/keys)

### Build & Run

1. Clone the repository:
   ```bash
   git clone https://github.com/your-username/CityHistory.git
   cd CityHistory
   ```

2. Open the project in Android Studio.

3. Let Gradle sync finish (it will download all dependencies automatically).

4. Run on an emulator or physical device (minSdk 24).

### Usage

1. Launch the app.
2. Paste your Groq API key into the **Groq API Key** field (tap the eye icon to reveal/hide it).
3. Type a city name into the **City Name** field (e.g. *Rome*, *Tokyo*, *Cairo*).
4. _(Optional)_ Tap **Model Settings** to expand the accordion and adjust:
   - **Max Tokens** — how long the response can be (default 250).
   - **Stop Sequences** — comma-separated stop strings, e.g. `END`.
5. Tap **Get History**.
6. The historical summary appears in a card below. The result is cached — tapping the button again for the same city and settings returns instantly.

---

## Groq API

The app calls the [Groq chat completions endpoint](https://console.groq.com/docs/openai), which is OpenAI-compatible:

```
POST https://api.groq.com/openai/v1/chat/completions
Authorization: Bearer <YOUR_API_KEY>

{
  "model": "llama3-70b-8192",
  "messages": [{ "role": "user", "content": "Provide a concise historical overview of Rome" }],
  "max_tokens": 512
}
```

The API key is passed as a per-request header and is **never logged or stored on disk**.

---

## Security Notes

- The API key is held in Compose UI state (`rememberSaveable`) — it survives screen rotation but is cleared when the process dies.
- OkHttp's `HttpLoggingInterceptor` runs only in `DEBUG` builds and logs request/response bodies (excluding the Authorization header value, which is not separately redacted at the OkHttp level — avoid enabling `BODY`-level logging on devices shared with others).
- No API key is committed to source control.

---

## License

```
MIT License

Copyright (c) 2026

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```

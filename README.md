# ChessLog

**ChessLog** is a comprehensive Android application for chess enthusiasts to import, view, analyze, and save chess games. It serves as a bridge between your online games and a powerful local analysis tool.

## Features

-   **Game Import**: Seamlessly search for users and import game archives directly from Chess.com.
-   **Powerful Analysis**:
    -   Integrated **Stockfish** engine (Online API & Local Binary support) for real-time evaluation and best move suggestions.
    -   Interactive chessboard for replaying moves and exploring variations.
    -   Legal move validation and PGN parsing.
-   **Local Library**: Save interesting games to your personal local database for offline access.
-   **Notes**: Add personal notes and annotations to any saved game.
-   **Game Management**: Edit player names and manage your saved collection.

## Tech Stack & Architecture

The app is built using modern Android development practices, following the **MVVM (Model-View-ViewModel)** architecture:

-   **Language**: Java
-   **UI**: XML Layouts with ViewBinding, Fragments, Navigation Component
-   **Local Database**: **Room** (SQLite abstraction)
-   **Networking**: **Retrofit** (for Chess.com and Stockfish APIs)
-   **Chess Logic**: `chesslib` for move generation and validation.
-   **Asynchronous Operations**: `LiveData` and `Executors`.

### Project Structure

-   `com.app.chesslog.data`: Contains Room entities (`ChessGame`), DAOs, and Retrofit models.
-   `com.app.chesslog.views`: Custom views (e.g., `ChessboardView`).
-   `com.app.chesslog.viewmodels`: `MainViewModel` handling state and business logic.
-   `com.app.chesslog.ui`: Fragments for specific screens (`AnalysisFragment`, `GamesFragment`, `ImportFragment`).

## Setup & Installation

1.  **Clone the repository**:
    ```bash
    git clone https://github.com/yourusername/chesslog.git
    ```
2.  **Open in Android Studio**:
    -   Select "Open an existing Android Studio project" and navigate to the cloned directory.
3.  **Build the Project**:
    -   Let Gradle sync and download dependencies.
4.  **Run**:
    -   Connect an Android device or start an emulator.
    -   Click the **Run** button (Shift + F10).

## Dependencies

-   [chesslib](https://github.com/bhlangonijr/chesslib): A Java chess library for move generation and validation.
-   [Retrofit](https://square.github.io/retrofit/): A type-safe HTTP client for Android.
-   [Room Persistence Library](https://developer.android.com/training/data-storage/room): For local data storage.
-   [Gson](https://github.com/google/gson): For JSON parsing.

## License

[MIT License](LICENSE)

package com.app.chesslog;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class UCIEngine {

    private static final String TAG = "UCIEngine";

    private Process engineProcess;
    private BufferedReader reader;
    private OutputStreamWriter writer;
    private final ExecutorService executor = Executors.newSingleThreadExecutor(); // For reading engine output

    private Consumer<String> onBestMoveListener;
    private Consumer<String> onInfoListener;
    private Consumer<String> onErrorListener;

    public void setOnBestMoveListener(Consumer<String> listener) {
        this.onBestMoveListener = listener;
    }

    public void setOnInfoListener(Consumer<String> listener) {
        this.onInfoListener = listener;
    }

    public void setOnErrorListener(Consumer<String> listener) {
        this.onErrorListener = listener;
    }

    /**
     * Starts the Stockfish engine process.
     * @param enginePath The full path to the Stockfish executable.
     */
    public void start(String enginePath) throws IOException {
        if (engineProcess != null) {
            stop(); // Stop any existing process
        }

        Log.d(TAG, "Starting engine at: " + enginePath);
        ProcessBuilder processBuilder = new ProcessBuilder(enginePath);
        engineProcess = processBuilder.start();

        if (engineProcess == null || engineProcess.getOutputStream() == null) {
            throw new IOException("Failed to start engine process or get output stream.");
        }

        reader = new BufferedReader(new InputStreamReader(engineProcess.getInputStream()));
        writer = new OutputStreamWriter(engineProcess.getOutputStream());

        // Start a background thread to read engine output
        executor.submit(this::readEngineOutput);

        // Initial UCI handshake
        sendCommand("uci");
        sendCommand("isready"); // Wait for engine to be ready
        // No need to block here, the readEngineOutput will handle responses
        Log.d(TAG, "Engine started. Waiting for UCI handshake...");
    }

    private void readEngineOutput() {
        String line;
        try {
            while ((line = reader.readLine()) != null) {
                Log.d(TAG, "Engine output: " + line);
                if (line.startsWith("bestmove")) {
                    if (onBestMoveListener != null) {
                        onBestMoveListener.accept(line);
                    }
                } else if (line.startsWith("info")) {
                    if (onInfoListener != null) {
                        onInfoListener.accept(line);
                    }
                } else if (line.startsWith("readyok")) {
                    Log.d(TAG, "Engine is ready.");
                    // You might want a callback here to signal the engine is ready
                }
            }
        } catch (IOException e) {
            Log.e(TAG, "Error reading engine output: " + e.getMessage());
            if (onErrorListener != null) {
                onErrorListener.accept("Error reading engine output: " + e.getMessage());
            }
        } finally {
            Log.d(TAG, "Engine output reader stopped.");
        }
    }

    /**
     * Sends a command to the engine.
     * @param command The UCI command to send.
     */
    public void sendCommand(String command) {
        try {
            writer.write(command + "\n");
            writer.flush();
            Log.d(TAG, "Sent command: " + command);
        } catch (IOException e) {
            Log.e(TAG, "Error sending command: " + e.getMessage());
            if (onErrorListener != null) {
                onErrorListener.accept("Error sending command: " + e.getMessage());
            }
        }
    }

    /**
     * Stops the engine process and cleans up resources.
     */
    public void stop() {
        if (engineProcess != null) {
            engineProcess.destroy(); // Terminate the process
            engineProcess = null;
            Log.d(TAG, "Engine process stopped.");
        }
        if (reader != null) {
            try {
                reader.close();
            } catch (IOException e) {
                Log.e(TAG, "Error closing reader: " + e.getMessage());
            }
        }
        if (writer != null) {
            try {
                writer.close();
            } catch (IOException e) {
                Log.e(TAG, "Error closing writer: " + e.getMessage());
            }
        }
        // Do not shut down executor here, it's reused for multiple analyses
        // executor.shutdown();
        Log.d(TAG, "Engine resources cleaned up.");
    }
}

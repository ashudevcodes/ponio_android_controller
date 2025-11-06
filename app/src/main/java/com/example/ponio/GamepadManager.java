package com.example.ponio;

import android.util.Log;
import android.view.View;

import com.google.android.material.button.MaterialButton;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GamepadManager {
    private static final String TAG = "GamepadManager";
    private static GamepadManager instance;

    private Socket socket;
    private OutputStream outputStream;
    private ExecutorService executor;
    public boolean isConnected = false;

    public MaterialButton btnA, btnB, btnX, btnY;
    public MaterialButton btnL1, btnR1, btnL2, btnR2;
    public MaterialButton btnStart, btnSelect;
    public MaterialButton btnDpadUp, btnDpadDown, btnDpadLeft, btnDpadRight;
    public View leftJoystick, rightJoystick;

    public float leftJoyX = 0.5f, leftJoyY = 0.5f;
    public float rightJoyX = 0.5f, rightJoyY = 0.5f;

    private GamepadManager() {
        executor = Executors.newSingleThreadExecutor();
    }

    public static synchronized GamepadManager getInstance() {
        if (instance == null) {
            instance = new GamepadManager();
        }
        return instance;
    }

    public void connect(String ip, int port, ConnectionCallback callback) {
        executor.execute(() -> {
            try {
                socket = new Socket(ip, port);
                outputStream = socket.getOutputStream();
                isConnected = true;

                sendCommand("CONNECT:Ponio Android Gamepad");

                if (callback != null) {
                    callback.onSuccess("Connected to " + ip + ":" + port);
                }

            } catch (IOException e) {
                isConnected = false;
                if (callback != null) {
                    callback.onError("Connection failed: " + e.getMessage());
                }
                Log.e(TAG, "Connection failed", e);
            }
        });
    }

    public void disconnect(DisconnectCallback callback) {
        executor.execute(() -> {
            try {
                if (socket != null && !socket.isClosed()) {
                    sendCommand("DISCONNECT");
                    socket.close();
                }
                isConnected = false;

                if (callback != null) {
                    callback.onDisconnected();
                }

            } catch (IOException e) {
                Log.e(TAG, "Disconnect failed", e);
                if (callback != null) {
                    callback.onDisconnected();
                }
            }
        });
    }

    public void sendCommand(String command) {
        if (!isConnected || outputStream == null) {
            Log.w(TAG, "Cannot send command - not connected");
            return;
        }

        executor.execute(() -> {
            try {
                String message = command + "\n";
                outputStream.write(message.getBytes());
                outputStream.flush();
                Log.d(TAG, "Command sent: " + command);
            } catch (IOException e) {
                Log.e(TAG, "Failed to send command: " + command, e);
                isConnected = false;
            }
        });
    }

    public void enableGamepadControls(boolean enabled) {
        if (btnA == null) {
            Log.w(TAG, "Buttons not initialized yet");
            return;
        }

        float alpha = enabled ? 1.0f : 0.5f;

        btnA.setEnabled(enabled);
        btnB.setEnabled(enabled);
        btnX.setEnabled(enabled);
        btnY.setEnabled(enabled);

        btnL1.setEnabled(enabled);
        btnR1.setEnabled(enabled);
        btnL2.setEnabled(enabled);
        btnR2.setEnabled(enabled);

        btnStart.setEnabled(enabled);
        btnSelect.setEnabled(enabled);

        btnDpadUp.setEnabled(enabled);
        btnDpadDown.setEnabled(enabled);
        btnDpadLeft.setEnabled(enabled);
        btnDpadRight.setEnabled(enabled);

        btnA.setAlpha(alpha);
        btnB.setAlpha(alpha);
        btnX.setAlpha(alpha);
        btnY.setAlpha(alpha);
        btnL1.setAlpha(alpha);
        btnR1.setAlpha(alpha);
        btnL2.setAlpha(alpha);
        btnR2.setAlpha(alpha);
        btnStart.setAlpha(alpha);
        btnSelect.setAlpha(alpha);
        btnDpadUp.setAlpha(alpha);
        btnDpadDown.setAlpha(alpha);
        btnDpadLeft.setAlpha(alpha);
        btnDpadRight.setAlpha(alpha);

        if (leftJoystick != null) leftJoystick.setAlpha(alpha);
        if (rightJoystick != null) rightJoystick.setAlpha(alpha);

        Log.d(TAG, "Gamepad controls " + (enabled ? "enabled" : "disabled"));
    }

    public void cleanup() {
        disconnect(null);
        if (executor != null && !executor.isShutdown()) {
            executor.shutdown();
        }
    }

    public interface ConnectionCallback {
        void onSuccess(String message);
        void onError(String error);
    }

    public interface DisconnectCallback {
        void onDisconnected();
    }
}

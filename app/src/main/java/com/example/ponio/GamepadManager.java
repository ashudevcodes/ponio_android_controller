package com.example.ponio;

import android.util.Log;
import android.view.View;

import com.google.android.material.button.MaterialButton;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GamepadManager {
    private static final String TAG = "GamepadManager";
    private static GamepadManager instance;

    // Connection protocol (TCP/UDP/Bluetooth)
    private ConnectionProtocol protocol;
    private String currentProtocol = "tcp"; // Default
    private ExecutorService executor;
    public boolean isConnected = false;

    // UI Components
    public MaterialButton btnA, btnB, btnX, btnY;
    public MaterialButton btnL1, btnR1, btnL2, btnR2;
    public MaterialButton btnStart, btnSelect;
    public MaterialButton btnDpadUp, btnDpadDown, btnDpadLeft, btnDpadRight;
    public View leftJoystick, rightJoystick;

    public float leftJoyX = 0.5f, leftJoyY = 0.5f;
    public float rightJoyX = 0.5f, rightJoyY = 0.5f;

    // Private constructor
    private GamepadManager() {
        executor = Executors.newSingleThreadExecutor();
        protocol = ConnectionProtocol.create("tcp");
    }

    // Singleton instance accessor
    public static synchronized GamepadManager getInstance() {
        if (instance == null) {
            instance = new GamepadManager();
        }
        return instance;
    }

    // Set connection protocol (call before connect)
    public void setProtocol(String protocolType) {
        this.currentProtocol = protocolType.toLowerCase();
        this.protocol = ConnectionProtocol.create(protocolType);
        Log.d(TAG, "Protocol set to: " + protocolType);
    }

    public String getCurrentProtocol() {
        return currentProtocol;
    }

    // Connect to server with specified protocol
    public void connect(String address, int port, ConnectionCallback callback) {
        executor.execute(() -> {
            try {
                protocol.connect(address, port);
                isConnected = true;

                // Send initial connection message
                sendCommand("CONNECT:Ponio Android Gamepad [" + protocol.getProtocolName() + "]");

                if (callback != null) {
                    callback.onSuccess("Connected via " + protocol.getProtocolName() + " to " + address + ":" + port);
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

    // Disconnect from server
    public void disconnect(DisconnectCallback callback) {
        executor.execute(() -> {
            try {
                if (protocol != null && protocol.isConnected()) {
				    protocol.disconnect();
                    sendCommand("DISCONNECT");
                }
                isConnected = false;

                if (callback != null) {
                    callback.onDisconnected();
                }

            } catch (IOException e) {
                Log.e(TAG, "Disconnect failed", e);
                isConnected = false;
                if (callback != null) {
                    callback.onDisconnected();
                }
            }
        });
    }

    // Send command to server
    public void sendCommand(String command) {
        if (!isConnected || protocol == null || !protocol.isConnected()) {
            Log.w(TAG, "Cannot send command - not connected");
            return;
        }

        executor.execute(() -> {
            try {
                String message = command + "\n";
                protocol.sendData(message.getBytes());
                Log.d(TAG, "Command sent via " + protocol.getProtocolName() + ": " + command);
            } catch (IOException e) {
                Log.e(TAG, "Failed to send command: " + command, e);
                isConnected = false;
            }
        });
    }

    // Enable/disable gamepad controls
    public void enableGamepadControls(boolean enabled) {
        if (btnA == null) {
            Log.w(TAG, "Buttons not initialized yet");
            return;
        }

        float alpha = enabled ? 1.0f : 0.5f;

        // Face buttons
        btnA.setEnabled(enabled);
        btnB.setEnabled(enabled);
        btnX.setEnabled(enabled);
        btnY.setEnabled(enabled);

        // Shoulder buttons
        btnL1.setEnabled(enabled);
        btnR1.setEnabled(enabled);
        btnL2.setEnabled(enabled);
        btnR2.setEnabled(enabled);

        // Menu buttons
        btnStart.setEnabled(enabled);
        btnSelect.setEnabled(enabled);

        // D-pad
        btnDpadUp.setEnabled(enabled);
        btnDpadDown.setEnabled(enabled);
        btnDpadLeft.setEnabled(enabled);
        btnDpadRight.setEnabled(enabled);

        // Set alpha
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

    // Cleanup
    public void cleanup() {
        disconnect(null);
        if (executor != null && !executor.isShutdown()) {
            executor.shutdown();
        }
    }

    // Callback interfaces
    public interface ConnectionCallback {
        void onSuccess(String message);
        void onError(String error);
    }

    public interface DisconnectCallback {
        void onDisconnected();
    }
}

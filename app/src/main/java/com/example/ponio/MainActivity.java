package com.example.ponio;

import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {
    private Socket socket;
    private OutputStream outputStream;
    private ExecutorService executor;
    private boolean isConnected = false;

    private TextInputEditText serverIpInput;
    private TextInputEditText serverPortInput;
    private MaterialButton connectButton;
    private TextView statusText;

    private MaterialButton btnA, btnB, btnX, btnY;
    private MaterialButton btnL1, btnR1, btnL2, btnR2;
    private MaterialButton btnStart, btnSelect;
    private MaterialButton btnDpadUp, btnDpadDown, btnDpadLeft, btnDpadRight;
    private View leftJoystick, rightJoystick;

    private float leftJoyX = 0.5f, leftJoyY = 0.5f;
    private float rightJoyX = 0.5f, rightJoyY = 0.5f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        executor = Executors.newSingleThreadExecutor();

        initializeViews();
        setupConnectionControls();
        setupGamepadControls();
    }

    private void initializeViews() {
        serverIpInput = findViewById(R.id.serverIpInput);
        serverPortInput = findViewById(R.id.serverPortInput);
        connectButton = findViewById(R.id.connectButton);
        statusText = findViewById(R.id.statusText);

        // Face buttons
        btnA = findViewById(R.id.btnA);
        btnB = findViewById(R.id.btnB);
        btnX = findViewById(R.id.btnX);
        btnY = findViewById(R.id.btnY);

        // Shoulder buttons
        btnL1 = findViewById(R.id.btnL1);
        btnR1 = findViewById(R.id.btnR1);
        btnL2 = findViewById(R.id.btnL2);
        btnR2 = findViewById(R.id.btnR2);

        // Menu buttons
        btnStart = findViewById(R.id.btnStart);
        btnSelect = findViewById(R.id.btnSelect);

        // D-pad
        btnDpadUp = findViewById(R.id.btnDpadUp);
        btnDpadDown = findViewById(R.id.btnDpadDown);
        btnDpadLeft = findViewById(R.id.btnDpadLeft);
        btnDpadRight = findViewById(R.id.btnDpadRight);

        // Joysticks
        leftJoystick = findViewById(R.id.leftJoystick);
        rightJoystick = findViewById(R.id.rightJoystick);

        // Set default values
        serverIpInput.setText("192.168.1.100");
        serverPortInput.setText("8888");
    }

    private void setupConnectionControls() {
        connectButton.setOnClickListener(v -> {
            if (!isConnected) {
                String ip = serverIpInput.getText().toString();
                String portStr = serverPortInput.getText().toString();

                if (ip.isEmpty() || portStr.isEmpty()) {
                    Toast.makeText(this, "Please enter IP and Port", Toast.LENGTH_SHORT).show();
                    return;
                }

                int port = Integer.parseInt(portStr);
                connectToServer(ip, port);
            } else {
                disconnect();
            }
        });
    }

    private void setupGamepadControls() {
        // Face buttons
        setupButton(btnA, "BTN_A");
        setupButton(btnB, "BTN_B");
        setupButton(btnX, "BTN_X");
        setupButton(btnY, "BTN_Y");

        // Shoulder buttons
        setupButton(btnL1, "BTN_L1");
        setupButton(btnR1, "BTN_R1");
        setupButton(btnL2, "BTN_L2");
        setupButton(btnR2, "BTN_R2");

        // Menu buttons
        setupButton(btnStart, "BTN_START");
        setupButton(btnSelect, "BTN_SELECT");

        // D-pad
        setupButton(btnDpadUp, "DPAD_UP");
        setupButton(btnDpadDown, "DPAD_DOWN");
        setupButton(btnDpadLeft, "DPAD_LEFT");
        setupButton(btnDpadRight, "DPAD_RIGHT");

        // Joysticks
        setupJoystick(leftJoystick, true);
        setupJoystick(rightJoystick, false);
    }

    private void setupButton(MaterialButton button, String buttonName) {
        button.setOnTouchListener((v, event) -> {
            if (!isConnected) return false;

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    sendCommand(buttonName + ":1");
                    button.setPressed(true);
                    return true;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    sendCommand(buttonName + ":0");
                    button.setPressed(false);
                    return true;
            }
            return false;
        });
    }

    private void setupJoystick(View joystick, boolean isLeft) {
        joystick.setOnTouchListener((v, event) -> {
            if (!isConnected) return false;

            float x = event.getX() / v.getWidth();
            float y = event.getY() / v.getHeight();

            // Clamp values between 0 and 1
            x = Math.max(0, Math.min(1, x));
            y = Math.max(0, Math.min(1, y));

            // Convert to -1.0 to 1.0 range
            float joyX = (x - 0.5f) * 2.0f;
            float joyY = (y - 0.5f) * 2.0f;

            if (isLeft) {
                leftJoyX = x;
                leftJoyY = y;
                sendCommand(String.format("LJOY:%.3f,%.3f", joyX, joyY));
            } else {
                rightJoyX = x;
                rightJoyY = y;
                sendCommand(String.format("RJOY:%.3f,%.3f", joyX, joyY));
            }

            switch (event.getAction()) {
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    // Reset to center
                    if (isLeft) {
                        sendCommand("LJOY:0.000,0.000");
                    } else {
                        sendCommand("RJOY:0.000,0.000");
                    }
                    break;
            }

            return true;
        });
    }

    private void connectToServer(String ip, int port) {
        executor.execute(() -> {
            try {
                socket = new Socket(ip, port);
                outputStream = socket.getOutputStream();
                isConnected = true;

                runOnUiThread(() -> {
                    statusText.setText("Connected to " + ip + ":" + port);
                    statusText.setTextColor(getColor(android.R.color.holo_green_dark));
                    connectButton.setText("Disconnect");
                    Toast.makeText(this, "Connected successfully", Toast.LENGTH_SHORT).show();
                    enableGamepadControls(true);
                });

                // Send initial connection message
                sendCommand("CONNECT:Android Virtual Gamepad");

            } catch (IOException e) {
                runOnUiThread(() -> {
                    statusText.setText("Connection failed: " + e.getMessage());
                    statusText.setTextColor(getColor(android.R.color.holo_red_dark));
                    Toast.makeText(this, "Connection failed", Toast.LENGTH_SHORT).show();
                });
                e.printStackTrace();
            }
        });
    }

    private void disconnect() {
        executor.execute(() -> {
            try {
                if (socket != null && !socket.isClosed()) {
                    sendCommand("DISCONNECT");
                    socket.close();
                }
                isConnected = false;

                runOnUiThread(() -> {
                    statusText.setText("Disconnected");
                    statusText.setTextColor(getColor(android.R.color.darker_gray));
                    connectButton.setText("Connect");
                    Toast.makeText(this, "Disconnected", Toast.LENGTH_SHORT).show();
                    enableGamepadControls(false);
                });

            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    private void sendCommand(String command) {
        if (!isConnected || outputStream == null) return;

        executor.execute(() -> {
            try {
                String message = command + "\n";
                outputStream.write(message.getBytes());
                outputStream.flush();
            } catch (IOException e) {
                runOnUiThread(() -> {
                    Toast.makeText(this, "Send failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
                e.printStackTrace();
                disconnect();
            }
        });
    }

    private void enableGamepadControls(boolean enabled) {
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
        leftJoystick.setAlpha(alpha);
        rightJoystick.setAlpha(alpha);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        disconnect();
        executor.shutdown();
    }
}
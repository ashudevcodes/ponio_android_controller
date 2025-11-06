package com.example.ponio;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class MainActivity extends AppCompatActivity {
    private TextInputEditText serverIpInput;
    private TextInputEditText serverPortInput;
    private MaterialButton connectButton;
    private TextView statusText;
    
    private GamepadManager gamepadManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        gamepadManager = GamepadManager.getInstance();
        
        initializeViews();
        setupConnectionControls();
    }

    private void initializeViews() {
        serverIpInput = findViewById(R.id.serverIpInput);
        serverPortInput = findViewById(R.id.serverPortInput);
        connectButton = findViewById(R.id.connectButton);
        statusText = findViewById(R.id.statusText);

        // Set default values
        serverIpInput.setText("192.168.29.4");
        serverPortInput.setText("8888");
    }

    private void setupConnectionControls() {
        connectButton.setOnClickListener(v -> {
            if (!gamepadManager.isConnected) {
                String ip = serverIpInput.getText().toString().trim();
                String portStr = serverPortInput.getText().toString().trim();

                if (ip.isEmpty() || portStr.isEmpty()) {
                    Toast.makeText(this, "Please enter IP and Port", Toast.LENGTH_SHORT).show();
                    return;
                }

                try {
                    int port = Integer.parseInt(portStr);
                    connectToServer(ip, port);
                } catch (NumberFormatException e) {
                    Toast.makeText(this, "Invalid port number", Toast.LENGTH_SHORT).show();
                }

            } else {
                disconnect();
            }
        });
    }

    private void connectToServer(String ip, int port) {
        // Disable button during connection
        connectButton.setEnabled(false);
        statusText.setText("Connecting...");
        statusText.setTextColor(getColor(android.R.color.holo_orange_dark));

        gamepadManager.connect(ip, port, new GamepadManager.ConnectionCallback() {
            @Override
            public void onSuccess(String message) {
                runOnUiThread(() -> {
                    statusText.setText(message);
                    statusText.setTextColor(getColor(android.R.color.holo_green_dark));
                    connectButton.setText("Disconnect");
                    connectButton.setEnabled(true);
                    Toast.makeText(MainActivity.this, "Connected successfully", Toast.LENGTH_SHORT).show();
                    
                    // Navigate to controller screen
                    Intent intent = new Intent(MainActivity.this, ControllerActivity.class);
                    startActivity(intent);
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    statusText.setText("Connection failed");
                    statusText.setTextColor(getColor(android.R.color.holo_red_dark));
                    connectButton.setEnabled(true);
                    Toast.makeText(MainActivity.this, error, Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    private void disconnect() {
        connectButton.setEnabled(false);
        
        gamepadManager.disconnect(() -> {
            runOnUiThread(() -> {
                statusText.setText("Disconnected");
                statusText.setTextColor(getColor(android.R.color.darker_gray));
                connectButton.setText("Connect");
                connectButton.setEnabled(true);
                Toast.makeText(MainActivity.this, "Disconnected", Toast.LENGTH_SHORT).show();
            });
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        
        if (gamepadManager.isConnected) {
            connectButton.setText("Disconnect");
            statusText.setText("Connected");
            statusText.setTextColor(getColor(android.R.color.holo_green_dark));
        } else {
            connectButton.setText("Connect");
            statusText.setText("Not Connected");
            statusText.setTextColor(getColor(android.R.color.darker_gray));
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (isFinishing() && gamepadManager.isConnected) {
            gamepadManager.cleanup();
        }
    }
}

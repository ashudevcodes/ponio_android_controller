package com.example.ponio;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final int PERMISSION_REQUEST_CODE = 100;
    private TextInputEditText serverIpInput;
    private TextInputEditText serverPortInput;
    private MaterialButton connectButton;
    private TextView statusText;
    private MaterialButton scanButton;
    private RecyclerView serverListView;
    private ProgressBar scanProgress;
    private TextView scanStatus;
    private ServerListAdapter adapter;
    private RadioGroup protocolGroup;
    private GamepadManager gamepadManager;
    private ServerDiscovery serverDiscovery;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        gamepadManager = GamepadManager.getInstance();
        serverDiscovery = new ServerDiscovery();

        initializeViews();
        setupConnectionControls();
        setupAutoDiscovery();
        checkPermissions();
    }

    private void initializeViews() {
        serverIpInput = findViewById(R.id.serverIpInput);
        serverPortInput = findViewById(R.id.serverPortInput);
        connectButton = findViewById(R.id.connectButton);
        statusText = findViewById(R.id.statusText);

        scanButton = findViewById(R.id.scanButton);
        serverListView = findViewById(R.id.serverListView);
        scanProgress = findViewById(R.id.scanProgress);
        scanStatus = findViewById(R.id.scanStatus);

        protocolGroup = findViewById(R.id.protocolGroup);

        serverIpInput.setText("");
        serverPortInput.setText("8888");

        serverListView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ServerListAdapter(server -> {
            serverIpInput.setText(server.ipAddress);
            serverPortInput.setText(String.valueOf(server.port));

            gamepadManager.setProtocol(server.protocol);
            updateProtocolSelection(server.protocol);

            connectToServer(server.ipAddress, server.port);
        });
        serverListView.setAdapter(adapter);

        protocolGroup.check(R.id.radioTcp);
    }

    private void setupConnectionControls() {
        protocolGroup.setOnCheckedChangeListener((group, checkedId) -> {
            String protocol = "tcp";
            if (checkedId == R.id.radioTcp) {
                protocol = "tcp";
            } else if (checkedId == R.id.radioUdp) {
                protocol = "udp";
            } else if (checkedId == R.id.radioBluetooth) {
                protocol = "bluetooth";
            }
            gamepadManager.setProtocol(protocol);
            Toast.makeText(this, "Protocol: " + protocol.toUpperCase(), Toast.LENGTH_SHORT).show();
        });

        // Manual connect button
        connectButton.setOnClickListener(v -> {
            if (!gamepadManager.isConnected) {
                String ip = serverIpInput.getText().toString().trim();
                String portStr = serverPortInput.getText().toString().trim();

                if (ip.isEmpty() || portStr.isEmpty()) {
                    Toast.makeText(this, "Please enter IP and Port or scan for servers", Toast.LENGTH_SHORT).show();
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

    private void setupAutoDiscovery() {
        scanButton.setOnClickListener(v -> {
            scanForServers();
        });
    }

    private void scanForServers() {
        scanButton.setEnabled(false);
        scanProgress.setVisibility(View.VISIBLE);
        scanStatus.setText("Scanning for Ponio servers...");
        scanStatus.setVisibility(View.VISIBLE);
        adapter.clear();

        serverDiscovery.scanForServers(new ServerDiscovery.ScanCallback() {
            @Override
            public void onServerFound(ServerDiscovery.DiscoveredServer server) {
                runOnUiThread(() -> {
                    adapter.addServer(server);
                    scanStatus.setText("Found: " + server.name);
                });
            }

            @Override
            public void onScanComplete(List<ServerDiscovery.DiscoveredServer> servers) {
                runOnUiThread(() -> {
                    scanButton.setEnabled(true);
                    scanProgress.setVisibility(View.GONE);

                    if (servers.isEmpty()) {
                        scanStatus.setText("No servers found. Make sure Ponio server is running.");
                    } else {
                        scanStatus.setText("Found " + servers.size() + " server(s). Tap to connect.");
                    }
                });
            }

            @Override
            public void onScanError(String error) {
                runOnUiThread(() -> {
                    scanButton.setEnabled(true);
                    scanProgress.setVisibility(View.GONE);
                    scanStatus.setText("Scan error: " + error);
                    Toast.makeText(MainActivity.this, "Scan failed: " + error, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void connectToServer(String ip, int port) {
        // Disable button during connection
        connectButton.setEnabled(false);
        statusText.setText("Connecting via " + gamepadManager.getCurrentProtocol().toUpperCase() + "...");
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

    private void updateProtocolSelection(String protocol) {
        switch (protocol.toLowerCase()) {
            case "tcp":
                protocolGroup.check(R.id.radioTcp);
                break;
            case "udp":
                protocolGroup.check(R.id.radioUdp);
                break;
            case "bluetooth":
                protocolGroup.check(R.id.radioBluetooth);
                break;
        }
    }

    private void checkPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // Android 12+ needs Bluetooth permissions
            String[] permissions = {
                    Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.BLUETOOTH_CONNECT,
                    Manifest.permission.ACCESS_FINE_LOCATION
            };

            boolean needsPermission = false;
            for (String permission : permissions) {
                if (ContextCompat.checkSelfPermission(this, permission)
                        != PackageManager.PERMISSION_GRANTED) {
                    needsPermission = true;
                    break;
                }
            }

            if (needsPermission) {
                ActivityCompat.requestPermissions(this, permissions, PERMISSION_REQUEST_CODE);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            boolean allGranted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                    break;
                }
            }

            if (!allGranted) {
                Toast.makeText(this,
                        "Bluetooth permissions required for Bluetooth connection",
                        Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        
        if (gamepadManager.isConnected) {
            connectButton.setText("Disconnect");
            statusText.setText("Connected via " + gamepadManager.getCurrentProtocol().toUpperCase());
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
        if (serverDiscovery != null) {
            serverDiscovery.cleanup();
        }
        if (isFinishing() && gamepadManager.isConnected) {
            gamepadManager.cleanup();
        }
    }
}
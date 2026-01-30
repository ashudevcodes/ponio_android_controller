package com.example.ponio;

import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ServerDiscovery {
    private static final String TAG = "ServerDiscovery";
    private static final int DISCOVERY_PORT = 8889;
    private static final String DISCOVERY_MESSAGE = "PONIO_DISCOVER";
    private static final int BROADCAST_TIMEOUT = 3000; // 3 seconds

    private ExecutorService executor;
    private boolean isScanning = false;

    public ServerDiscovery() {
        executor = Executors.newSingleThreadExecutor();
    }

    public void scanForServers(ScanCallback callback) {
        if (isScanning) {
            Log.w(TAG, "Scan already in progress");
            return;
        }

        isScanning = true;
        List<DiscoveredServer> servers = new ArrayList<>();

        executor.execute(() -> {
            DatagramSocket socket = null;
            try {
                socket = new DatagramSocket();
                socket.setBroadcast(true);
                socket.setSoTimeout(BROADCAST_TIMEOUT);

                // Send broadcast message
                byte[] sendData = DISCOVERY_MESSAGE.getBytes();

                // Try multiple broadcast addresses
                List<InetAddress> broadcastAddresses = getBroadcastAddresses();
                for (InetAddress broadcastAddress : broadcastAddresses) {
                    try {
                        DatagramPacket sendPacket = new DatagramPacket(
                                sendData,
                                sendData.length,
                                broadcastAddress,
                                DISCOVERY_PORT
                        );
                        socket.send(sendPacket);
                        Log.d(TAG, "Sent broadcast to: " + broadcastAddress.getHostAddress());
                    } catch (IOException e) {
                        Log.e(TAG, "Failed to send to " + broadcastAddress, e);
                    }
                }

                // Listen for responses
                byte[] receiveData = new byte[1024];
                long startTime = System.currentTimeMillis();

                while (System.currentTimeMillis() - startTime < BROADCAST_TIMEOUT) {
                    try {
						 DatagramPacket receivePacket = new DatagramPacket(
								 receiveData, 
								 receiveData.length
						 );

                        socket.receive(receivePacket);

                        String response = new String(
							  receivePacket.getData(),
							  0,
							  receivePacket.getLength()
						);

                        String serverIp = receivePacket.getAddress().getHostAddress();

                        // Parse server response (format: "PONIO_SERVER:name:port:tcp/udp")
                        if (response.startsWith("PONIO_SERVER:")) {
                            DiscoveredServer server = parseServerResponse(response, serverIp);
                            if (server != null && !servers.contains(server)) {
                                servers.add(server);
                                Log.d(TAG, "Found server: " + server.name + " at " + serverIp);

                                // Notify callback for each found server
                                callback.onServerFound(server);
                            }
                        }
                    } catch (IOException e) {
                        // Timeout or error - continue scanning
                    }
                }

                callback.onScanComplete(servers);

            } catch (Exception e) {
                Log.e(TAG, "Scan error", e);
                callback.onScanError(e.getMessage());
            } finally {
                if (socket != null && !socket.isClosed()) {
                    socket.close();
                }
                isScanning = false;
            }
        });
    }

    private DiscoveredServer parseServerResponse(String response, String ip) {
        try {
            // Format: "PONIO_SERVER:name:port:protocol"
            String[] parts = response.split(":");
            if (parts.length >= 4) {
                String name = parts[1];
                int port = Integer.parseInt(parts[2]);
                String protocol = parts[3];

                return new DiscoveredServer(name, ip, port, protocol);
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to parse server response: " + response, e);
        }
        return null;
    }

    private List<InetAddress> getBroadcastAddresses() {
        List<InetAddress> broadcastList = new ArrayList<>();

        try {
            List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());

            for (NetworkInterface networkInterface : interfaces) {
                if (networkInterface.isLoopback() || !networkInterface.isUp()) {
                    continue;
                }

                List<InetAddress> addresses = Collections.list(networkInterface.getInetAddresses());
                for (InetAddress address : addresses) {
                    if (address.isSiteLocalAddress()) {
                        // Calculate broadcast address
                        byte[] ip = address.getAddress();

                        // Simple broadcast calculation (works for /24 networks)
                        ip[3] = (byte) 255;

                        try {
                            InetAddress broadcast = InetAddress.getByAddress(ip);
                            broadcastList.add(broadcast);
                        } catch (Exception e) {
                            Log.e(TAG, "Failed to create broadcast address", e);
                        }
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to get broadcast addresses", e);
        }

        // Always add generic broadcast as fallback
        try {
            broadcastList.add(InetAddress.getByName("255.255.255.255"));
        } catch (Exception e) {
            Log.e(TAG, "Failed to add generic broadcast", e);
        }

        return broadcastList;
    }

    public void stopScan() {
        isScanning = false;
    }

    public void cleanup() {
        stopScan();
        if (executor != null && !executor.isShutdown()) {
            executor.shutdown();
        }
    }

    // Discovered server data class
    public static class DiscoveredServer {
        public String name;
        public String ipAddress;
        public int port;
        public String protocol; // "tcp" or "udp"

        public DiscoveredServer(String name, String ipAddress, int port, String protocol) {
            this.name = name;
            this.ipAddress = ipAddress;
            this.port = port;
            this.protocol = protocol;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof DiscoveredServer) {
                DiscoveredServer other = (DiscoveredServer) obj;
                return ipAddress.equals(other.ipAddress) && port == other.port;
            }
            return false;
        }

        @Override
        public int hashCode() {
            return (ipAddress + port).hashCode();
        }

        @Override
        public String toString() {
            return name + " (" + ipAddress + ":" + port + ") - " + protocol.toUpperCase();
        }
    }

    // Callback interface
    public interface ScanCallback {
        void onServerFound(DiscoveredServer server);
        void onScanComplete(List<DiscoveredServer> servers);
        void onScanError(String error);
    }
}

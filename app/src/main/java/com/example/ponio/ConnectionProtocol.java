package com.example.ponio;

// import android.bluetooth.BluetoothAdapter;
// import android.bluetooth.BluetoothDevice;
// import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.IOException;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.util.UUID;

public abstract class ConnectionProtocol {
    protected static final String TAG = "ConnectionProtocol";
    protected OutputStream outputStream;
    protected boolean isConnected = false;

    public abstract void connect(String address, int port) throws IOException;
    public abstract void disconnect() throws IOException;
    public abstract void sendData(byte[] data) throws IOException;
    public abstract boolean isConnected();
    public abstract String getProtocolName();

    // TCP Implementation
    public static class TCPProtocol extends ConnectionProtocol {
        private Socket socket;

        @Override
        public void connect(String address, int port) throws IOException {
            socket = new Socket(address, port);
            outputStream = socket.getOutputStream();
            isConnected = true;
            Log.d(TAG, "TCP connected to " + address + ":" + port);
        }

        @Override
        public void disconnect() throws IOException {
            isConnected = false;
            if (outputStream != null) {
                outputStream.close();
            }
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
            Log.d(TAG, "TCP disconnected");
        }

        @Override
        public void sendData(byte[] data) throws IOException {
            if (outputStream != null && isConnected) {
                outputStream.write(data);
                outputStream.flush();
            }
        }

        @Override
        public boolean isConnected() {
            return isConnected && socket != null && socket.isConnected() && !socket.isClosed();
        }

        @Override
        public String getProtocolName() {
            return "TCP";
        }
    }

    // UDP Implementation - COMMENTED OUT
    /*
    public static class UDPProtocol extends ConnectionProtocol {
        private DatagramSocket socket;
        private InetAddress serverAddress;
        private int serverPort;

        @Override
        public void connect(String address, int port) throws IOException {
            socket = new DatagramSocket();
            serverAddress = InetAddress.getByName(address);
            serverPort = port;
            isConnected = true;

            // Send initial connection packet
            sendData("CONNECT:Ponio Android Gamepad\n".getBytes());

            Log.d(TAG, "UDP connected to " + address + ":" + port);
        }

        @Override
        public void disconnect() throws IOException {
            isConnected = false;

            // Send disconnect packet
            try {
                sendData("DISCONNECT\n".getBytes());
            } catch (IOException e) {
                Log.w(TAG, "Failed to send disconnect packet", e);
            }

            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
            Log.d(TAG, "UDP disconnected");
        }

        @Override
        public void sendData(byte[] data) throws IOException {
            if (socket != null && !socket.isClosed() && isConnected) {
                DatagramPacket packet = new DatagramPacket(
                        data,
                        data.length,
                        serverAddress,
                        serverPort
                );
                socket.send(packet);
            }
        }

        @Override
        public boolean isConnected() {
            return isConnected && socket != null && !socket.isClosed();
        }

        @Override
        public String getProtocolName() {
            return "UDP";
        }
    }
    */

    // Bluetooth Implementation - COMMENTED OUT
    /*
    public static class BluetoothProtocol extends ConnectionProtocol {
        private BluetoothSocket bluetoothSocket;
        private BluetoothAdapter bluetoothAdapter;
        private static final UUID PONIO_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

        @Override
        public void connect(String address, int port) throws IOException {
            bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

            if (bluetoothAdapter == null) {
                throw new IOException("Bluetooth not supported on this device");
            }

            if (!bluetoothAdapter.isEnabled()) {
                throw new IOException("Bluetooth is not enabled");
            }

            BluetoothDevice device = bluetoothAdapter.getRemoteDevice(address);

            // Cancel discovery to improve connection speed
            if (bluetoothAdapter.isDiscovering()) {
                bluetoothAdapter.cancelDiscovery();
            }

            bluetoothSocket = device.createRfcommSocketToServiceRecord(PONIO_UUID);
            bluetoothSocket.connect();
            outputStream = bluetoothSocket.getOutputStream();
            isConnected = true;

            Log.d(TAG, "Bluetooth connected to " + address);
        }

        @Override
        public void disconnect() throws IOException {
            isConnected = false;
            if (outputStream != null) {
                outputStream.close();
            }
            if (bluetoothSocket != null) {
                bluetoothSocket.close();
            }
            Log.d(TAG, "Bluetooth disconnected");
        }

        @Override
        public void sendData(byte[] data) throws IOException {
            if (outputStream != null && isConnected) {
                outputStream.write(data);
                outputStream.flush();
            }
        }

        @Override
        public boolean isConnected() {
            return isConnected && bluetoothSocket != null && bluetoothSocket.isConnected();
        }

        @Override
        public String getProtocolName() {
            return "Bluetooth";
        }
    }
    */

    // Factory method to create protocol instances
    public static ConnectionProtocol create(String protocolType) {
        switch (protocolType.toLowerCase()) {
            case "tcp":
                return new TCPProtocol();
            // case "udp":
            //     return new UDPProtocol();
            // case "bluetooth":
            //     return new BluetoothProtocol();
            default:
                return new TCPProtocol(); // Default to TCP
        }
    }
}

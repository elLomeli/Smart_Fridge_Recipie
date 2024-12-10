package com.example.recipies;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Binder;
import android.os.IBinder;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

public class BluetoothService extends Service {
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothSocket btSocket;
    private InputStream inStream;
    private boolean isConnected = false;
    private String temperaturaStr, humedad;

    private final IBinder binder = new LocalBinder();
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    public class LocalBinder extends Binder {
        BluetoothService getService() {
            return BluetoothService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        return START_STICKY;
    }

    public boolean isBluetoothConnected() {
        return isConnected;
    }

    public void connectToBluetoothDevice(String deviceAddress) {
        BluetoothDevice device = bluetoothAdapter.getRemoteDevice(deviceAddress);
        try {
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED) {
                btSocket = device.createRfcommSocketToServiceRecord(MY_UUID);
                btSocket.connect();
                inStream = btSocket.getInputStream();
                isConnected = true;
                startBluetoothConnection();
                broadcastConnectionStatus(); // Notificar que se conectó
            }
        } catch (IOException e) {
            isConnected = false; // Asegurar que isConnected es false si falla
            broadcastConnectionStatus(); // Notificar que no se conectó
            Toast.makeText(this, "Error al conectar", Toast.LENGTH_SHORT).show();
        }
    }

    private void startBluetoothConnection() {
        new Thread(() -> {
            byte[] buffer = new byte[1024];
            int bytes;
            while (isConnected) {
                try {
                    bytes = inStream.read(buffer);
                    String readMessage = new String(buffer, 0, bytes);
                    String[] data = readMessage.split(",");
                    if (data.length >= 2) {
                        temperaturaStr = data[0];
                        humedad = data[1];
                        broadcastData();
                    }
                } catch (IOException e) {
                    isConnected = false;
                    broadcastConnectionStatus(); // Notificar que se desconectó
                    break;
                }
            }
        }).start();
    }

    private void broadcastData() {
        Intent intent = new Intent("BluetoothData");
        intent.putExtra("temperatura", temperaturaStr);
        intent.putExtra("humedad", humedad);
        sendBroadcast(intent);
    }

    private void broadcastConnectionStatus() {
        Intent intent = new Intent("BluetoothConnectionStatus");
        intent.putExtra("isConnected", isConnected);
        sendBroadcast(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        isConnected = false;
        broadcastConnectionStatus(); // Notificar que se desconectó al destruir el servicio
        try {
            if (btSocket != null) {
                btSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

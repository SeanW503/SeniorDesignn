package com.example.planteye;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import android.bluetooth.BluetoothSocket;
import java.io.IOException;
import java.util.UUID;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

public class settings extends AppCompatActivity {

    private static final int BLUETOOTH_REQUEST_CODE = 1;
    private BluetoothAdapter bluetoothAdapter;
    private BroadcastReceiver bluetoothReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // Initialize BluetoothAdapter
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        Button connectBtn = findViewById(R.id.connectBtn);
        connectBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (bluetoothAdapter == null) {
                    // Device does not support Bluetooth
                    Toast.makeText(settings.this, "Bluetooth is not supported on this device", Toast.LENGTH_SHORT).show();
                } else {
                    // Start discovering nearby devices
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (checkSelfPermission(Manifest.permission.DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION) != PackageManager.PERMISSION_GRANTED) {
                            // Request the permission
                            requestPermissions(new String[]{Manifest.permission.DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION}, PERMISSION_REQUEST_CODE);
                            return;
                        }
                    }
                    startDeviceDiscovery();
                }
            }
        });
    }

    private void startDeviceDiscovery() {
        // Check if BLUETOOTH_SCAN permission is granted
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_SCAN)
                != PackageManager.PERMISSION_GRANTED) {
            // Request BLUETOOTH_SCAN permission
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.BLUETOOTH_SCAN},
                    BLUETOOTH_REQUEST_CODE);
            return;
        }

        // Register for broadcasts when a device is discovered
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        bluetoothReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    // Check if the discovered device is your ESP32 device
                    if (device.getName() != null && device.getName().equals("ProjectBox")) {
                        // Connect to the ESP32 device
                        connectToDevice(device);
                    }
                }
            }
        };
        registerReceiver(bluetoothReceiver, filter);

        // Start discovery
        bluetoothAdapter.startDiscovery();
    }

    private void connectToDevice(BluetoothDevice device) {
        Log.d("Bluetooth", "connectToDevice called");

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_CONNECT)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.BLUETOOTH_CONNECT},
                    BLUETOOTH_REQUEST_CODE);
            return;
        }
        BluetoothSocket socket = null;
        try {
            Log.d("Bluetooth", "Attempting to connect");
            // Create a BluetoothSocket for the specified device
            socket = device.createRfcommSocketToServiceRecord(UUID.fromString("4fafc201-1fb5-459e-8fcc-c5c9c331914b")); // Replace "YOUR_UUID" with the actual UUID

            // Connect to the device
            socket.connect();

            Log.d("Bluetooth", "Connection successful");
            // Connection was successful
            Toast.makeText(this, "Successfully connected to device", Toast.LENGTH_SHORT).show();

            // Now you can use the socket for data transfer
            // For example, you can get input and output streams from the socket:
            // InputStream inputStream = socket.getInputStream();
            // OutputStream outputStream = socket.getOutputStream();

            // Handle data transfer here
        } catch (IOException e) {
            Log.d("Bluetooth", "Connection failed");
            // Connection was unsuccessful
            Toast.makeText(this, "Failed to connect to device", Toast.LENGTH_SHORT).show();

            // Handle any exceptions that occur during connection
            e.printStackTrace();
        } finally {
            try {
                // Close the socket when done
                if (socket != null) {
                    socket.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Unregister the Bluetooth receiver when the activity is destroyed
        if (bluetoothReceiver != null) {
            unregisterReceiver(bluetoothReceiver);
        }
    }
}


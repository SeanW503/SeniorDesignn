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
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.pm.PackageManager;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import android.util.Log;
import android.Manifest;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

public class settings extends AppCompatActivity {

    private static final int BLUETOOTH_REQUEST_CODE = 1;
    public BluetoothAdapter bluetoothAdapter;
    public BluetoothDevice bluetoothDevice;
    public BluetoothSocket bluetoothSocket;
    public IntentFilter intentFilter;
    //public BroadcastReceiver bluetoothReceiver;

    public InputStream inputStream;
    public OutputStream outputStream;

    public BroadcastReceiver bluetoothReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        Button connectBtn = findViewById(R.id.connectBtn);
        Button device = findViewById(R.id.getDev);

        // Initialize BluetoothAdapter
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        // Register reciever
        intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        intentFilter.addAction((BluetoothDevice.ACTION_ACL_DISCONNECTED));
        BroadcastReceiver bluetoothReceiver = null;
        registerReceiver(bluetoothReceiver, intentFilter);
        connectBtn.setEnabled(false);
        device.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (bluetoothAdapter == null) {
                    // Device does not support Bluetooth
                    Toast.makeText(settings.this, "Bluetooth is not supported on this device", Toast.LENGTH_SHORT).show();
                } else {
                    // Start discovering nearby devices
                    if (ActivityCompat.checkSelfPermission(settings.this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                        // TODO: Consider calling
                        //    ActivityCompat#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for ActivityCompat#requestPermissions for more details.
                        return;
                    }
                    Set<BluetoothDevice> devices = bluetoothAdapter.getBondedDevices();
                    for (BluetoothDevice dev : devices) {
                        if (dev.getName().equals("ProjectBox")) {
                            bluetoothDevice = dev;
                            break;
                        }
                    }
                    //startDeviceDiscovery();
                }
            }
        });

        connectBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    if (ActivityCompat.checkSelfPermission(settings.this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                        // TODO: Consider calling
                        //    ActivityCompat#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for ActivityCompat#requestPermissions for more details.
                        return;
                    }
                    bluetoothSocket = bluetoothDevice.createRfcommSocketToServiceRecord(UUID.fromString("4fafc201-1fb5-459e-8fcc-c5c9c331914b"));
                    bluetoothSocket.connect();
                    inputStream = bluetoothSocket.getInputStream();
                    outputStream = bluetoothSocket.getOutputStream();
                    Toast.makeText(getApplicationContext(), "Connected", Toast.LENGTH_LONG).show();
                } catch (Exception e) {
                }
            }
        });


        bluetoothReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                switch (intent.getAction()) {
                    case BluetoothAdapter.ACTION_DISCOVERY_FINISHED:
                        ;
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                connectBtn.setEnabled(true);
                            }
                        });
                        break;
                    case BluetoothDevice.ACTION_ACL_DISCONNECTED:
                        break;
                }
            }
        };
    }

/*
    private void startDeviceDiscovery() {
        // Check if Bluetooth is available on the device
        if (bluetoothAdapter == null) {
            Toast.makeText(settings.this, "Bluetooth is not supported on this device", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check if Bluetooth is enabled
        if (!bluetoothAdapter.isEnabled()) {
            // You can prompt the user to enable Bluetooth here
            // For simplicity, let's just show a Toast message
            Toast.makeText(settings.this, "Please enable Bluetooth", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check for BLUETOOTH_ADMIN permission
        if (ActivityCompat.checkSelfPermission(settings.this, android.Manifest.permission.BLUETOOTH_ADMIN) != PackageManager.PERMISSION_GRANTED) {
            // Request BLUETOOTH_ADMIN permission
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.BLUETOOTH_ADMIN},
                    BLUETOOTH_REQUEST_CODE);
            return;
        }

        // Check for BLUETOOTH_CONNECT permission
        if (ActivityCompat.checkSelfPermission(settings.this, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            // Request BLUETOOTH_CONNECT permission
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.BLUETOOTH_CONNECT},
                    BLUETOOTH_REQUEST_CODE);
            return;
        }

        // Check for BLUETOOTH_SCAN permission
        if (ActivityCompat.checkSelfPermission(settings.this, android.Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
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
                    if (ActivityCompat.checkSelfPermission(settings.this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                        // TODO: Consider calling
                        //    ActivityCompat#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for ActivityCompat#requestPermissions for more details.
                        return;
                    }
                    Log.d("BluetoothReceiver", "Device found: " + device.getName());
                    // Check if the discovered device is your ESP32 device
                    if (device.getName() != null && device.getName().equals("ProjectBox")) {
                        Log.d("BluetoothReceiver", "Connecting to ProjectBox");
                        // Connect to the ESP32 device
                        connectToDevice(device);
                    }
                } else {
                    Log.d("BluetoothReceiver", "Received an unexpected broadcast: " + action);
                }
            }
        };
        registerReceiver(bluetoothReceiver, filter);

        // Start discovery
        bluetoothAdapter.startDiscovery();
    }

    private void connectToDevice(BluetoothDevice device) {
        Log.d("Bluetooth", "connectToDevice called");
        Toast.makeText(settings.this, "Beginning of connect to Device", Toast.LENGTH_SHORT).show();
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

    }*/


    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Unregister the Bluetooth receiver when the activity is destroyed
        if (bluetoothReceiver != null) {
            unregisterReceiver(bluetoothReceiver);
        }
    }
}


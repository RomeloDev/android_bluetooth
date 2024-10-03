package com.example.bluetoothapprp;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {
    BluetoothAdapter bluetoothAdapter;
    Button scandev;
    ListView deviceListView;
    ArrayList<String> discoveredDevices;
    ArrayAdapter<String> arrayAdapter;
    BluetoothSocket bluetoothSocket;

    // UUID to create a connection
    private static final UUID MY_UUID = UUID.randomUUID();

    @SuppressLint({"MissingPermission", "WrongViewCast"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            v.setPadding(insets.getInsets(WindowInsetsCompat.Type.systemBars()).left,
                    insets.getInsets(WindowInsetsCompat.Type.systemBars()).top,
                    insets.getInsets(WindowInsetsCompat.Type.systemBars()).right,
                    insets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom);
            return insets;
        });

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        Button enableBluetoothBtn = findViewById(R.id.btnOn);
        Button disableBluetoothBtn = findViewById(R.id.btnOff);
        Button sendFileBtn = findViewById(R.id.btnsendfile);
        scandev = findViewById(R.id.btnDevices);
        deviceListView = findViewById(R.id.device_list);

        discoveredDevices = new ArrayList<>();
        arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, discoveredDevices);
        deviceListView.setAdapter(arrayAdapter);

        enableBluetoothBtn.setOnClickListener(view -> enableBluetooth());
        disableBluetoothBtn.setOnClickListener(view -> disableBluetooth());
        scandev.setOnClickListener(view -> startDeviceDiscovery());

        // Send file button action
        sendFileBtn.setOnClickListener(view -> {
            if (bluetoothSocket != null && bluetoothSocket.isConnected()) {
                sendFile();  // Send file when Bluetooth connection is established
            } else {
                Toast.makeText(MainActivity.this, "Not connected to any device", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Enabling Bluetooth
    @SuppressLint("MissingPermission")
    private void enableBluetooth() {
        if (bluetoothAdapter != null && !bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, 1); // 1 is requestCode
            Toast.makeText(MainActivity.this, "Enabling Bluetooth...", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(MainActivity.this, "Bluetooth is already enabled", Toast.LENGTH_SHORT).show();
        }
    }

    // Disabling Bluetooth
    @SuppressLint("MissingPermission")
    private void disableBluetooth() {
        if (bluetoothAdapter != null && bluetoothAdapter.isEnabled()) {
            bluetoothAdapter.disable();
            Toast.makeText(MainActivity.this, "Bluetooth Disabled", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(MainActivity.this, "Bluetooth is already disabled", Toast.LENGTH_SHORT).show();
        }
    }

    // Start Bluetooth device discovery
    @SuppressLint("MissingPermission")
    private void startDeviceDiscovery() {
        discoveredDevices.clear();
        arrayAdapter.notifyDataSetChanged();

        if (bluetoothAdapter != null && bluetoothAdapter.isEnabled()) {
            IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
            registerReceiver(receiver, filter);

            if (bluetoothAdapter.isDiscovering()) {
                bluetoothAdapter.cancelDiscovery();
            }
            bluetoothAdapter.startDiscovery();
            Toast.makeText(MainActivity.this, "Discovery started...", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(MainActivity.this, "Please enable Bluetooth", Toast.LENGTH_SHORT).show();
        }
    }

    // BroadcastReceiver to handle discovered Bluetooth devices
    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @SuppressLint("MissingPermission")
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                String deviceName = device.getName();
                String deviceAddress = device.getAddress();

                if (deviceName != null) {
                    discoveredDevices.add(deviceName + " (" + deviceAddress + ")");
                } else {
                    discoveredDevices.add("Unknown Device (" + deviceAddress + ")");
                }
                arrayAdapter.notifyDataSetChanged();
            }
        }
    };

    // Method to send a file via Bluetooth
    @SuppressLint("MissingPermission")
    private void sendFile() {
        try {
            // Specify the file to send (replace this path with your file)
            File file = new File(Environment.getExternalStorageDirectory(), "file_to_send.txt");
            FileInputStream fis = new FileInputStream(file);
            BufferedInputStream bis = new BufferedInputStream(fis);

            OutputStream outputStream = bluetoothSocket.getOutputStream();
            byte[] buffer = new byte[1024];
            int bytesRead;

            // Read the file and write it to the Bluetooth output stream
            while ((bytesRead = bis.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }

            bis.close();
            outputStream.flush();
            Toast.makeText(MainActivity.this, "File sent successfully", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(MainActivity.this, "Failed to send file", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
        if (bluetoothSocket != null) {
            try {
                bluetoothSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
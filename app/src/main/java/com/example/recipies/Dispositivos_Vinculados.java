package com.example.recipies;

import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Set;

public class Dispositivos_Vinculados extends AppCompatActivity {
    private BluetoothAdapter bluetoothAdapter;
    private ListView listaBT;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dispositivos_vinculados);

        listaBT = findViewById(R.id.ListaBT);

        // Obtener el adaptador Bluetooth
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            // El dispositivo no admite Bluetooth
            // Maneja este caso según tus necesidades
            return;
        }

        // Obtener la lista de dispositivos Bluetooth vinculados
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();

        ArrayList<String> deviceList = new ArrayList<>();

        for (BluetoothDevice device : pairedDevices) {
            // Agregar dispositivos emparejados a la lista
            deviceList.add(device.getName() + "\n" + device.getAddress());
        }

        // Mostrar dispositivos vinculados en el ListView
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, deviceList);
        listaBT.setAdapter(adapter);

        listaBT.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String deviceInfo = listaBT.getItemAtPosition(position).toString();
                String deviceAddress = deviceInfo.substring(deviceInfo.length() - 17); // Obtiene la dirección MAC del texto

                Intent resultIntent = new Intent();
                resultIntent.putExtra("DEVICE_ADDRESS", deviceAddress);
                setResult(RESULT_OK, resultIntent);
                finish(); // Cierra la actividad Dispositivos_Vinculados
            }
        });


    }
}
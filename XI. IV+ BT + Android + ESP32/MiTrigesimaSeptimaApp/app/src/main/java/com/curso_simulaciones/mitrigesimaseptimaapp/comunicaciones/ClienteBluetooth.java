package com.curso_simulaciones.mitrigesimaseptimaapp.comunicaciones;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.Context;

import com.curso_simulaciones.mitrigesimaseptimaapp.datos.AlmacenDatosRAM;

import java.util.UUID;

@SuppressLint("MissingPermission")
public class ClienteBluetooth {

    public BluetoothAdapter adaptadorBluetooth;
    public BluetoothDevice dispositivo;
    public BluetoothGatt bluetoothGatt;
    public BluetoothGattCharacteristic caracteristicaEscritura;

    // Los UUIDs EXACTOS que configuramos en el ESP32-S3
    public static final UUID SERVICE_UUID = UUID.fromString("4fafc201-1fb5-459e-8fcc-c5c9c331914b");
    public static final UUID CHARACTERISTIC_UUID = UUID.fromString("beb5483e-36e1-4688-b7f5-ea07361b26a8");

    public ClienteBluetooth() {
    }

    // 1. Inicializar el dispositivo a partir de la MAC
    public void abrirSocketCliente(String direccion) {
        adaptadorBluetooth = BluetoothAdapter.getDefaultAdapter();
        dispositivo = adaptadorBluetooth.getRemoteDevice(direccion);
    }

    // 2. Conectar mediante GATT
    public void conectarSocketCliente(Context contexto) {
        if (dispositivo != null) {
            bluetoothGatt = dispositivo.connectGatt(contexto, false, gattCallback);
        }
    }

    // Callback que maneja los eventos asíncronos de BLE
    private final BluetoothGattCallback gattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                AlmacenDatosRAM.conexion_bluetooth = "  Conectado con " + gatt.getDevice().getName();
                // Vital: Al conectar, debemos descubrir los servicios del ESP32
                gatt.discoverServices();
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                AlmacenDatosRAM.conexion_bluetooth = "  Desconectado...";
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                // Buscamos el servicio
                BluetoothGattService servicio = gatt.getService(SERVICE_UUID);
                if (servicio != null) {
                    // Vinculamos la característica donde vamos a escribir
                    caracteristicaEscritura = servicio.getCharacteristic(CHARACTERISTIC_UUID);
                }
            }
        }
    };

    // 3. Abrimos los flujos, pero los dejamos vacíos para no romper tu Actividad principal
    public void abrirFlujoEntrada() {}
    public void abrirFlujoSalida() {}

    // 4. Escribir los bytes directamente en la característica BLE
    public void escribirBytes(byte[] datoByteParaEnviar) {
        if (bluetoothGatt != null && caracteristicaEscritura != null && datoByteParaEnviar != null) {
            // Guardamos el JSON en la característica
            caracteristicaEscritura.setValue(datoByteParaEnviar);
            // Ejecutamos la escritura hacia el ESP32
            bluetoothGatt.writeCharacteristic(caracteristicaEscritura);
        }
    }

    // 5. Cerrar la conexión GATT
    public void cerrarSocketCliente() {
        if (bluetoothGatt != null) {
            bluetoothGatt.disconnect();
            bluetoothGatt.close();
            bluetoothGatt = null;
        }
    }

    public void cerrarFlujoEntrada() {}
    public void cerrarFlujoSalida() {}

}

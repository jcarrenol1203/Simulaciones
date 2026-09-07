package com.curso_simulaciones.mitrigesimaoctavaapp.comunicaciones;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.Context;

import com.curso_simulaciones.mitrigesimaoctavaapp.datos.AlmacenDatosRAM;

import java.util.UUID;

@SuppressLint("MissingPermission")
public class ClienteBluetooth {

    public BluetoothAdapter adaptadorBluetooth;
    public BluetoothDevice dispositivo;
    public BluetoothGatt bluetoothGatt;
    public BluetoothGattCharacteristic caracteristicaLectura;

    // Los UUIDs EXACTOS que configuramos en el ESP32-S3
    public static final UUID SERVICE_UUID = UUID.fromString("4fafc201-1fb5-459e-8fcc-c5c9c331914b");
    public static final UUID CHARACTERISTIC_UUID = UUID.fromString("beb5483e-36e1-4688-b7f5-ea07361b26a8");

    // UUID estándar de BLE para activar las notificaciones (Descriptor Client Characteristic Configuration)
    public static final UUID DESCRIPTOR_UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");

    // Variable para almacenar temporalmente el último JSON recibido
    private String ultimoDatoRecibido = null;

    public ClienteBluetooth() {
    }

    public void abrirSocketCliente(String direccion) {
        adaptadorBluetooth = BluetoothAdapter.getDefaultAdapter();
        dispositivo = adaptadorBluetooth.getRemoteDevice(direccion);
    }

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

                // LA SOLUCIÓN: Solicitamos un paquete más grande (128 bytes) ANTES de buscar servicios
                gatt.requestMtu(128);

            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                AlmacenDatosRAM.conexion_bluetooth = "  Desconectado...";
            }
        }

        // Agregamos este evento que se dispara automáticamente cuando Android nos aprueba el nuevo tamaño
        @Override
        public void onMtuChanged(BluetoothGatt gatt, int mtu, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                // Ahora sí, con la "tubería ensanchada", descubrimos los servicios
                gatt.discoverServices();
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                BluetoothGattService servicio = gatt.getService(SERVICE_UUID);
                if (servicio != null) {
                    caracteristicaLectura = servicio.getCharacteristic(CHARACTERISTIC_UUID);

                    if (caracteristicaLectura != null) {
                        // 1. Activamos las notificaciones internamente en Android
                        gatt.setCharacteristicNotification(caracteristicaLectura, true);

                        // 2. Le avisamos al ESP32 que empiece a enviar datos escribiendo en su Descriptor 2902
                        BluetoothGattDescriptor descriptor = caracteristicaLectura.getDescriptor(DESCRIPTOR_UUID);
                        if (descriptor != null) {
                            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                            gatt.writeDescriptor(descriptor);
                        }
                    }
                }
            }
        }

        // Este método es la ESTRELLA: Se dispara solo y automáticamente cada 100ms
        // cuando el ESP32 envía un nuevo dato.
        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            byte[] data = characteristic.getValue();
            if (data != null && data.length > 0) {
                // Guardamos el JSON recibido para que la Actividad lo pueda leer
                ultimoDatoRecibido = new String(data);
            }
        }
    };

    // Este es el método que tu Actividad está llamando constantemente en su ciclo run()
    public String leerString() {
        String dato = ultimoDatoRecibido;
        ultimoDatoRecibido = null; // Lo borramos después de leerlo para no procesar el mismo dato dos veces
        return dato;
    }

    public void abrirFlujoEntrada() {}
    public void abrirFlujoSalida() {}

    // Ya no usamos este método en esta app, pero lo dejamos vacío por si la Actividad lo llama
    public void escribirBytes(byte[] datoByteParaEnviar) {}

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
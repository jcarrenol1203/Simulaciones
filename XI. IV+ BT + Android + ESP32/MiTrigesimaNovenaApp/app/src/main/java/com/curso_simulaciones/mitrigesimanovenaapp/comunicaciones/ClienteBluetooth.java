package com.curso_simulaciones.mitrigesimanovenaapp.comunicaciones;

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

import com.curso_simulaciones.mitrigesimanovenaapp.datos.AlmacenDatosRAM;

import java.util.UUID;

@SuppressLint("MissingPermission")
public class ClienteBluetooth {

    public BluetoothAdapter adaptadorBluetooth;
    public BluetoothDevice dispositivo;
    public BluetoothGatt bluetoothGatt;

    // Necesitamos DOS características ahora: una para escuchar y otra para hablar
    public BluetoothGattCharacteristic caracteristicaLectura;
    public BluetoothGattCharacteristic caracteristicaEscritura;

    // Los UUIDs EXACTOS del ESP32-S3
    public static final UUID SERVICE_UUID = UUID.fromString("4fafc201-1fb5-459e-8fcc-c5c9c331914b");
    // UUID para ESCUCHAR la distancia (el ESP32 notifica por aquí)
    public static final UUID CHARACTERISTIC_TX_UUID = UUID.fromString("71850116-2917-48f8-a145-c38d8f0f0312");
    // UUID para ENVIAR los colores (el celular escribe aquí)
    public static final UUID CHARACTERISTIC_RX_UUID = UUID.fromString("beb5483e-36e1-4688-b7f5-ea07361b26a8");

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
                    // 1. Enlazamos la característica de LECTURA (distancia)
                    caracteristicaLectura = servicio.getCharacteristic(CHARACTERISTIC_TX_UUID);
                    // 2. Enlazamos la característica de ESCRITURA (colores)
                    caracteristicaEscritura = servicio.getCharacteristic(CHARACTERISTIC_RX_UUID);

                    // Suscribirnos para recibir datos del HC-SR04
                    if (caracteristicaLectura != null) {
                        gatt.setCharacteristicNotification(caracteristicaLectura, true);

                        BluetoothGattDescriptor descriptor = caracteristicaLectura.getDescriptor(DESCRIPTOR_UUID);
                        if (descriptor != null) {
                            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                            gatt.writeDescriptor(descriptor);
                        }
                    }
                }
            }
        }

        // Atrapa los datos que manda el HC-SR04
        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            // Verificamos que el dato provenga de la característica correcta
            if (characteristic.getUuid().equals(CHARACTERISTIC_TX_UUID)) {
                byte[] data = characteristic.getValue();
                if (data != null && data.length > 0) {
                    ultimoDatoRecibido = new String(data);
                }
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

    // Método ayudante para enviar el JSON del color directo, como String
    public void escribirString(String datoString) {
        if (datoString != null) {
            escribirBytes(datoString.getBytes());
        }
    }

    public void escribirBytes(byte[] datoByteParaEnviar) {
        if (bluetoothGatt != null && caracteristicaEscritura != null && datoByteParaEnviar != null) {
            caracteristicaEscritura.setValue(datoByteParaEnviar);
            bluetoothGatt.writeCharacteristic(caracteristicaEscritura);
        }
    }

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
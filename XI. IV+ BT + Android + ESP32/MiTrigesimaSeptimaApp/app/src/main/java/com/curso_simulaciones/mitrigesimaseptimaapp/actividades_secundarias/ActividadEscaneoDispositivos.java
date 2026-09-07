package com.curso_simulaciones.mitrigesimaseptimaapp.actividades_secundarias;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.ParcelUuid;
import android.widget.Toast;

import com.curso_simulaciones.mitrigesimaseptimaapp.comunicaciones.ClienteBluetooth;
import com.curso_simulaciones.mitrigesimaseptimaapp.datos.AlmacenDatosRAM;

import java.util.Collections;

/*
 El ESP32 del curso solo anuncia por BLE (BLEAdvertising), no participa del
 inquiry scan de Bluetooth clásico (BluetoothAdapter.startDiscovery) que usaba
 ScannerBluetooth. Por eso aquí se escanea con BluetoothLeScanner, filtrando
 directamente por el SERVICE_UUID del ESP32 (el mismo que ya usa ClienteBluetooth
 para conectar), y en cuanto aparece se auto-selecciona: solo hay un ESP32 en
 este montaje, así que no hace falta mostrar una lista.
 */
@SuppressLint("MissingPermission")
public class ActividadEscaneoDispositivos extends Activity {

    private static final long TIEMPO_MAXIMO_ESCANEO_MS = 10000;

    private BluetoothLeScanner escanerBLE;
    private final Handler manejador = new Handler(Looper.getMainLooper());
    private String direccionEncontrada;
    private boolean escaneando = false;

    private final Runnable avisoTimeout = new Runnable() {
        @Override
        public void run() {
            if (escaneando) {
                Toast.makeText(ActividadEscaneoDispositivos.this,
                        "No se encontró el ESP32 (PhysicsESP32_BLE)", Toast.LENGTH_SHORT).show();
                detenerEscaneo();
                finish();
            }
        }
    };

    private final ScanCallback callbackEscaneo = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            BluetoothDevice dispositivo = result.getDevice();
            direccionEncontrada = dispositivo.getAddress();
            detenerEscaneo();
            finish();
        }

        @Override
        public void onScanFailed(int errorCode) {
            Toast.makeText(ActividadEscaneoDispositivos.this,
                    "Falló el escaneo BLE (código " + errorCode + ")", Toast.LENGTH_SHORT).show();
            detenerEscaneo();
            finish();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        iniciarEscaneoBLE();
    }

    private void iniciarEscaneoBLE() {
        BluetoothAdapter adaptador = BluetoothAdapter.getDefaultAdapter();
        if (adaptador == null || !adaptador.isEnabled()) {
            Toast.makeText(this, "Activa el Bluetooth primero", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        escanerBLE = adaptador.getBluetoothLeScanner();

        ScanFilter filtroPorServicioESP32 = new ScanFilter.Builder()
                .setServiceUuid(new ParcelUuid(ClienteBluetooth.SERVICE_UUID))
                .build();

        ScanSettings ajustesEscaneo = new ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                .build();

        escaneando = true;
        escanerBLE.startScan(Collections.singletonList(filtroPorServicioESP32), ajustesEscaneo, callbackEscaneo);
        manejador.postDelayed(avisoTimeout, TIEMPO_MAXIMO_ESCANEO_MS);
    }

    private void detenerEscaneo() {
        if (escaneando && escanerBLE != null) {
            escanerBLE.stopScan(callbackEscaneo);
        }
        escaneando = false;
        manejador.removeCallbacks(avisoTimeout);
    }

    @Override
    public void onPause() {
        super.onPause();
        detenerEscaneo();
        AlmacenDatosRAM.direccion = direccionEncontrada;
        AlmacenDatosRAM.conexion_bluetooth = direccionEncontrada;
    }

}

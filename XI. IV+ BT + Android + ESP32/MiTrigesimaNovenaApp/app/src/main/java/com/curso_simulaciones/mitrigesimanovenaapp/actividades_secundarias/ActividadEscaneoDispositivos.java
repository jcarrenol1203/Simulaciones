package com.curso_simulaciones.mitrigesimanovenaapp.actividades_secundarias;

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
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.curso_simulaciones.mitrigesimanovenaapp.datos.AlmacenDatosRAM;

import java.util.ArrayList;
import java.util.List;

/*
 El ESP32 del curso solo anuncia por BLE (BLEAdvertising), no participa del
 inquiry scan de Bluetooth clásico (BluetoothAdapter.startDiscovery) que usa
 ScannerBluetooth en la guía. Por eso aquí se escanea con BluetoothLeScanner y
 se muestra en vivo la lista de dispositivos BLE detectados, sin filtrar por
 UUID -- así, si hay varios ESP32 con el mismo firmware/UUID cerca (p. ej. en
 un salón de clase), el estudiante puede identificar el suyo por nombre o
 dirección MAC y elegirlo manualmente, en vez de conectarse al primero que
 aparezca.
 */
@SuppressLint("MissingPermission")
public class ActividadEscaneoDispositivos extends Activity {

    private BluetoothLeScanner escanerBLE;
    private boolean escaneando = false;

    private ArrayAdapter<String> adaptador;
    private final List<BluetoothDevice> dispositivosEncontrados = new ArrayList<>();
    private final List<String> textosMostrados = new ArrayList<>();

    private final ScanCallback callbackEscaneo = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            BluetoothDevice dispositivo = result.getDevice();
            if (dispositivosEncontrados.contains(dispositivo)) {
                return;
            }
            dispositivosEncontrados.add(dispositivo);

            String nombre = dispositivo.getName();
            textosMostrados.add((nombre != null ? nombre : "(sin nombre)") + "\n" + dispositivo.getAddress());

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    adaptador.notifyDataSetChanged();
                }
            });
        }

        @Override
        public void onScanFailed(int errorCode) {
            Toast.makeText(ActividadEscaneoDispositivos.this,
                    "Falló el escaneo BLE (código " + errorCode + ")", Toast.LENGTH_SHORT).show();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(crearGUI());
        iniciarEscaneoBLE();
    }

    private LinearLayout crearGUI() {
        LinearLayout contenedor = new LinearLayout(this);
        contenedor.setOrientation(LinearLayout.VERTICAL);

        TextView titulo = new TextView(this);
        titulo.setText("Buscando dispositivos BLE cercanos...\nToca el ESP32 (PhysicsESP32_BLE) cuando aparezca:");
        titulo.setPadding(20, 20, 20, 20);
        contenedor.addView(titulo);

        ListView listaDispositivos = new ListView(this);
        adaptador = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, textosMostrados);
        listaDispositivos.setAdapter(adaptador);
        listaDispositivos.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        listaDispositivos.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                seleccionarDispositivo(dispositivosEncontrados.get(position));
            }
        });
        contenedor.addView(listaDispositivos);

        return contenedor;
    }

    private void iniciarEscaneoBLE() {
        BluetoothAdapter adaptadorBT = BluetoothAdapter.getDefaultAdapter();
        if (adaptadorBT == null || !adaptadorBT.isEnabled()) {
            Toast.makeText(this, "Activa el Bluetooth primero", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        escanerBLE = adaptadorBT.getBluetoothLeScanner();

        ScanSettings ajustesEscaneo = new ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                .build();

        // Sin ScanFilter: mostramos todos los BLE cercanos, no solo el ESP32,
        // para poder distinguir entre varios ESP32 con el mismo UUID.
        List<ScanFilter> sinFiltro = new ArrayList<>();

        escaneando = true;
        escanerBLE.startScan(sinFiltro, ajustesEscaneo, callbackEscaneo);
    }

    private void seleccionarDispositivo(BluetoothDevice dispositivo) {
        detenerEscaneo();
        AlmacenDatosRAM.direccion = dispositivo.getAddress();
        AlmacenDatosRAM.conexion_bluetooth = dispositivo.getAddress();
        finish();
    }

    private void detenerEscaneo() {
        if (escaneando && escanerBLE != null) {
            escanerBLE.stopScan(callbackEscaneo);
        }
        escaneando = false;
    }

    @Override
    public void onPause() {
        super.onPause();
        detenerEscaneo();
    }

}

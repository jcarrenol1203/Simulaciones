package com.curso_simulaciones.mitrigesimasextaapp.actividades_secundarias;

import android.app.Activity;
import android.os.Bundle;

import com.curso_simulaciones.mitrigesimasextaapp.datos.AlmacenDatosRAM;
import com.example.comunicaciones.ScannerBluetooth;

public class ActividadEscaneoDispositivos extends Activity {


    ScannerBluetooth scanear;
    //@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        scanear=new ScannerBluetooth(this);
        scanear.descubriendoDispositivos();
    }

    @Override
    public void onPause() {
        super.onPause();
        AlmacenDatosRAM.direccion= scanear.direccion;
    }


}
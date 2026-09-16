package com.curso_simulaciones.micuadragesimasegundaapp.utilidades;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

public class Magnetometro extends GaugeSimple implements SensorEventListener {
    private SensorManager sensorManager;
    private int componenteMagnetico = 4;

    public Magnetometro(Context context) {
        super(context);
        //estado inicial
        setComponenteMagnetico(componenteMagnetico);
    }

    public void setComponenteMagnetico(int componenteMagnetico) {
        this.componenteMagnetico = componenteMagnetico;
        if (componenteMagnetico == 1) {
            this.setUnidades("Bx (uT)");
            this.setRango(-100, 100);
        }
        if (componenteMagnetico == 2) {
            this.setUnidades("By (uT)");
            this.setRango(-100, 100);
        }
        if (componenteMagnetico == 3) {
            this.setUnidades("Bz (uT)");
            this.setRango(-100, 100);
        }
        if (componenteMagnetico == 4) {
            this.setUnidades("B (uT)");
            this.setRango(0, 100);
        }
    }

    public void captarSensor(Context context) {
        //captamos el servicio del sensor
        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD), SensorManager.SENSOR_DELAY_FASTEST);
    }

    //se activa sólo cuando hay cambios
    public void onSensorChanged(SensorEvent event) {
        //en x
        float m = 0;
        float medida_x = 0;
        float medida_y = 0;
        float medida_z = 0;
        float medida = 0;
        medida_x = event.values[SensorManager.DATA_X];
        medida_y = event.values[SensorManager.DATA_Y];
        medida_z = event.values[SensorManager.DATA_Z];
        float resultado = medida_x * medida_x + medida_y * medida_y + medida_z * medida_z;
        m = (float) (Math.sqrt(resultado));
        if (componenteMagnetico == 1) {
            medida = medida_x;
            this.setUnidades(" Bx (uT)");
        }
        if (componenteMagnetico == 2) {
            medida = medida_y;
            this.setUnidades(" By (uT)");
        }
        if (componenteMagnetico == 3) {
            medida = medida_z;
            this.setUnidades(" Bz (uT)");
        }
        if (componenteMagnetico == 4) {
            medida = m;
            this.setUnidades(" B (uT)");
        }
        //un decimal
        medida = (float) (Math.round(medida * 10) / 10.0f);
        cambiarEscala(medida);
        this.setMedida(medida);
    }

    /*
     * Reescala dinámicamente el rango del gauge según la magnitud medida.
     * Para las componentes (Bx, By, Bz) el rango es simétrico [-max, max];
     * para la magnitud total (B) el rango va de 0 a max.
     */
    public void cambiarEscala(float medida) {
        float valorAbsoluto = Math.abs(medida);
        float maximo;
        if (valorAbsoluto <= 100f) {
            maximo = 100f;
        } else if (valorAbsoluto <= 250f) {
            maximo = 250f;
        } else if (valorAbsoluto <= 500f) {
            maximo = 500f;
        } else if (valorAbsoluto <= 1000f) {
            maximo = 1000f;
        } else if (valorAbsoluto <= 2000f) {
            maximo = 2000f;
        } else {
            maximo = 5000f;
        }
        float minimo = (componenteMagnetico == 4) ? 0f : -maximo;
        this.setRango(minimo, maximo);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }
}

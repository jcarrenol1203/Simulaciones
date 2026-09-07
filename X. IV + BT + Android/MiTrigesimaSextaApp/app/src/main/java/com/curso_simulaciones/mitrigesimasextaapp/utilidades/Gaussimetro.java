package com.curso_simulaciones.mitrigesimasextaapp.utilidades;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

public class Gaussimetro extends GaugeSimple implements SensorEventListener {

    private SensorManager sensorManager;
    private int componenteGaussimetro = 4;

    public Gaussimetro(Context context) {
        super(context);

        //estado inicial
        setComponenteGaussimetro(componenteGaussimetro);

    }


    public void setComponenteGaussimetro(int componenteGaussimetro) {

        this.componenteGaussimetro = componenteGaussimetro;

        if (componenteGaussimetro == 1) {
            this.setUnidades(" Bx (µT)");}

        if (componenteGaussimetro == 2) {
            this.setUnidades(" By (µT)");}

        if (componenteGaussimetro == 3) {
            this.setUnidades(" Bz (µT)");}

        if (componenteGaussimetro == 4) {
            this.setUnidades(" B (µT)");}
    }


    public void captarSensor(Context context) {

        //captamos el servicio del sensor
        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD), SensorManager.SENSOR_DELAY_FASTEST);

    }

    //se activa sólo cuando hay cambios
    public void onSensorChanged(SensorEvent event) {

        //en x
        float b = 0;
        float medida_x = 0;
        float medida_y = 0;
        float medida_z = 0;
        float medida = 0;

        medida_x = event.values[SensorManager.DATA_X];
        medida_y = event.values[SensorManager.DATA_Y];
        medida_z = event.values[SensorManager.DATA_Z];

        float resultado = medida_x * medida_x + medida_y * medida_y + medida_z * medida_z;
        b = (float) (Math.sqrt(resultado));

        if (componenteGaussimetro == 1) {

            medida = medida_x;
            this.setUnidades(" Bx (µT)");

        }

        if (componenteGaussimetro == 2) {

            medida = medida_y;
            this.setUnidades(" By (µT)");

        }

        if (componenteGaussimetro == 3) {

            medida = medida_z;
            this.setUnidades(" Bz (µT)");

        }

        if (componenteGaussimetro == 4) {

            medida = b;
            this.setUnidades(" B (µT)");

        }

        //dos decimales
        medida = (float) (Math.round(medida * 100) / 100.0f);
        this.setMedida(medida);
        cambiarEscala(medida);

        //almacenar dato actual

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }


    /*
    Bx, By y Bz pueden ser negativas (rango simétrico -maximo..maximo);
    B (magnitud) siempre es positiva (rango 0..maximo)
    */
    public void cambiarEscala(float medida) {

        float valorAbsoluto = Math.abs(medida);
        float maximo;

        if (valorAbsoluto <= 100f) {

            maximo = 100f;

        } else if (valorAbsoluto <= 200f) {

            maximo = 200f;

        } else if (valorAbsoluto <= 500f) {

            maximo = 500f;

        } else if (valorAbsoluto <= 1000f) {

            maximo = 1000f;

        } else if (valorAbsoluto <= 5000f) {

            maximo = 5000f;

        } else {

            maximo = 10000f;

        }

        if (componenteGaussimetro == 4) {

            this.setRango(0f, maximo);

        } else {

            this.setRango(-maximo, maximo);

        }

    }

}

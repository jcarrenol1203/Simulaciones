package com.curso_simulaciones.mitrigesimasextaapp.utilidades;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

public class Acelerometro extends GaugeSimple implements SensorEventListener {

    private SensorManager sensorManager;
    private int componenteAceleracion = 4;

    public Acelerometro(Context context) {
        super(context);
        //estado inicial
        setComponenteAcelerometro(componenteAceleracion);

    }


    public void setComponenteAcelerometro(int componenteAceleracion) {

        this.componenteAceleracion = componenteAceleracion;

        if (componenteAceleracion ==1) {
            this.setUnidades("ax (m/s^2)");}

        if (componenteAceleracion ==2) {
            this.setUnidades("ay (m/s^2)");}

        if (componenteAceleracion ==3) {
            this.setUnidades("az (m/s^2)");}

        if (componenteAceleracion ==4) {
            this.setUnidades("a (m/s^2)");}



    }

    public void captarSensor(Context context) {

        //captamos el servicio del sensor
        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_FASTEST);

    }

    //se activa sólo cuando hay cambios
    public void onSensorChanged(SensorEvent event) {

        //en x
        float a = 0;
        float medida_x = 0;
        float medida_y = 0;
        float medida_z = 0;
        float medida = 0;

        medida_x = event.values[SensorManager.DATA_X];
        medida_y = event.values[SensorManager.DATA_Y];
        medida_z = event.values[SensorManager.DATA_Z];
        float resultado = medida_x * medida_x + medida_y * medida_y + medida_z * medida_z;
        a = (float) (Math.sqrt(resultado));


        if (componenteAceleracion == 1) {
            medida = medida_x;
            this.setUnidades(" ax (m/S2)");
        }
        if (componenteAceleracion == 2){
            medida = medida_y;
            this.setUnidades(" ay (m/S2)");
        }
        if (componenteAceleracion == 3){
            medida = medida_z;
            this.setUnidades(" az (m/S2)");
        }
        if (componenteAceleracion == 4){
            medida = a;
            this.setUnidades(" a (m/S2)");
        }

        //un decimal
        medida = (float) (Math.round(medida * 10) / 10.0f);
        this.setMedida(medida);
        cambiarEscala(medida);

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }


    /*
    ax, ay y az pueden ser negativas (rango simétrico -maximo..maximo);
    a (magnitud) siempre es positiva (rango 0..maximo)
    */
    public void cambiarEscala(float medida) {

        float valorAbsoluto = Math.abs(medida);
        float maximo;

        if (valorAbsoluto <= 10f) {

            maximo = 10f;

        } else if (valorAbsoluto <= 20f) {

            maximo = 20f;

        } else if (valorAbsoluto <= 50f) {

            maximo = 50f;

        } else if (valorAbsoluto <= 100f) {

            maximo = 100f;

        } else {

            maximo = 200f;

        }

        if (componenteAceleracion == 4) {

            this.setRango(0f, maximo);

        } else {

            this.setRango(-maximo, maximo);

        }

    }

}

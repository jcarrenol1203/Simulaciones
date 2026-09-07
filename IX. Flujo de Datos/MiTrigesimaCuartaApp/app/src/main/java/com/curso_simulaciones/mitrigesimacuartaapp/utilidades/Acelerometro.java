package com.curso_simulaciones.mitrigesimacuartaapp.utilidades;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import com.curso_simulaciones.mitrigesimacuartaapp.datos.AlmacenDatosRAM;

public class Acelerometro extends GaugeSimple implements SensorEventListener {

    private SensorManager sensorManager;

    public Acelerometro(Context context) {
        super(context);
        captarSensor(context);
        //rango inicial; se reescala dinámicamente en cada lectura (cambiarEscala)
        this.setRango(0, 20);
    }

    private void captarSensor(Context context) {
        //captamos el servicio del sensor
        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_FASTEST);
    }

    //se activa sólo cuando hay cambios
    public void onSensorChanged(SensorEvent event) {

        float medida_x = event.values[SensorManager.DATA_X];
        float medida_y = event.values[SensorManager.DATA_Y];
        float medida_z = event.values[SensorManager.DATA_Z];

        float resultado = medida_x * medida_x + medida_y * medida_y + medida_z * medida_z;
        float a = (float) (Math.sqrt(resultado));

        //el gauge siempre muestra la magnitud resultante
        this.setUnidades(" a (m/S2)");
        float medida = (float) (Math.round(a * 10) / 10.0f);
        cambiarEscala(medida);
        this.setMedida(medida);

        //almacenar las cuatro cifras: se necesitan todas para la tabla y el archivo,
        //no solo la que se ve en el gauge
        AlmacenDatosRAM.ax = medida_x;
        AlmacenDatosRAM.ay = medida_y;
        AlmacenDatosRAM.az = medida_z;
        AlmacenDatosRAM.a = a;
        AlmacenDatosRAM.datoActual = medida;

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    /*
      Reescala dinámicamente el gauge según la magnitud de la aceleración,
      igual que hace Gaussimetro.cambiarEscala() en MiVigesimaNovenaApp (módulo VIII).
      "a" nunca es negativo (es una magnitud, sqrt de una suma de cuadrados),
      por eso el rango va de 0 a "maximo" en vez de ser simétrico.
     */
    private void cambiarEscala(float medida) {
        float valorAbsoluto = Math.abs(medida);
        float maximo;
        if (valorAbsoluto <= 20f) {
            maximo = 20f;
        } else if (valorAbsoluto <= 50f) {
            maximo = 50f;
        } else if (valorAbsoluto <= 100f) {
            maximo = 100f;
        } else if (valorAbsoluto <= 200f) {
            maximo = 200f;
        } else {
            maximo = 500f;
        }
        this.setRango(0f, maximo);
    }

}

package com.curso_simulaciones.mitrigesimanovenaapp.utilidades;

import android.content.Context;

public class Distanciometro extends GaugeSimple {

    public Distanciometro(Context context) {
        super(context);
    }

    /**
     * Reajusta el rango del gauge según la magnitud de la distancia medida,
     * para que la aguja siempre se mueva dentro de una escala legible en vez
     * de quedarse comprimida contra un extremo.
     *
     * @param medida
     */
    public void cambiarEscala(float medida) {
        float maximo = 20f;
        float minimo = 0f;

        if (medida > 0 && ((medida < 20) ^ (medida == 20))) {
            maximo = 20f;
            minimo = 0f;
        }
        if (medida > 20 && ((medida < 50) ^ (medida == 50f))) {
            maximo = 50f;
            minimo = 0f;
        }
        if (medida > 50 && ((medida < 100) ^ (medida == 100f))) {
            maximo = 100f;
            minimo = 0f;
        }
        if (medida > 100 && ((medida < 200) ^ (medida == 200f))) {
            maximo = 200f;
            minimo = 0f;
        }
        if (medida > 200 && ((medida < 300) ^ (medida == 300f))) {
            maximo = 300f;
            minimo = 0f;
        }
        if (medida > 300 && ((medida < 600) ^ (medida == 600f))) {
            maximo = 600f;
            minimo = 0f;
        }

        this.setRango(minimo, maximo);
    }
}

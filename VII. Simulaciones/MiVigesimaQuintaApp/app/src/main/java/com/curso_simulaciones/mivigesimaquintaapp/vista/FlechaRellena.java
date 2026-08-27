package com.curso_simulaciones.mivigesimaquintaapp.vista;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;

import com.curso_simulaciones.simulphysics.objetos_laboratorio.ObjetoLaboratorio;

/*
 Flecha con la punta RELLENA (un triángulo sólido), a diferencia de
 la Flecha de la librería simulphysics, cuya punta es un contorno en
 forma de "V" sin relleno. Se necesitaba solo para el indicador del
 eje Y (Figura 12B); no toca la librería compartida.
 */
public class FlechaRellena extends ObjetoLaboratorio {

    private float longitud;
    private float posicionAngularPropia;

    //tamaño de la punta: FIJO (no proporcional a longitud), para que
    //una flecha larga no termine con un triángulo gigante
    private float tamanoCabeza;

    public FlechaRellena(float posicionX, float posicionY, float longitud) {
        this.posicionX = posicionX;
        this.posicionY = posicionY;
        this.longitud = longitud;
        this.tamanoCabeza = 0.14f * longitud; //valor por defecto, por si no se llama setTamanoCabeza(...)
    }

    public void rotar(float posicionAngular) {
        this.posicionAngularPropia = posicionAngular;
    }

    public void setTamanoCabeza(float tamanoCabeza) {
        this.tamanoCabeza = tamanoCabeza;
    }

    public void dibujese(Canvas canvas, Paint pincel) {

        canvas.save();

        pincel.setColor(color);
        canvas.translate(posicionX, posicionY);
        canvas.rotate(posicionAngularPropia);

        float cabeza = tamanoCabeza;

        //el asta
        pincel.setStyle(Paint.Style.STROKE);
        pincel.setStrokeWidth(grosorLinea);
        canvas.drawLine(0, 0, longitud - cabeza, 0, pincel);

        //la punta, como triángulo relleno
        Path punta = new Path();
        punta.moveTo(longitud, 0);
        punta.lineTo(longitud - cabeza, -0.55f * cabeza);
        punta.lineTo(longitud - cabeza, 0.55f * cabeza);
        punta.close();
        pincel.setStyle(Paint.Style.FILL);
        canvas.drawPath(punta, pincel);

        canvas.restore();
        pincel.reset();

    }

}

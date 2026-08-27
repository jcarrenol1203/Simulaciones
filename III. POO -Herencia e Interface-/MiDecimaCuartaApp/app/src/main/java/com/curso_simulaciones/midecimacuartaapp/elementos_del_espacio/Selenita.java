package com.curso_simulaciones.midecimacuartaapp.elementos_del_espacio;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;

/**
 * Selenita: extraterrestre concreto (habitante imaginario de la Luna).
 *
 * Según Tabla 1 del taller se dibuja como un cuerpo rectangular
 * rojo, con dos "botones"/ojitos blancos en la parte superior.
 *
 * En la escena, un Selenita se desplaza horizontalmente con
 * velocidad constante y rota con velocidad angular constante
 * alrededor de su propio centroide (eso se programa en la
 * Activity, llamando a mover(dx, 0, angulo) heredado de
 * Extraterrestre - esta clase solo se encarga de DIBUJARSE,
 * no de decidir cómo se mueve).
 */
public class Selenita extends Extraterrestre {

    // dimensiones fijas del cuerpo rectangular del Selenita
    private static final float ANCHO_CUERPO = 45f;
    private static final float ALTO_CUERPO = 70f;

    /**
     * Constructor por defecto.
     * Selenita centrado en (0,0).
     */
    public Selenita() {
        super();
        this.color = Color.RED;
    }

    /**
     * Constructor de Selenita centrado en
     * (posicionCentroideX, posicionCentroideY).
     *
     * @param posicionCentroideX posición X inicial del centroide
     * @param posicionCentroideY posición Y inicial del centroide
     */
    public Selenita(float posicionCentroideX, float posicionCentroideY) {
        super(posicionCentroideX, posicionCentroideY);
        this.color = Color.RED;
    }

    @Override
    public void dibujese(Canvas canvas, Paint pincel) {

        canvas.save();

        //magnificar (por si en algún momento se decide cambiar de tamaño)
        canvas.scale(magnificacion, magnificacion, posicionCentroideX, posicionCentroideY);
        //rotar alrededor de su propio centroide (o del eje que se le haya fijado)
        canvas.rotate(posicionAngularRotacionEjeXY, posicionEjeRotacionX, posicionEjeRotacionY);

        float xIzquierda = posicionCentroideX - 0.5f * ANCHO_CUERPO;
        float yArriba = posicionCentroideY - 0.5f * ALTO_CUERPO;
        float xDerecha = posicionCentroideX + 0.5f * ANCHO_CUERPO;
        float yAbajo = posicionCentroideY + 0.5f * ALTO_CUERPO;

        //dibujar el cuerpo como un rectángulo de esquinas RECTAS
        //(sin redondear), solo relleno, SIN contorno negro
        pincel.setStyle(Paint.Style.FILL);
        pincel.setColor(color);
        canvas.drawRect(xIzquierda, yArriba, xDerecha, yAbajo, pincel);

        //dibujar la extrusión semicircular arriba del cuerpo (como una
        //"cabecita" que sobresale del borde superior). El rectángulo que
        //delimita el círculo se centra justo en el borde superior del
        //cuerpo (yArriba), así el semicírculo queda "pegado" y sobresale
        //solo hacia arriba, no hacia dentro del cuerpo.
        float radioExtrusion = 0.22f * ANCHO_CUERPO;
        RectF rectExtrusion = new RectF(
                posicionCentroideX - radioExtrusion,
                yArriba - radioExtrusion,
                posicionCentroideX + radioExtrusion,
                yArriba + radioExtrusion);
        //arco que arranca en 180° y barre 180°: dibuja solo la MITAD
        //SUPERIOR del círculo; el "true" cierra la figura hacia el centro
        //para que quede rellena como un semicírculo sólido, no un arco hueco
        canvas.drawArc(rectExtrusion, 180f, 180f, true, pincel);

        //dibujar los dos "botones"/ojitos blancos, RECTANGULARES
        //(no circulares) en la parte superior del cuerpo
        pincel.setColor(Color.WHITE);
        float separacionOjos = 0.18f * ANCHO_CUERPO;
        float alturaOjos = posicionCentroideY - 0.25f * ALTO_CUERPO;
        float anchoOjo = 0.16f * ANCHO_CUERPO;
        float altoOjo = 0.16f * ANCHO_CUERPO;
        canvas.drawRect(
                posicionCentroideX - separacionOjos - 0.5f * anchoOjo,
                alturaOjos - 0.5f * altoOjo,
                posicionCentroideX - separacionOjos + 0.5f * anchoOjo,
                alturaOjos + 0.5f * altoOjo,
                pincel);
        canvas.drawRect(
                posicionCentroideX + separacionOjos - 0.5f * anchoOjo,
                alturaOjos - 0.5f * altoOjo,
                posicionCentroideX + separacionOjos + 0.5f * anchoOjo,
                alturaOjos + 0.5f * altoOjo,
                pincel);

        //dibujar el rectángulo blanco que simula la boca, en la parte
        //inferior del cuerpo
        pincel.setColor(Color.WHITE);
        float anchoBoca = 0.4f * ANCHO_CUERPO;
        float altoBoca = 0.1f * ALTO_CUERPO;
        float yBoca = posicionCentroideY + 0.2f * ALTO_CUERPO;
        canvas.drawRect(
                posicionCentroideX - 0.5f * anchoBoca,
                yBoca - 0.5f * altoBoca,
                posicionCentroideX + 0.5f * anchoBoca,
                yBoca + 0.5f * altoBoca,
                pincel);

        //regresar la rotación (y la magnificación)
        canvas.restore();
    }
}
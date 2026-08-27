package com.curso_simulaciones.midecimacuartaapp.elementos_del_espacio;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;

/**
 * Venusiano: extraterrestre concreto (habitante imaginario de Venus).
 *
 * Según Tabla 1 del taller se dibuja como una cabeza/cuerpo
 * circular azul, con dos ojos blancos con pupila, y un pequeño
 * "cinturón" rectangular debajo.
 *
 * En la escena, un Venusiano oscila su centroide con Movimiento
 * Armónico Simple (MAS) en dirección vertical, y se desplaza
 * horizontalmente con velocidad constante (eso se programa en
 * la Activity llamando a mover(dx, dy) heredado de Extraterrestre;
 * esta clase solo se encarga de DIBUJARSE).
 */
public class Venusiano extends Extraterrestre {

    // radio fijo de la cabeza/cuerpo circular del Venusiano
    private static final float RADIO = 45f;

    /**
     * Constructor por defecto.
     * Venusiano centrado en (0,0).
     */
    public Venusiano() {
        super();
        this.color = Color.BLUE;
    }

    /**
     * Constructor de Venusiano centrado en
     * (posicionCentroideX, posicionCentroideY).
     *
     * @param posicionCentroideX posición X inicial del centroide
     * @param posicionCentroideY posición Y inicial del centroide
     */
    public Venusiano(float posicionCentroideX, float posicionCentroideY) {
        super(posicionCentroideX, posicionCentroideY);
        this.color = Color.BLUE;
    }

    @Override
    public void dibujese(Canvas canvas, Paint pincel) {

        canvas.save();

        //magnificar (por si en algún momento se decide cambiar de tamaño)
        canvas.scale(magnificacion, magnificacion, posicionCentroideX, posicionCentroideY);
        //rotar (el Venusiano de este taller no rota, pero se deja
        //la transformación por consistencia con el resto de la jerarquía)
        canvas.rotate(posicionAngularRotacionEjeXY, posicionEjeRotacionX, posicionEjeRotacionY);

        pincel.setStyle(Paint.Style.FILL);
        pincel.setColor(color);

        //dibujar PRIMERO el óvalo superior, ANTES que el círculo principal:
        //así el círculo (que se dibuja encima) tapa la mitad inferior del
        //óvalo, y el óvalo queda "un poco inmerso" en el círculo en vez de
        //apoyado por fuera. Se centra justo en el borde superior del círculo.
        //Ancho = 3/4 del DIÁMETRO del círculo (diámetro = 2*RADIO)
        float anchoOvalo = 1.5f * RADIO;
        float altoOvalo = 0.4f * RADIO;
        float yCentroOvalo = posicionCentroideY - RADIO;
        RectF rectOvalo = new RectF(
                posicionCentroideX - 0.5f * anchoOvalo,
                yCentroOvalo - 0.5f * altoOvalo,
                posicionCentroideX + 0.5f * anchoOvalo,
                yCentroOvalo + 0.5f * altoOvalo);
        canvas.drawOval(rectOvalo, pincel);

        //dibujar la cabeza/cuerpo: CÍRCULO perfecto azul, relleno,
        //sin contorno negro. Al dibujarse DESPUÉS del óvalo, tapa la
        //parte de éste que le corresponde "hundir".
        canvas.drawCircle(posicionCentroideX, posicionCentroideY, RADIO, pincel);

        //dibujar los ojos: círculos blancos con una pupila de punto azul.
        //Se subieron más arriba (antes casi estaban a la altura del centro)
        float separacionOjos = 0.42f * RADIO;
        float alturaOjos = posicionCentroideY - 0.35f * RADIO;
        float ojoIzquierdoX = posicionCentroideX - separacionOjos;
        float ojoDerechoX = posicionCentroideX + separacionOjos;
        pincel.setColor(Color.WHITE);
        canvas.drawCircle(ojoIzquierdoX, alturaOjos, 0.28f * RADIO, pincel);
        canvas.drawCircle(ojoDerechoX, alturaOjos, 0.28f * RADIO, pincel);

        //pupila: punto azul (mismo color del cuerpo) en el centro de cada ojo
        pincel.setColor(color);
        canvas.drawCircle(ojoIzquierdoX, alturaOjos, 0.1f * RADIO, pincel);
        canvas.drawCircle(ojoDerechoX, alturaOjos, 0.1f * RADIO, pincel);

        //dibujar la nariz: un punto negro justo en el centro del círculo
        pincel.setColor(Color.BLACK);
        canvas.drawCircle(posicionCentroideX, posicionCentroideY, 0.08f * RADIO, pincel);

        //dibujar la boca: un RECTÁNGULO blanco, cuyo largo es unas 6-7
        //veces su ancho (una barrita horizontal delgada, no una línea)
        pincel.setColor(Color.WHITE);
        float largoBoca = 0.55f * RADIO;
        float anchoBoca = largoBoca / 6.5f;
        float yBoca = posicionCentroideY + 0.45f * RADIO;
        canvas.drawRect(
                posicionCentroideX - 0.5f * largoBoca,
                yBoca - 0.5f * anchoBoca,
                posicionCentroideX + 0.5f * largoBoca,
                yBoca + 0.5f * anchoBoca,
                pincel);

        //dibujar el "cinturón"/base rectangular debajo del cuerpo, TANGENTE
        //al círculo (su borde superior coincide exactamente con el borde
        //inferior del círculo, sin encimarse ni dejar espacio).
        //Ancho = 3/4 del DIÁMETRO del círculo, igual que el óvalo de arriba
        pincel.setColor(color);
        float anchoCinturon = 1.5f * RADIO;
        float altoCinturon = 0.35f * RADIO;
        float yAbajo = posicionCentroideY + RADIO;
        canvas.drawRect(
                posicionCentroideX - 0.5f * anchoCinturon,
                yAbajo,
                posicionCentroideX + 0.5f * anchoCinturon,
                yAbajo + altoCinturon,
                pincel);

        //regresar la rotación (y la magnificación)
        canvas.restore();
    }
}
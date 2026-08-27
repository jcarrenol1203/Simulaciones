package com.curso_simulaciones.midecimacuartaapp.elementos_del_espacio;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

/**
 * Marciano: extraterrestre concreto (instanciable) que
 * SÍ implementa dibujese(), tal como pide el taller.
 *
 * No agrega atributos nuevos respecto a Extraterrestre -
 * le basta con lo heredado (posición, rotación, color,
 * magnificación) - así que sus constructores solo delegan
 * a super(...) y su único trabajo propio es "saber dibujarse".
 */
public class Marciano extends Extraterrestre {

    /**
     * Constructor por defecto
     * del marciano con "centroide" en (0,0)
     * y de radio 50f
     */
    public Marciano() {
        super();
    }

    /**
     * Constructor del marciano con "centroide"
     * en (posicionCentroideX,posicionCentroideY)
     * y diámetro igual a 2*radio
     */
    public Marciano(float posicionCentroideX, float posicionCentroideY) {
        super(posicionCentroideX, posicionCentroideY);
    }

    // se implementó este método dque en su clase madre es abstracto
    @Override
    public void dibujese(Canvas canvas, Paint pincel) {

        //estilo del pindel
        pincel.setStyle(Paint.Style.STROKE);
        //grosor del pincel
        pincel.setStrokeWidth(2f);
        //color del pincel
        pincel.setColor(color);

        canvas.save();

        //magnificar: escala el dibujo alrededor del propio centroide,
        //así el marciano crece/decrece sin cambiar de posición
        canvas.scale(magnificacion, magnificacion, posicionCentroideX, posicionCentroideY);
        //rotar
        canvas.rotate(posicionAngularRotacionEjeXY, posicionEjeRotacionX, posicionEjeRotacionY);
        //dibujar circulo de la cara del marciano
        //(solo relleno, SIN contorno negro alrededor - según la
        //caricatura de referencia el círculo principal no lleva borde)
        float radio = 50f;
        pincel.setStyle(Paint.Style.FILL);
        canvas.drawCircle(posicionCentroideX, posicionCentroideY, radio, pincel);

        //dibujar los ojitos del marcianito
        float separacion = 0.3f * radio;
        float posicionOjoCentroIzquierdoX = posicionCentroideX - separacion;
        float posicionOjoCentroIzquierdoY = posicionCentroideY - separacion;
        float posicionOjoCentroDerechoX = posicionCentroideX + separacion;
        float posicionOjoCentroDerechoY = posicionCentroideY - separacion;
        pincel.setStyle(Paint.Style.FILL);
        pincel.setColor(Color.WHITE);
        canvas.drawCircle(posicionOjoCentroIzquierdoX, posicionOjoCentroIzquierdoY, 0.2f * radio, pincel);
        canvas.drawCircle(posicionOjoCentroDerechoX, posicionOjoCentroDerechoY, 0.2f * radio, pincel);
        //dibujar la pupila de los ojitos
        pincel.setColor(color);
        canvas.drawCircle(posicionOjoCentroIzquierdoX, posicionOjoCentroIzquierdoY, 0.1f * radio, pincel);
        canvas.drawCircle(posicionOjoCentroDerechoX, posicionOjoCentroDerechoY, 0.1f * radio, pincel);

        //dibujar la boquita del marcianito
        float altoBoca = 0.2f * radio;
        float anchoBoca = 0.5f * radio;
        pincel.setColor(Color.WHITE);
        float descenso = 0.3f * radio;
        float posicionBocaCentroX = posicionCentroideX;
        float posicionBocaCentroY = posicionCentroideY + descenso;
        float xs_izquierda = posicionBocaCentroX - 0.5f * anchoBoca;
        float ys_izquierda = posicionBocaCentroY - 0.5f * altoBoca;
        float xi_derecha = posicionBocaCentroX + 0.5f * anchoBoca;
        float yi_derecha = posicionBocaCentroY + 0.5f * altoBoca;
        canvas.drawRect(xs_izquierda, ys_izquierda, xi_derecha, yi_derecha, pincel);

        //dibujar las orejitas del marcianito
        //oreja derecha
        float x_i_oreja_derecha = posicionOjoCentroDerechoX + 0.1f * radio;
        float y_i_oreja_derecha = posicionOjoCentroDerechoY - 0.2f * radio;
        float x_s_oreja_derecha = posicionCentroideX + radio;
        float y_s_oreja_derecha = posicionCentroideY - 2f * radio;
        //orejita izaquierda
        float x_i_oreja_izquierda = posicionOjoCentroIzquierdoX - 0.1f * radio;
        float y_i_oreja_izquierda = posicionOjoCentroIzquierdoY - 0.2f * radio;
        float x_s_oreja_izquierda = posicionCentroideX - radio;
        float y_s_oreja_izquierda = posicionCentroideY - 2f * radio;

        pincel.setColor(color);
        pincel.setStrokeWidth(20f);

        canvas.drawLine(x_i_oreja_derecha, y_i_oreja_derecha, x_s_oreja_derecha, y_s_oreja_derecha, pincel);
        canvas.drawLine(x_i_oreja_izquierda, y_i_oreja_izquierda, x_s_oreja_izquierda, y_s_oreja_izquierda, pincel);

        //regresa la rotación (y la magnificación)
        canvas.restore();

    }
}
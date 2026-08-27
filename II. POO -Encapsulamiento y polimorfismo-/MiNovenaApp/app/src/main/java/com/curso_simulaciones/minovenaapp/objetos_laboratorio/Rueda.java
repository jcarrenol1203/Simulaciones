package com.curso_simulaciones.minovenaapp.objetos_laboratorio;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

/** Rueda con el diseño de la figura 44: neumático, disco, radios y buje. */
public class Rueda {
    // Radio total de la rueda, calculado de forma responsiva.
    private final float radio;
    // Centro original de la rueda.
    private final float xInicial, yInicial;
    // Estado actual que cambia durante la animación.
    private float x, y, angulo;
    // Color del disco interior; la figura usa rojo.
    private int color = Color.RED;

    // Construye la rueda y conserva su posición inicial.
    public Rueda(float xInicial, float yInicial, float radio) {
        this.xInicial = xInicial;
        this.yInicial = yInicial;
        this.x = xInicial;
        this.y = yInicial;
        this.radio = radio;
    }

    // Permite personalizar el color interior de cada rueda.
    public void setColor(int color) { this.color = color; }

    // Sobrecarga para trasladar la rueda sin rotarla.
    public void mover(float desplazamientoX, float desplazamientoY) {
        x = xInicial + desplazamientoX;
        y = yInicial + desplazamientoY;
    }

    // Sobrecarga para rotar la rueda alrededor de su centro.
    public void mover(float angulo) {
        x = xInicial;
        y = yInicial;
        this.angulo = angulo;
    }

    // Sobrecarga para trasladar y rotar al mismo tiempo.
    public void mover(float desplazamientoX, float desplazamientoY, float angulo) {
        x = xInicial + desplazamientoX;
        y = yInicial + desplazamientoY;
        this.angulo = angulo;
    }

    // Dibuja la rueda completa con la apariencia solicitada.
    public void dibujese(Canvas canvas, Paint pincel) {
        // Aísla la rotación para no afectar a las demás ruedas.
        canvas.save();
        // Gira radios y marcadores alrededor del centro.
        canvas.rotate(angulo, x, y);

        // Neumático negro sólido, visible como el anillo exterior.
        pincel.setStyle(Paint.Style.FILL);
        pincel.setColor(Color.rgb(0, 25, 0));
        canvas.drawCircle(x, y, radio, pincel);

        // Disco interior rojo (o el color configurado para esta rueda).
        pincel.setColor(color);
        canvas.drawCircle(x, y, radio * 0.84f, pincel);

        // Radios blancos que parten del buje y llegan cerca del borde.
        pincel.setColor(Color.WHITE);
        pincel.setStyle(Paint.Style.STROKE);
        pincel.setStrokeWidth(Math.max(4f, radio * 0.07f));
        for (int i = 0; i < 8; i++) {
            float anguloRadio = (float) (i * Math.PI / 4.0);
            float inicioX = x + (float) Math.cos(anguloRadio) * radio * 0.20f;
            float inicioY = y + (float) Math.sin(anguloRadio) * radio * 0.20f;
            float finX = x + (float) Math.cos(anguloRadio) * radio * 0.70f;
            float finY = y + (float) Math.sin(anguloRadio) * radio * 0.70f;
            canvas.drawLine(inicioX, inicioY, finX, finY, pincel);

            // Círculo blanco que remata cada radio, como en la figura.
            pincel.setStyle(Paint.Style.FILL);
            canvas.drawCircle(finX, finY, Math.max(5f, radio * 0.09f), pincel);
            pincel.setStyle(Paint.Style.STROKE);
        }

        // Buje central blanco grande.
        pincel.setStyle(Paint.Style.FILL);
        canvas.drawCircle(x, y, radio * 0.25f, pincel);

        // Restaura el canvas antes de dibujar otra rueda.
        canvas.restore();
    }
}

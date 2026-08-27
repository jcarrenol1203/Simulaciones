package com.curso_simulaciones.mioctavaapp.objetos_laboratorio;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

public class Polea {
    private float radio;
    private float posicionInicialX, posicionInicialY;
    private float posicionX, posicionY, posicionAngular;
    private int color = Color.RED;

    public Polea() {
        radio = 20f;
    }

    public Polea(float posicionInicialX, float posicionInicialY, float radio) {
        posicionX = posicionInicialX;
        posicionY = posicionInicialY;
        this.posicionInicialX = posicionInicialX;
        this.posicionInicialY = posicionInicialY;
        this.radio = radio;
    }

    public void setRadioPolea(float radio) { this.radio = radio; }
    public float getRadioPolea() { return radio; }
    public void setColorPolea(int color) { this.color = color; }
    public int getColorPolea() { return color; }

    public void moverPolea(float desplazamientoX, float desplazamientoY) {
        posicionX = posicionInicialX + desplazamientoX;
        posicionY = posicionInicialY + desplazamientoY;
    }

    public void moverPolea(float posicionAngular) {
        posicionX = posicionInicialX;
        posicionY = posicionInicialY;
        this.posicionAngular = posicionAngular;
    }

    public void moverPolea(float desplazamientoX, float desplazamientoY, float posicionAngular) {
        posicionX = posicionInicialX + desplazamientoX;
        posicionY = posicionInicialY + desplazamientoY;
        this.posicionAngular = posicionAngular;
    }

    public void dibujese(Canvas canvas, Paint pincel) {
        pincel.setStyle(Paint.Style.STROKE);
        pincel.setStrokeWidth(2f);
        pincel.setColor(color);
        canvas.save();
        canvas.rotate(posicionAngular, posicionX, posicionY);
        canvas.drawCircle(posicionX, posicionY, radio, pincel);
        for (int i = 0; i < 12; i++) {
            canvas.rotate(i * 36f, posicionX, posicionY);
            canvas.drawLine(posicionX, posicionY, posicionX, posicionY - radio, pincel);
            canvas.rotate(-i * 36f, posicionX, posicionY);
        }
        canvas.restore();
    }
}

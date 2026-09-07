package com.curso_simulaciones.mitrigesimaseptimaapp.utilidades;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.MotionEvent;
import android.view.View;

public class TableroColor extends View {

    private Context context;
    private int rojo = 100;
    private int verde = 200;
    private int azul = 60;
    private int color_rgb, color_azul, color_rojo, color_verde;

    public TableroColor(Context context) {
        super(context);
        this.context = context;
        eventos();
    }

    private void eventos() {
        this.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                int action = event.getAction();
                //x = event.getX();//en pixeles
                //    y = event.getY();//en pixeles
                switch (action) {
                    case MotionEvent.ACTION_DOWN:
                        break;
                    case MotionEvent.ACTION_MOVE:
                        break;
                    case MotionEvent.ACTION_UP:
                        break;
                }
                return true;
            }
        });
    }

    public void setColoRojo(int rojo) {
        this.rojo = rojo;
    }

    public void setColoVerde(int verde) {
        this.verde = verde;
    }

    public void setColoAzul(int azul) {
        this.azul = azul;
    }

    //Método para dibujar
    protected void onDraw(Canvas canvas) {
        Paint pincel = new Paint();
        pincel.setAntiAlias(true);
        pincel.setLinearText(true);
        pincel.setFilterBitmap(true);//si se escala
        pincel.setDither(true);//si se escala

        float ancho = this.getWidth();
        float alto = this.getHeight();
        float a = 0;
        if (ancho > alto) {
            a = alto;
        } else {
            a = ancho;
        }
        float indent = 0.05f * alto;

        color_rgb = Color.rgb(rojo, verde, azul);
        pincel.setColor(color_rgb);
        pincel.setStyle(Paint.Style.FILL);
        canvas.drawRect(indent, indent, 0.5f * ancho - indent, alto - indent, pincel);
        pincel.setColor(Color.BLACK);
        pincel.setStyle(Paint.Style.STROKE);
        canvas.drawRect(indent, indent, 0.5f * ancho - indent, alto - indent, pincel);

        pincel.setStyle(Paint.Style.FILL);
        color_rojo = Color.rgb(rojo, 0, 0);
        pincel.setColor(color_rojo);
        canvas.drawRect(indent + 0.5f * ancho, indent, ancho - indent, indent + 0.1f * alto, pincel);
        pincel.setTextSize(0.05f * a);
        pincel.setColor(Color.RED);
        canvas.drawText("Rojo= " + rojo, indent + 0.5f * ancho, indent + 0.2f * alto, pincel);

        color_verde = Color.rgb(0, verde, 0);
        pincel.setColor(color_verde);
        canvas.drawRect(indent + 0.5f * ancho, indent + 0.3f * alto, ancho - indent, indent + 0.4f * alto, pincel);
        pincel.setColor(Color.rgb(0, 128, 0));
        canvas.drawText("Verde= " + verde, indent + 0.5f * ancho, indent + 0.5f * alto, pincel);

        color_azul = Color.rgb(0, 0, azul);
        pincel.setColor(color_azul);
        canvas.drawRect(indent + 0.5f * ancho, indent + 0.6f * alto, ancho - indent, indent + 0.7f * alto, pincel);
        pincel.setColor(Color.BLUE);
        canvas.drawText("Azul= " + azul, indent + 0.5f * ancho, indent + 0.8f * alto, pincel);

        pincel.setTextSize(0.03f * a);
        pincel.setColor(Color.LTGRAY);
        canvas.drawText("Copyright 2021 para Diego L. Aristizábal Ramírez", indent, alto - 0.3f * indent, pincel);

        //Para efectos de animación
        invalidate();
    }

}

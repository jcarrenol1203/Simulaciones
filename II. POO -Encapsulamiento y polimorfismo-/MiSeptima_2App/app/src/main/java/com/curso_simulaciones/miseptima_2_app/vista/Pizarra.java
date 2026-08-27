package com.curso_simulaciones.miseptima_2_app.vista;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.View;

import com.curso_simulaciones.miseptima_2_app.objetos_laboratorio.Polea;

public class Pizarra extends View {
    private Polea[] poleas;

    public Pizarra(Context context) {
        super(context);
    }

    public void setEstadoEscena(Polea[] poleas) {
        this.poleas = poleas;
    }

    private void dibujarEscena(Canvas canvas, Paint pincel) {
        for (Polea polea : poleas) {
            if (polea != null) {
                polea.dibujese(canvas, pincel);
            }
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Paint pincel = new Paint(Paint.ANTI_ALIAS_FLAG);
        if (poleas != null) {
            dibujarEscena(canvas, pincel);
        }
        invalidate();
    }
}

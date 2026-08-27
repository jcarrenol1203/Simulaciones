package com.curso_simulaciones.mioctavaapp.vista;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.View;

import com.curso_simulaciones.mioctavaapp.objetos_laboratorio.Polea;

public class Pizarra extends View {
    private Polea[] poleas;

    public Pizarra(Context context) { super(context); }
    public void setEstadoEscena(Polea[] poleas) { this.poleas = poleas; }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Paint pincel = new Paint(Paint.ANTI_ALIAS_FLAG);
        if (poleas != null) {
            for (Polea polea : poleas) {
                if (polea != null) polea.dibujese(canvas, pincel);
            }
        }
        invalidate();
    }
}

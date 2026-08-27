package com.curso_simulaciones.mivigesimaquintaapp.vista;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.view.View;

import com.curso_simulaciones.mivigesimaquintaapp.datos.AlmacenDatosRAM;
import com.curso_simulaciones.simulphysics.objetos_laboratorio.ObjetoLaboratorio;

public class Pizarra extends View {

    private ObjetoLaboratorio objetosLab[];

    /*
     Aviso de que ya se conoce el tamaño REAL (en pixeles) con el que
     Android terminó de dibujar esta View en pantalla. Es la única
     fuente confiable de las dimensiones de la pizarra: a diferencia
     de estimarlas a partir de la resolución de pantalla en portrait
     (que no es exacta en todos los dispositivos por las barras de
     sistema, notch, etc.), este valor es el tamaño final ya medido
     por el sistema de layout, así que es responsivo en cualquier
     celular o tablet.
     */
    public interface EscuchaTamano {
        void onTamanoDisponible(int ancho, int alto);
    }

    private EscuchaTamano escuchaTamano;

    /**
     * Constructor
     *
     * @param context
     */
    public Pizarra(Context context) {
        super(context);

    }

    public void setEscuchaTamano(EscuchaTamano escuchaTamano) {
        this.escuchaTamano = escuchaTamano;
    }

    @Override
    protected void onSizeChanged(int ancho, int alto, int anchoAnterior, int altoAnterior) {
        super.onSizeChanged(ancho, alto, anchoAnterior, altoAnterior);

        if (ancho > 0 && alto > 0 && escuchaTamano != null) {
            escuchaTamano.onTamanoDisponible(ancho, alto);
        }
    }


    public void setEstadoEscena(ObjetoLaboratorio[] cuerpos) {

        this.objetosLab = cuerpos;

    }


    //Método para dibujar la escena
    private void dibujarEscena(Canvas canvas, Paint pincel) {

        for (int i = 0; i < objetosLab.length; i++) {
            if (objetosLab[i] != null) {
                objetosLab[i].dibujese(canvas, pincel);
            }
        }

    }


    //método para dibujar
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        Paint pincel = new Paint();
        pincel.setAntiAlias(true);

        //dibujar la escena física
        if (objetosLab != null)
            dibujarEscena(canvas, pincel);

        //dibujar el panel de valores (Figura 12B: "Desplegar aquí los valores")
        dibujarValores(canvas, pincel);

        //necesario para actualizar los dibujos en animaciones
        invalidate();

    }

    private void dibujarValores(Canvas canvas, Paint pincel) {

        pincel.setTextSize(CR.pcApxL(2.1f));
        pincel.setTypeface(Typeface.MONOSPACE);
        pincel.setColor(Color.BLACK);

        float x = CR.pcApxX(1f);
        float paso = CR.pcApxY(4f);
        float y = paso;

        canvas.drawText("--- Masas (deslizadores) ---", x, y, pincel);
        y += paso;
        canvas.drawText("m1   (kg)   = " + String.format("%.1f", AlmacenDatosRAM.m1), x, y, pincel);
        y += paso;
        canvas.drawText("m2   (kg)   = " + String.format("%.1f", AlmacenDatosRAM.m2), x, y, pincel);
        y += paso;
        canvas.drawText("m3   (kg)   = " + String.format("%.1f", AlmacenDatosRAM.m3), x, y, pincel);
        y += paso;

        String t = String.format("%.2f", AlmacenDatosRAM.tiempo);
        canvas.drawText("t    (s)     = " + t, x, y, pincel);
        y += paso;

        canvas.drawText("--- Aceleraciones ---", x, y, pincel);
        y += paso;
        canvas.drawText("a1  (m/s2)  = " + String.format("%.2f", AlmacenDatosRAM.a1), x, y, pincel);
        y += paso;
        canvas.drawText("a2  (m/s2)  = " + String.format("%.2f", AlmacenDatosRAM.a2), x, y, pincel);
        y += paso;
        canvas.drawText("a3  (m/s2)  = " + String.format("%.2f", AlmacenDatosRAM.a3), x, y, pincel);
        y += paso;

        canvas.drawText("--- Desplazamientos ---", x, y, pincel);
        y += paso;
        canvas.drawText("d1   (m)    = " + String.format("%.2f", AlmacenDatosRAM.desplazamiento_m1_en_metros), x, y, pincel);
        y += paso;
        canvas.drawText("d2   (m)    = " + String.format("%.2f", AlmacenDatosRAM.desplazamiento_m2_en_metros), x, y, pincel);
        y += paso;
        canvas.drawText("d3   (m)    = " + String.format("%.2f", AlmacenDatosRAM.desplazamiento_m3_en_metros), x, y, pincel);
        y += paso;

        canvas.drawText("--- Posiciones (y) ---", x, y, pincel);
        y += paso;
        canvas.drawText("y1   (m)    = " + String.format("%.2f", AlmacenDatosRAM.y1_en_metros), x, y, pincel);
        y += paso;
        canvas.drawText("y2   (m)    = " + String.format("%.2f", AlmacenDatosRAM.y2_en_metros), x, y, pincel);
        y += paso;
        canvas.drawText("y3   (m)    = " + String.format("%.2f", AlmacenDatosRAM.y3_en_metros), x, y, pincel);
        y += paso;

        canvas.drawText("--- Tension cuerdas ---", x, y, pincel);
        y += paso;
        canvas.drawText("T_sup (N)   = " + String.format("%.2f", AlmacenDatosRAM.T_sup), x, y, pincel);
        y += paso;
        canvas.drawText("T_inf (N)   = " + String.format("%.2f", AlmacenDatosRAM.T_inf), x, y, pincel);

        pincel.setColor(Color.BLACK);
        String autor = "Copyright 2026 para Diego Luis Aristizábal Ramírez";
        canvas.drawText(autor, CR.pcApxX(2), CR.pcApxY(92), pincel);
        String universidad = "Universidad Nacional de Colombia - Sede Medellín";
        canvas.drawText(universidad, CR.pcApxX(2), CR.pcApxY(96), pincel);

    }

}

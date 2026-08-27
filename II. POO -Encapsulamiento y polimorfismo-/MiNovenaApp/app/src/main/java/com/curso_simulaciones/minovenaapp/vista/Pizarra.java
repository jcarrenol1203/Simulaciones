package com.curso_simulaciones.minovenaapp.vista;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.View;
import com.curso_simulaciones.minovenaapp.objetos_laboratorio.Rueda;

/** Vista personalizada que pinta la escena en cada fotograma. */
public class Pizarra extends View {
    // Arreglo recibido desde la actividad con las ruedas de la escena.
    private Rueda[] ruedas;
    // Constructor requerido para crear la vista desde Java.
    public Pizarra(Context context) { super(context); }
    // Actualiza las ruedas que la vista debe representar.
    public void setEstadoEscena(Rueda[] ruedas) { this.ruedas = ruedas; }

    // Android llama a este método cada vez que debe redibujar la vista.
    @Override protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Paint pincel = new Paint(Paint.ANTI_ALIAS_FLAG);
        if (ruedas != null) for (Rueda rueda : ruedas) if (rueda != null) rueda.dibujese(canvas, pincel);
        invalidate(); // Solicita el siguiente fotograma para mantener la animación.
    }
}

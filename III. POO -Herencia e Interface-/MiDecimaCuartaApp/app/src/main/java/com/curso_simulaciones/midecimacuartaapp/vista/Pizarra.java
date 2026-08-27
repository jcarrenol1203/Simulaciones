package com.curso_simulaciones.midecimacuartaapp.vista;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.View;

import com.curso_simulaciones.midecimacuartaapp.elementos_del_espacio.ObjetoEspacial;

/**
 * Pizarra: vista personalizada (extends View) donde se dibuja
 * toda la escena.
 *
 * Guarda un arreglo de ObjetoEspacial[] - el tipo MÁS GENERAL de
 * toda la jerarquía - así que puede recibir en la misma casilla
 * a un Marciano, un Selenita, un Venusiano o una EstrellaFija sin
 * distinguirlos: a cada uno simplemente le pide que se dibujese()
 * a sí mismo (polimorfismo), sin importarle que unos desciendan
 * de Extraterrestre y otra directamente de ObjetoEspacial.
 */
public class Pizarra extends View {

    private ObjetoEspacial objetos[];


    /**
     * Constructor
     *
     * @param context
     */
    public Pizarra(Context context) {
        super(context);

    }


    public void setEstadoEscena(ObjetoEspacial[] objetos) {

        this.objetos = objetos;

    }



    //Método para dibujar la escena
    private void dibujarEscena(Canvas canvas, Paint pincel) {

        //recorrer el arreglo y pedirle a cada objeto que se dibuje;
        //Java decide en tiempo de ejecución cuál dibujese() ejecutar
        //según el tipo REAL de cada objeto guardado (enlace dinámico)
        for (int i = 0; i < objetos.length; i++) {
            if (objetos[i] != null) {
                objetos[i].dibujese(canvas, pincel);
            }
        }


    }



    //método para dibujar
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);


        Paint pincel = new Paint();
        //evita efecto sierra
        pincel.setAntiAlias(true);

        if (objetos != null)
            dibujarEscena(canvas, pincel);


        //necesario para actualizar los dibujos en animaciones
        invalidate();

    }
}
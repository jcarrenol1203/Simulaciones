package com.curso_simulaciones.mivigesimaquintaapp.vista;

import com.curso_simulaciones.simulphysics.objetos_laboratorio.Marca;

/*
 Marca (etiqueta de texto) que además se puede reubicar cuadro a
 cuadro. Se necesita para la etiqueta "P" de la polea móvil: a
 diferencia de los bloques (Masa), que llevan su etiqueta pegada
 adentro y se mueven junto con mover(dx,dy), Marca no tiene un
 método para cambiar su posición después de creada.
 */
public class EtiquetaMovil extends Marca {

    public EtiquetaMovil(String marca, float posicionX, float posicionY) {
        super(marca, posicionX, posicionY);
    }

    public void actualizarPosicion(float posicionX, float posicionY) {
        this.posicionX = posicionX;
        this.posicionY = posicionY;
    }

}

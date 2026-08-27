package com.curso_simulaciones.midecimacuartaapp.elementos_del_espacio;

import android.graphics.Canvas;
import android.graphics.Paint;

/**
 * Interfaz Dibujable.
 *
 * Es un CONTRATO puro: no tiene atributos ni implementación,
 * solo declara la firma de un método. Cualquier clase que
 * escriba "implements Dibujable" se compromete a proveer
 * su propia versión de dibujese(Canvas, Paint), o de lo
 * contrario el compilador no la deja compilar (a menos que
 * esa clase también sea abstracta y traspase la obligación
 * a sus propias subclases, que es justo lo que hace
 * ObjetoEspacial en este taller).
 *
 * Gracias a esta interfaz, la clase Pizarra puede recorrer
 * un arreglo de ObjetoEspacial[] y llamar dibujese(...) sobre
 * cada elemento sin necesitar saber si es una EstrellaFija,
 * un Marciano, un Selenita o un Venusiano: solo le importa
 * que el objeto "sepa dibujarse" porque implementa Dibujable.
 */
public interface Dibujable {

    /**
     * Dibuja el objeto sobre el canvas recibido, usando
     * el pincel (Paint) recibido para color/estilo/grosor.
     *
     * Nótese que en una interfaz el método NO lleva cuerpo
     * (no hay llaves { } con código), solo termina en ";".
     *
     * @param canvas superficie de dibujo entregada por Pizarra.onDraw()
     * @param pincel configuración de color/estilo con la que se dibuja
     */
    void dibujese(Canvas canvas, Paint pincel);
}
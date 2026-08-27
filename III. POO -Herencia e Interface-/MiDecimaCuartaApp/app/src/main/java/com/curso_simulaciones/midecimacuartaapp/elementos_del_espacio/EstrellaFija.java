package com.curso_simulaciones.midecimacuartaapp.elementos_del_espacio;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;

/**
 * EstrellaFija: "extends ObjetoEspacial" directamente, NO
 * "extends Extraterrestre" - es decir, es HERMANA de Extraterrestre
 * en la jerarquía (Figura 25), no su hija. Por eso NO hereda nada
 * de posición/rotación/magnificación: como es "Fija", nunca se
 * mueve ni rota ni cambia de tamaño después de creada, así que
 * declara sus propios atributos mínimos (posición, radio, color)
 * en vez de usar los de Extraterrestre.
 *
 * Se dibuja según Tabla 1 como una estrella de 5 puntas rellena,
 * usando un Path (una ruta de puntos unidos por líneas) en vez
 * de drawCircle/drawRect como las demás clases.
 */
public class EstrellaFija extends ObjetoEspacial {

    // posición fija del centro de la estrella
    protected float posicionCentroX, posicionCentroY;

    // "radio" externo de la estrella (distancia del centro a cada punta)
    protected float radioExterno;

    // color de relleno de la estrella; azul por defecto (ver Tabla 1)
    protected int color = Color.BLUE;

    /**
     * Constructor por defecto.
     * Estrella centrada en (0,0), con radio externo 30f.
     */
    public EstrellaFija() {
        this.radioExterno = 30f;
    }

    /**
     * Constructor de EstrellaFija centrada en
     * (posicionCentroX, posicionCentroY), con el radio externo dado.
     *
     * @param posicionCentroX posición X del centro de la estrella
     * @param posicionCentroY posición Y del centro de la estrella
     * @param radioExterno    distancia del centro a la punta de la estrella
     */
    public EstrellaFija(float posicionCentroX, float posicionCentroY, float radioExterno) {
        this.posicionCentroX = posicionCentroX;
        this.posicionCentroY = posicionCentroY;
        this.radioExterno = radioExterno;
    }

    /**
     * Modifica el color de la estrella.
     *
     * @param color nuevo color
     */
    public void setColor(int color) {
        this.color = color;
    }

    /**
     * Devuelve el color actual de la estrella.
     *
     * @return el color actual
     */
    public int getColor() {
        return color;
    }

    /**
     * Retorna la posición X del centro de la estrella.
     *
     * @return posición X
     */
    public float getPosicionX() {
        return posicionCentroX;
    }

    /**
     * Retorna la posición Y del centro de la estrella.
     *
     * @return posición Y
     */
    public float getPosicionY() {
        return posicionCentroY;
    }

    @Override
    public void dibujese(Canvas canvas, Paint pincel) {

        //estilo relleno, color de la estrella
        pincel.setStyle(Paint.Style.FILL);
        pincel.setColor(color);

        //radio interno: distancia del centro a los "valles" entre puntas.
        //0.5 veces el radio externo da puntas bien marcadas (proporción típica de estrella de 5 puntas)
        float radioInterno = 0.5f * radioExterno;

        //una estrella de 5 puntas tiene 10 vértices en total,
        //alternando entre radio externo (puntas) y radio interno (valles)
        Path estrella = new Path();
        int numeroVertices = 10;
        for (int i = 0; i < numeroVertices; i++) {

            //separación angular entre vértices consecutivos: 360/10 = 36 grados
            //se empieza en -90 grados para que la primera punta apunte hacia arriba
            double anguloGrados = -90 + i * (360.0 / numeroVertices);
            double anguloRadianes = Math.toRadians(anguloGrados);

            //los vértices de índice PAR usan el radio externo (puntas),
            //los de índice IMPAR usan el radio interno (valles)
            float radioVertice = (i % 2 == 0) ? radioExterno : radioInterno;

            float x = (float) (posicionCentroX + radioVertice * Math.cos(anguloRadianes));
            float y = (float) (posicionCentroY + radioVertice * Math.sin(anguloRadianes));

            if (i == 0) {
                //primer punto: solo se posiciona el "lápiz", sin dibujar línea todavía
                estrella.moveTo(x, y);
            } else {
                //resto de puntos: se traza una línea desde el punto anterior
                estrella.lineTo(x, y);
            }
        }
        //cierra la figura, uniendo el último vértice con el primero
        estrella.close();

        canvas.drawPath(estrella, pincel);

        //contorno negro para que la estrella se distinga bien sobre el fondo blanco.
        //IMPORTANTE: pincel es un mismo objeto Paint COMPARTIDO por todos los
        //objetos de la escena (Pizarra lo crea una sola vez por frame). Si no se
        //fija aquí explícitamente el grosor del trazo, se hereda cualquier valor
        //que haya dejado el objeto dibujado justo antes (por ejemplo Marciano deja
        //setStrokeWidth(20f) puesto para sus orejas) - eso hacía que el contorno de
        //la estrella saliera gigante y la tapara casi por completo de negro.
        pincel.setStrokeWidth(2f);
        pincel.setStyle(Paint.Style.STROKE);
        pincel.setColor(Color.BLACK);
        canvas.drawPath(estrella, pincel);
    }
}
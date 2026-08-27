package com.curso_simulaciones.midecimacuartaapp.elementos_del_espacio;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

/**
 * Extraterrestre: clase intermedia de la jerarquía.
 *
 * "extends ObjetoEspacial" -> hereda la obligación (vía Dibujable)
 * de tener dibujese(), pero TAMPOCO la implementa aquí todavía
 * (por eso Extraterrestre sigue siendo "abstract", tal como pide
 * el enunciado del taller). La razón de fondo: no existe una
 * única forma de "dibujar un extraterrestre" en general - un
 * Marciano se dibuja distinto a un Selenita o a un Venusiano -
 * pero SÍ existe una única forma de manejar su posición, su
 * rotación y su tamaño, y eso es justo lo que esta clase
 * centraliza para que Marciano/Selenita/Venusiano no tengan
 * que repetirlo cada una por su lado.
 *
 * En otras palabras: esta clase es el equivalente de CuerpoRigido
 * en los talleres anteriores, pero llamando "centroide" a lo que
 * antes se llamaba "centro de masa".
 */
public abstract class Extraterrestre extends ObjetoEspacial {

    // magnificacion: factor de escala para agrandar/achicar el dibujo
    // (1 = tamaño normal, 1.5 = 50% más grande, etc.)
    protected float magnificacion = 1;

    // posición ORIGINAL con la que se creó el objeto (no cambia nunca
    // después del constructor); mover() siempre calcula la posición
    // ACTUAL como esta posición inicial más un desplazamiento
    protected float posicionInicialCentroideX, posicionInicialCentroideY;

    // posición ACTUAL del centroide (centro geométrico) del extraterrestre
    protected float posicionCentroideX, posicionCentroideY;

    // punto (eje) alrededor del cual se aplica la rotación actual
    protected float posicionEjeRotacionX, posicionEjeRotacionY;

    // ángulo actual de rotación alrededor de ese eje, en grados
    protected float posicionAngularRotacionEjeXY;

    // color con el que se dibuja el extraterrestre; rojo por defecto
    protected int color = Color.RED;

    /**
     * Constructor por defecto del extraterrestre.
     * Su centroide está ubicado en la posición (0,0),
     * la posición angular es cero y su color es rojo.
     * (No hace falta escribir nada dentro: los atributos
     * "float" ya arrancan en 0 y "color" ya tiene el valor
     * por defecto Color.RED puesto arriba en su declaración)
     */
    public Extraterrestre() {

    }

    /**
     * Constructor de Extraterrestre cuyo centroide está
     * ubicado en (posicionInicialCentroideX, posicionInicialCentroideY).
     *
     * @param posicionInicialCentroideX posición X inicial del centroide
     * @param posicionInicialCentroideY posición Y inicial del centroide
     */
    public Extraterrestre(float posicionInicialCentroideX, float posicionInicialCentroideY) {
        // this.posicionCentroideX es el ATRIBUTO del objeto;
        // posicionInicialCentroideX (sin "this.") es el PARÁMETRO
        // recibido - "this." es obligatorio aquí porque se llaman
        // distinto en el atributo destino, pero se usa para dejar
        // clarísimo de cuál "dueño" es cada variable
        this.posicionCentroideX = posicionInicialCentroideX;
        this.posicionCentroideY = posicionInicialCentroideY;

        // además de la posición actual, se guarda la posición
        // INICIAL por separado, porque mover() la va a necesitar
        // más adelante como referencia fija
        this.posicionInicialCentroideX = posicionInicialCentroideX;
        this.posicionInicialCentroideY = posicionInicialCentroideY;
    }

    /**
     * Modifica el color del extraterrestre.
     *
     * @param color nuevo color, típicamente uno de android.graphics.Color
     */
    public void setColor(int color) {
        this.color = color;
    }

    /**
     * Devuelve el color actual del extraterrestre.
     *
     * @return el color actual
     */
    public int getColor() {
        return color;
    }

    /*
     Se podría hacer un método setPosicion(x,y)
     para cambiar la posición del centroide, pero más adelante
     esta el método mover(x,y) que hace esto, con la lógica
     de que el desplazamiento siempre es relativo a la posición
     inicial (así se evita "perder" el punto de partida)
    */

    /**
     * Retorna la posición en X del centroide.
     *
     * @return posición X actual
     */
    public float getPosicionX() {
        return posicionCentroideX;
    }

    /**
     * Retorna la posición en Y del centroide.
     *
     * @return posición Y actual
     */
    public float getPosicionY() {
        return posicionCentroideY;
    }

    /**
     * Modifica la posición (posicionCentroideX, posicionCentroideY)
     * del centroide del extraterrestre, calculándola como la
     * posición inicial más el desplazamiento recibido.
     *
     * @param desplazamientoCentroideX desplazamiento en X respecto a la posición inicial
     * @param desplazamientoCentroideY desplazamiento en Y respecto a la posición inicial
     */
    public void mover(float desplazamientoCentroideX, float desplazamientoCentroideY) {

        this.posicionCentroideX = this.posicionInicialCentroideX + desplazamientoCentroideX;
        this.posicionCentroideY = this.posicionInicialCentroideY + desplazamientoCentroideY;

    }

    /**
     * Modifica la posición angular del extraterrestre,
     * rotando alrededor de un eje que pasa por SU PROPIA
     * posición inicial (es decir, gira "sobre sí mismo").
     *
     * Nótese que este método se llama IGUAL que el anterior
     * ("mover"), pero recibe un solo parámetro en vez de dos:
     * esto es SOBRECARGA (overload), no sobrescritura - Java
     * decide cuál de los dos ejecutar según cuántos argumentos
     * le pasas al llamarlo.
     *
     * @param posicionAngular ángulo de rotación, en grados
     */
    public void mover(float posicionAngular) {
        this.posicionEjeRotacionX = this.posicionInicialCentroideX;
        this.posicionEjeRotacionY = this.posicionInicialCentroideY;

        this.posicionAngularRotacionEjeXY = posicionAngular;
    }

    /**
     * Modifica la posición (posicionCentroideX, posicionCentroideY)
     * del centroide del extraterrestre Y ADEMÁS genera una rotación
     * alrededor del eje que pasa por ese nuevo centroide (es decir,
     * combina desplazamiento con rotación "sobre sí mismo" en su
     * nueva posición). Esta es la tercera sobrecarga de "mover".
     *
     * @param desplazamientoCentroideX desplazamiento en X
     * @param desplazamientoCentroideY desplazamiento en Y
     * @param posicionAngular          ángulo de rotación, en grados
     */
    public void mover(float desplazamientoCentroideX, float desplazamientoCentroideY, float posicionAngular) {
        this.posicionCentroideX = this.posicionInicialCentroideX + desplazamientoCentroideX;
        this.posicionCentroideY = this.posicionInicialCentroideY + desplazamientoCentroideY;

        // el eje de rotación se ubica en la posición NUEVA
        // (recién calculada arriba), no en la inicial
        this.posicionEjeRotacionX = this.posicionCentroideX;
        this.posicionEjeRotacionY = this.posicionCentroideY;

        this.posicionAngularRotacionEjeXY = posicionAngular;
    }

    /**
     * Genera una rotación del extraterrestre a la posición
     * angular indicada, alrededor de un eje EXTERNO (dado por
     * posicionEjeRotacionX, posicionEjeRotacionY), que no tiene
     * por qué coincidir con el centroide del propio objeto -
     * por ejemplo, para simular que algo gira alrededor de otro
     * punto de la pantalla.
     *
     * @param posicionEjeRotacionX posición X del eje de rotación
     * @param posicionEjeRotacionY posición Y del eje de rotación
     * @param posicionAngular      ángulo de rotación, en grados
     */
    public void rotar(float posicionEjeRotacionX, float posicionEjeRotacionY, float posicionAngular) {

        this.posicionEjeRotacionX = posicionEjeRotacionX;
        this.posicionEjeRotacionY = posicionEjeRotacionY;

        this.posicionAngularRotacionEjeXY = posicionAngular;

    }

    /**
     * Cambia el factor de magnificación (escala) del extraterrestre,
     * tomando su propio centroide como punto de referencia del escalado
     * (así, al agrandarlo, no "se corre" de su posición).
     *
     * @param magnificacion nuevo factor de escala (1 = tamaño normal)
     */
    public void setMagnificar(float magnificacion) {
        this.magnificacion = magnificacion;
    }

    /**
     * dibujese() sigue siendo ABSTRACTO aquí: Extraterrestre
     * todavía no sabe cómo se ve un extraterrestre "en general"
     * (¿un Marciano? ¿un Selenita?), así que traspasa la obligación
     * heredada de Dibujable/ObjetoEspacial a sus propias subclases
     * (Marciano, Selenita, Venusiano), que SÍ la van a cumplir.
     */
    public abstract void dibujese(Canvas canvas, Paint pincel);
}
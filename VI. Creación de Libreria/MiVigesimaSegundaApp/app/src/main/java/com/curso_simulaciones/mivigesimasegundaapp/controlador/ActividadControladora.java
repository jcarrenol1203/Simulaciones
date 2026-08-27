package com.curso_simulaciones.mivigesimasegundaapp.controlador;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.curso_simulaciones.mivigesimasegundaapp.datos.AlmacenDatosRAM;
import com.curso_simulaciones.mivigesimasegundaapp.vista.CR;
import com.curso_simulaciones.mivigesimasegundaapp.vista.Pizarra;
import com.curso_simulaciones.simulphysics.objetos_laboratorio.Cuerda;
import com.curso_simulaciones.simulphysics.objetos_laboratorio.CuerpoRectangular;
import com.curso_simulaciones.simulphysics.objetos_laboratorio.Marca;
import com.curso_simulaciones.simulphysics.objetos_laboratorio.Masa;
import com.curso_simulaciones.simulphysics.objetos_laboratorio.ObjetoLaboratorio;
import com.curso_simulaciones.simulphysics.objetos_laboratorio.Polea;

public class ActividadControladora extends Activity {


    private Pizarra pizarra;

    private CuerpoRectangular suelo, columna, columnaBorde;
    private Polea polea_izquierda, polea_derecha, polea_P;
    private Cuerda cuerda_superior, cuerda_m3, cuerda_soporte_P, cuerda_P_m1, cuerda_P_m2;
    private Masa masa_1, masa_2, masa_3;
    private Marca marca_P, marca_m1, marca_m2, marca_m3;

    private ObjetoLaboratorio[] objetos = new ObjetoLaboratorio[20];


    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        gestionarResolucion();


        //para crear elementos de la GUI
        crearElementosGUI();

        //para informar cómo se debe pegar el adminitrador de
        //diseño obtenido con el método GUI
        ViewGroup.LayoutParams parametro_layout_principal = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);


        //pegar el contenedor con la GUI
        this.setContentView(crearGUI(), parametro_layout_principal);


    }//fin onCreate


    /*Método auxiliar para asuntos de resolución*/
    private void gestionarResolucion() {

        /*
        Según el diseño de la GUI se puede anticipar cuál es la
        dimensión de la pizarra. En este caso es el 100% del ancho
        de la pantalla y el 100% del alto de la misma
        */
        CR.anchoPizarra = AlmacenDatosRAM.ancho_pantalla;
        CR.altoPizarra = AlmacenDatosRAM.alto_pantalla;

    }

    /*método responsable de la creación de los elementos de la GUI*/
    private void crearElementosGUI() {


        //crear pizarra sabiendo de antemano sus dimensiones
        pizarra = new Pizarra(this);
        pizarra.setBackgroundColor(Color.WHITE);

        crearObjetosLaboratorio();


    }//fin crearElementosGUI


    /*método responsable de administrar el diseño de la GUI*/
    private LinearLayout crearGUI() {

        //el linear principal
        LinearLayout linear_principal = new LinearLayout(this);
        linear_principal.setOrientation(LinearLayout.VERTICAL);


        //pegar pizarra a linearArriba
        linear_principal.addView(pizarra);

        return linear_principal;

    }//fin crearGUI


    /*
   Crea los objetos cuerpo rígido con su estado inicial.
   -X esta en porcentaje del ancho del canvas
   -Y está en porcentaje del alto del canvas
   -Cualquier otra dimensión está en porcentaje del menor
    entre el alto y el ancho del canvas
   */
    private void crearObjetosLaboratorio() {

        /*
         Radios de las poleas: las dos fijas (azules) son más
         grandes que la móvil P (verde), igual que en la figura
        */
        float radioGrande = CR.pcApxL(7f);
        float radioChica = CR.pcApxL(5f);

        //dimensiones de cada masa (bloques delgados y altos)
        float anchoMasa = CR.pcApxL(6f);
        float altoMasa = CR.pcApxL(13f);

        /*
         OJO con la responsividad: CR.pcApxY usa el ALTO de la
         pantalla como referencia, y CR.pcApxL usa el ANCHO (el
         lado menor en vertical). En un teléfono muy alargado esas
         dos unidades casi coinciden, pero en una tablet (relación
         ancho/alto distinta) se separan bastante. Por eso, para
         que nada se choque sin importar el dispositivo, TODA la
         cadena vertical de aquí en adelante se arma en pcApxL
         (colgando cada pieza de la anterior), y pcApxY solo se usa
         UNA vez, como ancla de dónde empieza el piso.
        */
        float yPisoArriba = CR.pcApxY(72f);

        //estructura de soporte: una columna amarilla parada sobre un suelo negro
        float xColumna = CR.pcApxX(50f);
        float anchoColumna = CR.pcApxL(30f);
        float altoColumna = CR.pcApxL(58f);

        float xColumnaIzquierda = xColumna - 0.5f * anchoColumna;
        float xColumnaDerecha = xColumna + 0.5f * anchoColumna;

        //la columna se construye desde el piso hacia arriba (todo en pcApxL)
        float yColumnaAbajo = yPisoArriba;
        float yColumna = yColumnaAbajo - 0.5f * altoColumna;
        float yColumnaArriba = yColumnaAbajo - altoColumna;

        /*
         Las dos poleas fijas se anclan a los vértices superiores
         de la columna mediante su varilla de soporte (la que se
         dibuja en diagonal), no quedan centradas sobre el vértice.
         Por eso el CENTRO de cada polea se ubica un poco hacia
         arriba y hacia afuera del vértice (a 45°), a una distancia
         igual al largo de esa varilla (1.8*radio), de forma que
         la punta de la varilla SÍ caiga exactamente sobre el
         vértice de la columna. Así la varilla queda diagonal,
         pero como las dos poleas quedan a la misma altura, la
         cuerda que las une (y las que bajan de cada una) sigue
         siendo horizontal/vertical.
        */
        float largoVarilla = 1.8f * radioGrande;
        float diagonal = largoVarilla / (float) Math.sqrt(2.0);

        float xPoleaIzquierda = xColumnaIzquierda - diagonal;
        float xPoleaDerecha = xColumnaDerecha + diagonal;
        float yPoleasFijas = yColumnaArriba - diagonal;

        //polea izquierda: la varilla apunta hacia abajo y a la derecha (al vértice)
        float anguloVarillaIzquierda = -45f;
        //polea derecha: la varilla apunta hacia abajo y a la izquierda (al vértice), en espejo
        float anguloVarillaDerecha = 45f;

        /*
         El punto tangente correcto para una cuerda VERTICAL no es
         el punto de abajo del círculo (esa dirección apunta al
         centro, no es tangente): es el punto lateral (izquierda o
         derecha), igual que ya se hace para m1/m2 más abajo. Por
         eso P y m3 no cuelgan del centro de su polea sino de un
         punto corrido un radio hacia el lado.
        */
        float xP = xPoleaIzquierda - radioGrande;
        float xM1 = xP - radioChica;
        float xM2 = xP + radioChica;
        float xM3 = xPoleaDerecha + radioGrande;

        /*
         Largos de cuerda (todos en pcApxL): P cuelga de la polea
         izquierda, m1 y m2 cuelgan de P, y m3 cuelga de la polea
         derecha. Como todo está encadenado en la misma unidad, la
         proporción entre poleas/columna/cuerdas/masas se mantiene
         igual sin importar el ancho o alto real de la pantalla.
        */
        float longitudCuerdaSoporteP = CR.pcApxL(20f);
        float longitudCuerdaM1 = CR.pcApxL(10f);
        float longitudCuerdaM2 = CR.pcApxL(24f);
        float longitudCuerdaM3 = CR.pcApxL(16f);

        float yP = yPoleasFijas + longitudCuerdaSoporteP;
        float yM1 = yP + longitudCuerdaM1 + 0.5f * altoMasa;
        float yM2 = yP + longitudCuerdaM2 + 0.5f * altoMasa;
        float yM3 = yPoleasFijas + longitudCuerdaM3 + 0.5f * altoMasa;

        /*
        Creación de los objetos físicos y despliegue del
        estado inicial de la escena
        */

        //dimensiones del suelo: pegado justo debajo de la columna, sin espacio entre ambos
        float altoSuelo = CR.pcApxL(11f);

        /*
         Borde de la columna: como CuerpoRectangular siempre dibuja
         su contorno en negro (viene así fijo en la librería), se
         simula un borde amarillo oscuro dibujando primero un
         rectángulo un poco más grande de ese color por detrás, que
         asoma apenas por los bordes de la columna real.
        */
        float grosorBorde = CR.pcApxL(0.5f);
        columnaBorde = new CuerpoRectangular(xColumna, yColumna, anchoColumna + 2 * grosorBorde, altoColumna + 2 * grosorBorde);
        columnaBorde.setColor(Color.rgb(184, 141, 4));
        objetos[0] = columnaBorde;

        //la columna que sirve de estructura de soporte
        columna = new CuerpoRectangular(xColumna, yColumna, anchoColumna, altoColumna);
        columna.setColor(Color.rgb(252, 221, 63));
        objetos[1] = columna;

        /*
         El suelo se dibuja DESPUÉS del borde y la columna (índice
         más alto = se pinta encima), así tapa por completo la
         franja del borde oscuro que asoma por debajo de la columna.
        */
        suelo = new CuerpoRectangular(xColumna, yColumnaAbajo + 0.5f * altoSuelo, CR.pcApxL(76f), altoSuelo);
        suelo.setColor(Color.rgb(20, 20, 20));
        objetos[2] = suelo;

        //polea fija izquierda (varilla de soporte en diagonal, anclada al vértice)
        polea_izquierda = new Polea(xPoleaIzquierda, yPoleasFijas, radioGrande);
        polea_izquierda.setColor(Color.BLUE);
        polea_izquierda.setGrosorLinea(CR.pcApxL(0.6f));
        polea_izquierda.setSoportePolea(true);
        polea_izquierda.rotarEje(anguloVarillaIzquierda);
        objetos[3] = polea_izquierda;

        //polea fija derecha (varilla en espejo)
        polea_derecha = new Polea(xPoleaDerecha, yPoleasFijas, radioGrande);
        polea_derecha.setColor(Color.BLUE);
        polea_derecha.setGrosorLinea(CR.pcApxL(0.6f));
        polea_derecha.setSoportePolea(true);
        polea_derecha.rotarEje(anguloVarillaDerecha);
        objetos[4] = polea_derecha;

        //polea móvil P, colgada de la cuerda que baja de la polea izquierda
        polea_P = new Polea(xP, yP, radioChica);
        polea_P.setColor(Color.rgb(20, 140, 60));
        polea_P.setGrosorLinea(CR.pcApxL(0.6f));
        objetos[5] = polea_P;

        //cuerda superior horizontal, tangente al borde de arriba de ambas poleas fijas
        cuerda_superior = new Cuerda(xPoleaIzquierda, yPoleasFijas - radioGrande, xPoleaDerecha, yPoleasFijas - radioGrande);
        cuerda_superior.setColor(Color.rgb(150, 20, 45));
        cuerda_superior.setGrosorLinea(CR.pcApxL(0.5f));
        objetos[6] = cuerda_superior;

        //cuerda vertical de la polea derecha hasta la masa m3 (tangente al costado derecho)
        cuerda_m3 = new Cuerda(xM3, yPoleasFijas, xM3, yM3 - 0.5f * altoMasa);
        cuerda_m3.setColor(Color.rgb(150, 20, 45));
        cuerda_m3.setGrosorLinea(CR.pcApxL(0.5f));
        objetos[7] = cuerda_m3;

        //cuerda vertical que sostiene a la polea P, tangente al costado izquierdo de la polea izquierda
        cuerda_soporte_P = new Cuerda(xP, yPoleasFijas, xP, yP - radioChica);
        cuerda_soporte_P.setColor(Color.rgb(150, 20, 45));
        cuerda_soporte_P.setGrosorLinea(CR.pcApxL(0.5f));
        objetos[8] = cuerda_soporte_P;

        //cuerda vertical de la polea P hacia la masa m1 (costado izquierdo de P)
        cuerda_P_m1 = new Cuerda(xM1, yP, xM1, yM1 - 0.5f * altoMasa);
        cuerda_P_m1.setColor(Color.rgb(150, 20, 45));
        cuerda_P_m1.setGrosorLinea(CR.pcApxL(0.5f));
        objetos[9] = cuerda_P_m1;

        //cuerda vertical de la polea P hacia la masa m2 (costado derecho de P)
        cuerda_P_m2 = new Cuerda(xM2, yP, xM2, yM2 - 0.5f * altoMasa);
        cuerda_P_m2.setColor(Color.rgb(150, 20, 45));
        cuerda_P_m2.setGrosorLinea(CR.pcApxL(0.5f));
        objetos[10] = cuerda_P_m2;

        //las tres masas (bloques negros)
        masa_1 = new Masa(xM1, yM1, anchoMasa, altoMasa);
        masa_1.setColor(Color.rgb(20, 20, 20));
        objetos[11] = masa_1;

        masa_2 = new Masa(xM2, yM2, anchoMasa, altoMasa);
        masa_2.setColor(Color.rgb(20, 20, 20));
        objetos[12] = masa_2;

        masa_3 = new Masa(xM3, yM3, anchoMasa, altoMasa);
        masa_3.setColor(Color.rgb(20, 20, 20));
        objetos[13] = masa_3;

        //etiquetas de texto, por fuera de los bloques, con subíndice real como en la figura
        marca_P = new Marca("P", xP - CR.pcApxL(3.5f), yP - radioChica - CR.pcApxL(2f));
        marca_P.setColor(Color.BLACK);
        marca_P.setTamano(CR.pcApxL(3.5f));
        objetos[14] = marca_P;

        marca_m1 = new Marca("m₁", xM1 - CR.pcApxL(2f), yM1 + 0.5f * altoMasa + CR.pcApxL(4f));
        marca_m1.setColor(Color.BLACK);
        marca_m1.setTamano(CR.pcApxL(3.5f));
        objetos[15] = marca_m1;

        marca_m2 = new Marca("m₂", xM2 - CR.pcApxL(2f), yM2 + 0.5f * altoMasa + CR.pcApxL(4f));
        marca_m2.setColor(Color.BLACK);
        marca_m2.setTamano(CR.pcApxL(3.5f));
        objetos[16] = marca_m2;

        marca_m3 = new Marca("m₃", xM3 - CR.pcApxL(2f), yM3 + 0.5f * altoMasa + CR.pcApxL(4f));
        marca_m3.setColor(Color.BLACK);
        marca_m3.setTamano(CR.pcApxL(3.5f));
        objetos[17] = marca_m3;

        //desplegar la escena inicial
        pizarra.setEstadoEscena(objetos);

    }


}

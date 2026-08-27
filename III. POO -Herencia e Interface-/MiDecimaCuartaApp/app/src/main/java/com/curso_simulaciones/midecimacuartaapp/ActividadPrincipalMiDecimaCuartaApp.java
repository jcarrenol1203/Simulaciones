package com.curso_simulaciones.midecimacuartaapp;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.curso_simulaciones.midecimacuartaapp.elementos_del_espacio.EstrellaFija;
import com.curso_simulaciones.midecimacuartaapp.elementos_del_espacio.Marciano;
import com.curso_simulaciones.midecimacuartaapp.elementos_del_espacio.ObjetoEspacial;
import com.curso_simulaciones.midecimacuartaapp.elementos_del_espacio.Selenita;
import com.curso_simulaciones.midecimacuartaapp.elementos_del_espacio.Venusiano;
import com.curso_simulaciones.midecimacuartaapp.vista.CR;
import com.curso_simulaciones.midecimacuartaapp.vista.Pizarra;

/**
 * Activity principal: crea la GUI, arranca el hilo de animación,
 * y va cambiando el estado (posición/rotación/tamaño/color) de
 * cada objeto espacial en cada "tick" del hilo.
 *
 * "implements Runnable" es lo que permite pasar "this" a
 * "new Thread(this)" más abajo: el Thread solo necesita que
 * este objeto tenga un método run(), sin importarle que además
 * sea una Activity.
 */
public class ActividadPrincipalMiDecimaCuartaApp extends Activity implements Runnable {

    //Pizarra para dibujar
    Pizarra pizarra;
    //objetos dibujables para Pizarra
    private Marciano marciano_1;
    private Selenita selenita_1;
    private Venusiano venusiano_1;
    private EstrellaFija estrella_1, estrella_2, estrella_3, estrella_4, estrella_5;
    //arreglo de tipo ObjetoEspacial: aquí está el polimorfismo -
    //en las mismas casillas conviven Marciano, Selenita, Venusiano
    //y EstrellaFija, aunque no todos hereden de la misma clase
    //intermedia (solo comparten el ancestro común ObjetoEspacial)
    private ObjetoEspacial[] objetos_espaciales = new ObjetoEspacial[10];

    //factor de magnificación acumulado del marciano_1 (crece con el tiempo)
    private float aumento_1 = 1;


    //período de muestreo en milisegundos
    private long periodo_muestreo = 50;
    private float tiempo;
    //hilo responsable de controlar la animación
    private Thread hilo;


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /*
        llamada al método para crear los elementos de la
        interfaz gráfica de usuario (GUI)
        */
        crearElementosGui();

        /*
        para informar cómo se debe adaptar la GUI a la pantalla del dispositivo
        */
        ViewGroup.LayoutParams parametro_layout_principal = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

        /*
        pegar al contenedor la GUI:
        en el argumento se está llamando al método crearGui()
        */
        this.setContentView(crearGui(), parametro_layout_principal);

        /*
         los objetos espaciales con responsividad se crearán dentro
         del hilo con el fin de garantizar que las dimensiones
         de la pizarra donde se desplegarán con responsividad
         tenga ya dimensiones no nulas
         */
        //hilo que administra la animación
        hilo = new Thread(this);
        hilo.start();

    }


    //crear los objetos de la interfaz gráfica de usuario (GUI)
    private void crearElementosGui() {

        //crear pizarra sabiendo de antemano sus dimensiones
        pizarra = new Pizarra(this);
        pizarra.setBackgroundColor(Color.WHITE);
    }


    //organizar la distribución de los objetos de la GUI usando administradores de diseño
    private LinearLayout crearGui() {

        //el linear principal
        LinearLayout linearPrincipal = new LinearLayout(this);
        linearPrincipal.setOrientation(LinearLayout.VERTICAL);
        linearPrincipal.setBackgroundColor(Color.BLACK);
        linearPrincipal.setWeightSum(10.0f);


        //linear secundario arriba
        LinearLayout linearArriba = new LinearLayout(this);


        //linear secundario abajo
        LinearLayout linearAbajo = new LinearLayout(this);
        linearAbajo.setBackgroundColor(Color.YELLOW);


        //pegar linearArriba al principal
        LinearLayout.LayoutParams parametrosPegadoArriba = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0);
        parametrosPegadoArriba.weight = 8.5f;
        parametrosPegadoArriba.setMargins(50, 50, 50, 50);
        linearPrincipal.addView(linearArriba, parametrosPegadoArriba);


        //pegar linearAbajo al principal
        LinearLayout.LayoutParams parametrosPegadoAbajo = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0);
        parametrosPegadoAbajo.weight = 1.5f;
        linearPrincipal.addView(linearAbajo, parametrosPegadoAbajo);


        //pegar pizarra a linearArriba
        linearArriba.addView(pizarra);


        return linearPrincipal;

    }


    @Override
    public void run() {

        boolean ON = true;

        //hilo sin fin
        while (true) {

            try {
                Thread.sleep(periodo_muestreo);
            } catch (InterruptedException e) {
                e.printStackTrace();

            }

            /*
            hacer la creación de los objetos espaciales con
            responsividad SÓLO cuando se garantice que la
            GUI se conformó completamente con el fin de que
            las dimensiones de la pizarra NO SEAN NULAS.
            */
            if (pizarra.getWidth() != 0 && ON == true) {
                crearObjetosConResponsividad();
                ON = false;
            }

            //ya creados los objetos con responsividad, hacer efectiva la animación
            if (ON == false) {
                tiempo = tiempo + 0.05f;
                //cambio de estado de la escena física en la pizarra
                cambiarEstadosEscenaPizarra(tiempo);
            }


        }

    }

    private void crearObjetosConResponsividad() {

        CR.anchoPizarra = pizarra.getWidth();
        CR.altoPizarra = pizarra.getHeight();
        crearObjetosLaboratorio();

    }

    /*
    Crea los objetos espaciales con su estado inicial
    -X esta en porcentaje del ancho del canvas
    -Y está en porcentaje del alto del canvas
    -Cualquier otra dimensión está en porcentaje del menor
     entre el alto y el ancho del canvas
    */
    private void crearObjetosLaboratorio() {

        //marciano 1: arranca abajo a la izquierda, saldrá "disparado" parabólicamente
        float x_1 = CR.pcApxX(10);
        float y_1 = CR.pcApxY(75);
        marciano_1 = new Marciano(x_1, y_1);
        //agregarlo al arreglo de objetos espaciales
        objetos_espaciales[1] = marciano_1;

        //selenita 1: arranca en el centro-izquierda de la pizarra
        float x_2 = CR.pcApxX(45);
        float y_2 = CR.pcApxY(30);
        selenita_1 = new Selenita(x_2, y_2);
        //agregarlo al arreglo de objetos espaciales
        objetos_espaciales[2] = selenita_1;

        //venusiano 1: arranca en el centro-derecha de la pizarra
        float x_3 = CR.pcApxX(70);
        float y_3 = CR.pcApxY(55);
        venusiano_1 = new Venusiano(x_3, y_3);
        //agregarlo al arreglo de objetos espaciales
        objetos_espaciales[3] = venusiano_1;

        //5 estrellas fijas: distintas posiciones, tamaños y colores,
        //repartidas por la parte superior de la pizarra.
        //El radio va de 1.5% a 5% del canvas -a propósito un rango bien
        //amplio- para que la diferencia de tamaño entre ellas sea obvia
        //a simple vista, no solo detectable si se comparan con cuidado.
        float xEstrella_1 = CR.pcApxX(15);
        float yEstrella_1 = CR.pcApxY(18);
        float radioEstrella_1 = CR.pcApxL(1.5f);
        estrella_1 = new EstrellaFija(xEstrella_1, yEstrella_1, radioEstrella_1);
        estrella_1.setColor(Color.YELLOW);
        objetos_espaciales[4] = estrella_1;

        float xEstrella_2 = CR.pcApxX(32);
        float yEstrella_2 = CR.pcApxY(10);
        float radioEstrella_2 = CR.pcApxL(3.2f);
        estrella_2 = new EstrellaFija(xEstrella_2, yEstrella_2, radioEstrella_2);
        estrella_2.setColor(Color.CYAN);
        objetos_espaciales[5] = estrella_2;

        float xEstrella_3 = CR.pcApxX(50);
        float yEstrella_3 = CR.pcApxY(15);
        float radioEstrella_3 = CR.pcApxL(5f);
        estrella_3 = new EstrellaFija(xEstrella_3, yEstrella_3, radioEstrella_3);
        estrella_3.setColor(Color.MAGENTA);
        objetos_espaciales[6] = estrella_3;

        float xEstrella_4 = CR.pcApxX(68);
        float yEstrella_4 = CR.pcApxY(10);
        float radioEstrella_4 = CR.pcApxL(2.2f);
        estrella_4 = new EstrellaFija(xEstrella_4, yEstrella_4, radioEstrella_4);
        estrella_4.setColor(Color.RED);
        objetos_espaciales[7] = estrella_4;

        float xEstrella_5 = CR.pcApxX(88);
        float yEstrella_5 = CR.pcApxY(20);
        float radioEstrella_5 = CR.pcApxL(4f);
        estrella_5 = new EstrellaFija(xEstrella_5, yEstrella_5, radioEstrella_5);
        estrella_5.setColor(Color.GREEN);
        objetos_espaciales[8] = estrella_5;

        //desplegar la escena inicial
        pizarra.setEstadoEscena(objetos_espaciales);

    }


    /*
    Cambia el estado de movimiento de los objetos espaciales
    -X esta en porcentaje del ancho del canvas
    -Y está en porcentaje del alto del canvas
    -Cualquier otra dimensión está en porcentaje del menor
     entre el alto y el ancho del canvas

    Las 5 EstrellaFija NO se tocan aquí: son "fijas" por definición,
    su posición/tamaño/color quedaron establecidos de una sola vez
    en crearObjetosLaboratorio() y nunca vuelven a cambiar.
    */
    private void cambiarEstadosEscenaPizarra(float tiempo) {

        //modelo de la marciano_1
        //movimiento parabólico
        float desplazamiento_x_1 = CR.pcApxX(10 * tiempo); //MU
        float desplazamiento_y_1 = CR.pcApxY(-35 * tiempo + 4.9f * tiempo * tiempo); //caida libre
        //mover al marciano
        marciano_1.mover(desplazamiento_x_1, desplazamiento_y_1);
        //aumentar tamaño de marciano_1
        aumento_1 = aumento_1 + 0.01f;
        marciano_1.setMagnificar(aumento_1);
        //cambiar color de marciano_1 en el instante que su tamaño
        //esté en 40% mayor que el inicial
        if (aumento_1 > 1.4f) {
            marciano_1.setColor(Color.GREEN);
        }

        //modelo de la selenita_1
        //MU horizontal + MCU alrededor de su propio centroide
        float desplazamiento_x_2 = CR.pcApxX(5 * tiempo); //MU
        float teta_2 = 80 * tiempo; //MCU, en grados
        //mover(dx, dy, angulo): desplaza Y ADEMÁS rota alrededor
        //del centroide resultante (heredado de Extraterrestre)
        selenita_1.mover(desplazamiento_x_2, 0, teta_2);

        //modelo de la venusiano_1
        //MU horizontal + MAS (oscilación armónica) en vertical
        float desplazamiento_x_3 = CR.pcApxX(4 * tiempo); //MU
        float frecuencia_3 = 1f; //Hz
        float fase_en_radianes_3 = (float) (2 * Math.PI * frecuencia_3 * tiempo);
        float amplitud_en_porcentaje_3 = 8f;
        float desplazamiento_y_3 = CR.pcApxY(amplitud_en_porcentaje_3 * (float) Math.sin(fase_en_radianes_3));
        //mover(dx, dy): solo desplazamiento, sin rotación, tal como pide el enunciado
        venusiano_1.mover(desplazamiento_x_3, desplazamiento_y_3);

    }
}
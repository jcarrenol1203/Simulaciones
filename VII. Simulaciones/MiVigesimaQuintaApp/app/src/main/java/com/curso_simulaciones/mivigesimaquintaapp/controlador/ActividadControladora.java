package com.curso_simulaciones.mivigesimaquintaapp.controlador;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.curso_simulaciones.mivigesimaquintaapp.datos.AlmacenDatosRAM;
import com.curso_simulaciones.mivigesimaquintaapp.vista.CR;
import com.curso_simulaciones.mivigesimaquintaapp.vista.EtiquetaMovil;
import com.curso_simulaciones.mivigesimaquintaapp.vista.FlechaRellena;
import com.curso_simulaciones.mivigesimaquintaapp.vista.Pizarra;
import com.curso_simulaciones.simulphysics.objetos_laboratorio.Cuerda;
import com.curso_simulaciones.simulphysics.objetos_laboratorio.CuerpoRectangular;
import com.curso_simulaciones.simulphysics.objetos_laboratorio.Marca;
import com.curso_simulaciones.simulphysics.objetos_laboratorio.Masa;
import com.curso_simulaciones.simulphysics.objetos_laboratorio.ObjetoLaboratorio;
import com.curso_simulaciones.simulphysics.objetos_laboratorio.Polea;

public class ActividadControladora extends Activity {

    //variable tamaño de las letras basado en resolución de pantalla
    private int tamanoLetraResolucionIncluida;

    private Pizarra pizarra;

    //Objetos GUI de control (masas y botones)
    private TextView text_m1, text_m2, text_m3;
    private SeekBar seek_bar_m1, seek_bar_m2, seek_bar_m3;
    private Button boton_empezar, boton_pausar;

    //valor mínimo y máximo del rango de cada masa, en kg (unidades SI)
    private final float masaMinima = 1f;
    private final int masaRangoSeekBar = 24; //masas entre 1 kg y 25 kg

    //valores iniciales de las masas, en kg (cerca del equilibrio para
    //que la primera corrida se vea a ritmo pausado; el estudiante
    //puede alejarlas del equilibrio con los deslizadores)
    private float m1 = 8, m2 = 8, m3 = 15;

    //geometría de la escena (todo en pixeles, cadena de CR.pcApxL como en MiVigesimaSegundaApp)
    private float radioGrande, radioChica;
    private float anchoMasa, altoMasa;
    private float xP, xM1, xM2, xM3;
    private float yPoleasFijas, yPisoArriba;

    //objetos de laboratorio de la escena física
    private CuerpoRectangular suelo, columna, columnaBorde;
    private Polea polea_izquierda, polea_derecha, polea_P;
    private Cuerda cuerda_superior, cuerda_m3, cuerda_soporte_P, cuerda_P_m1, cuerda_P_m2;
    private Masa masa_1, masa_2, masa_3;
    private EtiquetaMovil marca_P, marca_m1, marca_m2, marca_m3;

    private ObjetoLaboratorio[] objetos = new ObjetoLaboratorio[24];

    //hilo responsable de controlar la animación
    private HiloAnimacion hilo;

    //true solo cuando ya se construyó la escena física (una vez se conoce
    //el tamaño real de la pizarra); evita tocar objetos aún no creados
    private boolean escenaLista = false;
    private int anchoPizarraConocido = -1, altoPizarraConocido = -1;


    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        gestionarResolucion();

        //para crear elementos de la GUI
        crearElementosGUI();

        //para informar cómo se debe pegar el administrador de
        //diseño obtenido con el método GUI
        ViewGroup.LayoutParams parametro_layout_principal = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

        //pegar el contenedor con la GUI
        this.setContentView(crearGUI(), parametro_layout_principal);

        //valores iniciales de masas y posiciones en AlmacenDatosRAM
        actualizarValoresIniciales();

        //hilo que administra los cálculos
        hilo = new HiloAnimacion(this);
        hilo.start();

        //para administrar los eventos
        eventos();

    }//fin onCreate


    /*Método auxiliar para asuntos de resolución*/
    private void gestionarResolucion() {

        //este tamaño de letra sí puede estimarse con la resolución en
        //portrait: solo se usa para los controles de la derecha, cuyo
        //ancho relativo (20%) no cambia con la orientación
        tamanoLetraResolucionIncluida = (int) (0.8f * AlmacenDatosRAM.tamanoLetraResolucionIncluida);

        /*
        Las dimensiones de la PIZARRA (CR.anchoPizarra/altoPizarra), en
        cambio, NO se estiman a partir de la resolución en portrait:
        esa estimación (intercambiar alto y ancho) no es exacta en
        todos los dispositivos (barras de sistema, notch, tablets con
        otra proporción de pantalla), y era la causa de que la escena
        se viera cortada o corrida según el dispositivo. En vez de
        eso, se usa el tamaño REAL con el que Android termina midiendo
        la propia View de la pizarra (ver Pizarra.onSizeChanged /
        setEscuchaTamano más abajo), que es responsivo en cualquier
        celular o tablet porque ya tiene en cuenta la proporción 80/20
        de la GUI, la orientación y las barras de sistema reales.
        */

    }

    /*método responsable de la creación de los elementos de la GUI*/
    private void crearElementosGUI() {

        text_m1 = crearTextoMasa("MASA m1 (kg)");
        text_m2 = crearTextoMasa("MASA m2 (kg)");
        text_m3 = crearTextoMasa("MASA m3 (kg)");

        seek_bar_m1 = crearSeekBarMasa(m1);
        seek_bar_m2 = crearSeekBarMasa(m2);
        seek_bar_m3 = crearSeekBarMasa(m3);

        boton_empezar = new Button(this);
        boton_empezar.setTextSize(TypedValue.COMPLEX_UNIT_SP, tamanoLetraResolucionIncluida);
        boton_empezar.setText("EMPEZAR");
        boton_empezar.getBackground().setColorFilter(Color.rgb(220, 156, 80), PorterDuff.Mode.MULTIPLY);

        boton_pausar = new Button(this);
        boton_pausar.setTextSize(TypedValue.COMPLEX_UNIT_SP, tamanoLetraResolucionIncluida);
        boton_pausar.setText("PAUSAR");
        boton_pausar.getBackground().setColorFilter(Color.rgb(220, 156, 80), PorterDuff.Mode.MULTIPLY);
        boton_pausar.setEnabled(false);

        //crear la pizarra: la escena física se construye más adelante,
        //cuando Android informe el tamaño real con el que la dibujó
        //(ver setEscuchaTamano), no aquí
        pizarra = new Pizarra(this);
        pizarra.setBackgroundColor(Color.WHITE);
        pizarra.setEscuchaTamano(new Pizarra.EscuchaTamano() {
            public void onTamanoDisponible(int ancho, int alto) {

                //evita reconstruir la escena si el tamaño no cambió
                if (ancho == anchoPizarraConocido && alto == altoPizarraConocido) return;

                anchoPizarraConocido = ancho;
                altoPizarraConocido = alto;

                CR.anchoPizarra = ancho;
                CR.altoPizarra = alto;

                crearObjetosLaboratorio();
            }
        });

    }//fin crearElementosGUI

    private TextView crearTextoMasa(String etiqueta) {

        TextView texto = new TextView(this);
        texto.setTextSize(TypedValue.COMPLEX_UNIT_SP, tamanoLetraResolucionIncluida);
        texto.setGravity(Gravity.CENTER);
        texto.setTextColor(Color.BLACK);
        texto.setText(etiqueta + "\n" + (int) masaMinima + " a " + (int) (masaMinima + masaRangoSeekBar) + " kg");

        return texto;
    }

    private SeekBar crearSeekBarMasa(float valorInicial) {

        SeekBar seekBar = new SeekBar(this);
        seekBar.setMax(masaRangoSeekBar);
        seekBar.setProgress((int) (valorInicial - masaMinima));
        seekBar.setScaleY(0.6f);

        return seekBar;
    }


    /*método responsable de administrar el diseño de la GUI*/
    private LinearLayout crearGUI() {

        //el linear principal
        LinearLayout linear_principal = new LinearLayout(this);
        linear_principal.setOrientation(LinearLayout.HORIZONTAL);
        linear_principal.setWeightSum(10.0f);

        //linear secundario izquierda: la pizarra (escena + valores)
        LinearLayout linear_izquierda = new LinearLayout(this);
        linear_izquierda.setWeightSum(1.0f);

        //linear secundario derecha: los controles
        LinearLayout linear_derecha = new LinearLayout(this);
        linear_derecha.setBackgroundColor(Color.YELLOW);
        linear_derecha.setWeightSum(8.0f);

        //pegar estos secundarios al principal: 80% / 20%, como en la Figura 12B
        LinearLayout.LayoutParams parametros_pegado_izquierdo = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT);
        parametros_pegado_izquierdo.weight = 8.0f;
        linear_principal.addView(linear_izquierda, parametros_pegado_izquierdo);

        LinearLayout.LayoutParams parametros_pegado_derecho = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT);
        parametros_pegado_derecho.weight = 2.0f;
        linear_principal.addView(linear_derecha, parametros_pegado_derecho);

        //pegar componentes a linear_derecha (verticalmente)
        linear_derecha.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams parametros_pegado_componentes = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0);
        parametros_pegado_componentes.setMargins(5, 5, 5, 5);
        parametros_pegado_componentes.weight = 1.0f;

        linear_derecha.addView(text_m1, parametros_pegado_componentes);
        linear_derecha.addView(seek_bar_m1, parametros_pegado_componentes);
        linear_derecha.addView(text_m2, parametros_pegado_componentes);
        linear_derecha.addView(seek_bar_m2, parametros_pegado_componentes);
        linear_derecha.addView(text_m3, parametros_pegado_componentes);
        linear_derecha.addView(seek_bar_m3, parametros_pegado_componentes);
        linear_derecha.addView(boton_empezar, parametros_pegado_componentes);
        linear_derecha.addView(boton_pausar, parametros_pegado_componentes);

        //pegar la pizarra al linear izquierdo, con tamaño explícito
        //(evitar WRAP_CONTENT en una View sin tamaño intrínseco propio)
        linear_izquierda.setOrientation(LinearLayout.VERTICAL);
        linear_izquierda.addView(pizarra, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        return linear_principal;
    }//fin gui


    private void eventos() {

        boton_empezar.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                if (boton_empezar.getText() == "EMPEZAR") {

                    hilo.pausa = false;
                    habilitarSliders(false);
                    boton_empezar.setText("NUEVO");
                    boton_pausar.setText("PAUSAR");
                    boton_pausar.setEnabled(true);

                } else {

                    hilo.pausa = true;
                    hilo.tiempo = 0f;
                    habilitarSliders(true);
                    boton_empezar.setText("EMPEZAR");
                    boton_pausar.setEnabled(false);

                }

            }
        });

        boton_pausar.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                if (boton_pausar.getText() == "PAUSAR") {

                    boton_pausar.setText("CONTINUAR");
                    hilo.pausa = true;

                } else {

                    boton_pausar.setText("PAUSAR");
                    hilo.pausa = false;

                }

            }
        });

        seek_bar_m1.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                m1 = masaMinima + progress;
                actualizarValoresIniciales();
                hilo.tiempo = 0f; //nueva configuración de masas: se vuelve a soltar desde el reposo
            }

            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        seek_bar_m2.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                m2 = masaMinima + progress;
                actualizarValoresIniciales();
                hilo.tiempo = 0f;
            }

            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        seek_bar_m3.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                m3 = masaMinima + progress;
                actualizarValoresIniciales();
                hilo.tiempo = 0f;
            }

            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

    }

    private void habilitarSliders(boolean habilitar) {
        seek_bar_m1.setEnabled(habilitar);
        seek_bar_m2.setEnabled(habilitar);
        seek_bar_m3.setEnabled(habilitar);
    }


    /*
    Crea los objetos cuerpo rígido con su estado inicial, EXACTAMENTE
    con la misma disposición geométrica (poleas y masas) que
    MiVigesimaSegundaApp: columna sobre un suelo, dos poleas fijas
    ancladas a las esquinas superiores, una polea móvil P colgada de
    la izquierda con m1 y m2 a sus lados, y m3 colgando de la polea
    derecha. Todo en porcentaje vía CR para mantener responsividad.
    */
    private void crearObjetosLaboratorio() {

        radioGrande = CR.pcApxL(7f);
        radioChica = CR.pcApxL(5f);
        AlmacenDatosRAM.radioGrande = radioGrande;
        AlmacenDatosRAM.radioChica = radioChica;

        anchoMasa = CR.pcApxL(6f);
        altoMasa = CR.pcApxL(13f);
        AlmacenDatosRAM.semiAltoMasa = 0.5f * altoMasa;

        /*
         Con yPisoArriba = 72% (el valor original de MiVigesimaSegundaApp)
         la parte de arriba de las poleas fijas queda en y ≈ -1.9%, es
         decir, por FUERA del lienzo (arriba del todo) en cualquier
         dispositivo — es un margen que ya venía mal en la escena
         estática original (nunca se había probado en un celular real).
         Subiendo el piso a 78% se le da a toda la cadena (columna,
         poleas, P, m1, m2, m3) un margen de ~4% antes del borde
         superior, sin tocar ninguna proporción relativa entre ellos:
         es un corrimiento vertical rígido de toda la disposición, no
         un cambio de la disposición en sí.
        */
        yPisoArriba = CR.pcApxY(78f);

        /*
         Corrida ~9% a la derecha (del 50% original al 59%) para que
         el conjunto no se encime con el panel de valores que se
         dibuja a la izquierda de la pizarra; es un corrimiento
         horizontal rígido de todo el conjunto, no un cambio de la
         disposición en sí.
        */
        float xColumna = CR.pcApxX(59f);
        float anchoColumna = CR.pcApxL(30f);
        float altoColumna = CR.pcApxL(58f);

        float xColumnaIzquierda = xColumna - 0.5f * anchoColumna;
        float xColumnaDerecha = xColumna + 0.5f * anchoColumna;

        float yColumnaAbajo = yPisoArriba;
        float yColumna = yColumnaAbajo - 0.5f * altoColumna;
        float yColumnaArriba = yColumnaAbajo - altoColumna;

        float largoVarilla = 1.8f * radioGrande;
        float diagonal = largoVarilla / (float) Math.sqrt(2.0);

        float xPoleaIzquierda = xColumnaIzquierda - diagonal;
        float xPoleaDerecha = xColumnaDerecha + diagonal;
        yPoleasFijas = yColumnaArriba - diagonal;

        float anguloVarillaIzquierda = -45f;
        float anguloVarillaDerecha = 45f;

        xP = xPoleaIzquierda - radioGrande;
        xM1 = xP - radioChica;
        xM2 = xP + radioChica;
        xM3 = xPoleaDerecha + radioGrande;

        float longitudCuerdaSoporteP = CR.pcApxL(20f);
        float longitudCuerdaM1 = CR.pcApxL(10f);
        float longitudCuerdaM2 = CR.pcApxL(24f);
        float longitudCuerdaM3 = CR.pcApxL(16f);

        float yP = yPoleasFijas + longitudCuerdaSoporteP;
        float yM1 = yP + longitudCuerdaM1 + 0.5f * altoMasa;
        float yM2 = yP + longitudCuerdaM2 + 0.5f * altoMasa;
        float yM3 = yPoleasFijas + longitudCuerdaM3 + 0.5f * altoMasa;

        //posiciones y límites geométricos guardados para el modelo físico y el hilo
        AlmacenDatosRAM.yPoleasFijas_en_pixeles = yPoleasFijas;
        AlmacenDatosRAM.yPisoArriba_en_pixeles = yPisoArriba;

        AlmacenDatosRAM.x1_en_pixeles = xM1;
        AlmacenDatosRAM.x2_en_pixeles = xM2;
        AlmacenDatosRAM.x3_en_pixeles = xM3;
        AlmacenDatosRAM.xP_en_pixeles = xP;

        AlmacenDatosRAM.yi1_en_pixeles = yM1;
        AlmacenDatosRAM.yi2_en_pixeles = yM2;
        AlmacenDatosRAM.yi3_en_pixeles = yM3;
        AlmacenDatosRAM.yiP_en_pixeles = yP;

        float altoSuelo = CR.pcApxL(11f);

        float grosorBorde = CR.pcApxL(0.5f);
        columnaBorde = new CuerpoRectangular(xColumna, yColumna, anchoColumna + 2 * grosorBorde, altoColumna + 2 * grosorBorde);
        columnaBorde.setColor(Color.rgb(184, 141, 4));
        objetos[0] = columnaBorde;

        columna = new CuerpoRectangular(xColumna, yColumna, anchoColumna, altoColumna);
        columna.setColor(Color.rgb(252, 221, 63));
        objetos[1] = columna;

        suelo = new CuerpoRectangular(xColumna, yColumnaAbajo + 0.5f * altoSuelo, CR.pcApxL(76f), altoSuelo);
        suelo.setColor(Color.rgb(20, 20, 20));
        objetos[2] = suelo;

        polea_izquierda = new Polea(xPoleaIzquierda, yPoleasFijas, radioGrande);
        polea_izquierda.setColor(Color.BLUE);
        polea_izquierda.setGrosorLinea(CR.pcApxL(0.6f));
        polea_izquierda.setSoportePolea(true);
        polea_izquierda.rotarEje(anguloVarillaIzquierda);
        objetos[3] = polea_izquierda;

        polea_derecha = new Polea(xPoleaDerecha, yPoleasFijas, radioGrande);
        polea_derecha.setColor(Color.BLUE);
        polea_derecha.setGrosorLinea(CR.pcApxL(0.6f));
        polea_derecha.setSoportePolea(true);
        polea_derecha.rotarEje(anguloVarillaDerecha);
        objetos[4] = polea_derecha;

        polea_P = new Polea(xP, yP, radioChica);
        polea_P.setColor(Color.rgb(20, 140, 60));
        polea_P.setGrosorLinea(CR.pcApxL(0.6f));
        objetos[5] = polea_P;

        cuerda_superior = new Cuerda(xPoleaIzquierda, yPoleasFijas - radioGrande, xPoleaDerecha, yPoleasFijas - radioGrande);
        cuerda_superior.setColor(Color.rgb(150, 20, 45));
        cuerda_superior.setGrosorLinea(CR.pcApxL(0.5f));
        objetos[6] = cuerda_superior;

        cuerda_m3 = new Cuerda(xM3, yPoleasFijas, xM3, yM3 - 0.5f * altoMasa);
        cuerda_m3.setColor(Color.rgb(150, 20, 45));
        cuerda_m3.setGrosorLinea(CR.pcApxL(0.5f));
        objetos[7] = cuerda_m3;

        cuerda_soporte_P = new Cuerda(xP, yPoleasFijas, xP, yP - radioChica);
        cuerda_soporte_P.setColor(Color.rgb(150, 20, 45));
        cuerda_soporte_P.setGrosorLinea(CR.pcApxL(0.5f));
        objetos[8] = cuerda_soporte_P;

        cuerda_P_m1 = new Cuerda(xM1, yP, xM1, yM1 - 0.5f * altoMasa);
        cuerda_P_m1.setColor(Color.rgb(150, 20, 45));
        cuerda_P_m1.setGrosorLinea(CR.pcApxL(0.5f));
        objetos[9] = cuerda_P_m1;

        cuerda_P_m2 = new Cuerda(xM2, yP, xM2, yM2 - 0.5f * altoMasa);
        cuerda_P_m2.setColor(Color.rgb(150, 20, 45));
        cuerda_P_m2.setGrosorLinea(CR.pcApxL(0.5f));
        objetos[10] = cuerda_P_m2;

        //las tres masas: bloques lisos, sin etiqueta interna
        masa_1 = new Masa(xM1, yM1, anchoMasa, altoMasa);
        masa_1.setColor(Color.rgb(20, 20, 20));
        objetos[11] = masa_1;

        masa_2 = new Masa(xM2, yM2, anchoMasa, altoMasa);
        masa_2.setColor(Color.rgb(20, 20, 20));
        objetos[12] = masa_2;

        masa_3 = new Masa(xM3, yM3, anchoMasa, altoMasa);
        masa_3.setColor(Color.rgb(20, 20, 20));
        objetos[13] = masa_3;

        /*
         Etiquetas por fuera de cada bloque/polea, con subíndice real
         (m₁, m₂, m₃), igual que en MiVigesimaSegundaApp. A diferencia
         de esa app (donde nada se movía), aquí P, m1, m2 y m3 sí se
         desplazan verticalmente durante la animación, así que las
         cuatro etiquetas se reubican cuadro a cuadro en
         cambiarEstadosEscenaPizarra() — por eso son EtiquetaMovil y
         no Marca. m1 y m2 quedan hacia afuera de P (izquierda y
         derecha respectivamente); m3, hacia afuera de la columna.
        */
        marca_P = new EtiquetaMovil("P", xP - CR.pcApxL(3.5f), yP - radioChica - CR.pcApxL(2f));
        marca_P.setColor(Color.BLACK);
        marca_P.setTamano(CR.pcApxL(3.5f));
        objetos[14] = marca_P;

        /*
         drawText dibuja con el origen en el borde IZQUIERDO del
         texto (el texto crece hacia la derecha desde posicionX). Por
         eso m1 necesita un corrimiento grande hacia la izquierda: no
         solo tiene que despejar medio bloque (anchoMasa/2 = 3%), sino
         además el ANCHO del propio texto "m₁" (~4-4.5% con este tamaño
         de letra), para que el texto completo quede a la izquierda
         del bloque y no se encime con él.
        */
        marca_m1 = new EtiquetaMovil("m₁", xM1 - CR.pcApxL(9f), yM1 + CR.pcApxL(1f));
        marca_m1.setColor(Color.BLACK);
        marca_m1.setTamano(CR.pcApxL(3.5f));
        objetos[15] = marca_m1;

        marca_m2 = new EtiquetaMovil("m₂", xM2 + CR.pcApxL(4f), yM2 + CR.pcApxL(1f));
        marca_m2.setColor(Color.BLACK);
        marca_m2.setTamano(CR.pcApxL(3.5f));
        objetos[16] = marca_m2;

        marca_m3 = new EtiquetaMovil("m₃", xM3 + CR.pcApxL(4f), yM3 + CR.pcApxL(1f));
        marca_m3.setColor(Color.BLACK);
        marca_m3.setTamano(CR.pcApxL(3.5f));
        objetos[17] = marca_m3;

        /*
         Indicador del eje Y de la Figura 12B: flecha vertical hacia
         abajo con el "0" marcado a la altura de las poleas fijas
         (el origen) y la etiqueta "Eje y" en la punta. Va en el
         hueco entre la polea derecha y m3. Es de referencia fija:
         no se mueve durante la animación, por eso son Marca (no
         EtiquetaMovil) y no se tocan en cambiarEstadosEscenaPizarra().
        */
        //corrido ~15% de la pantalla a la derecha para que no cruce la polea/cuerda de m3
        float xEjeY = xPoleaDerecha + 0.5f * (xM3 - xPoleaDerecha) + CR.pcApxX(15f);
        float largoEjeY = (yPisoArriba - CR.pcApxL(8f)) - yPoleasFijas;

        FlechaRellena flecha_ejeY = new FlechaRellena(xEjeY, yPoleasFijas, largoEjeY);
        flecha_ejeY.rotar(90f); //Canvas.rotate positivo = horario: 90° convierte "hacia la derecha" en "hacia abajo"
        flecha_ejeY.setColor(Color.BLACK);
        flecha_ejeY.setGrosorLinea(CR.pcApxL(0.4f));
        flecha_ejeY.setTamanoCabeza(CR.pcApxL(2f)); //tamaño fijo y pequeño, no proporcional al largo de la flecha
        objetos[18] = flecha_ejeY;

        float tickMedio = CR.pcApxL(1.3f);
        Cuerda tick_origen = new Cuerda(xEjeY - tickMedio, yPoleasFijas, xEjeY + tickMedio, yPoleasFijas);
        tick_origen.setColor(Color.BLACK);
        tick_origen.setGrosorLinea(CR.pcApxL(0.4f));
        objetos[19] = tick_origen;

        Marca marca_0 = new Marca("0", xEjeY + CR.pcApxL(1.5f), yPoleasFijas - CR.pcApxL(1f));
        marca_0.setColor(Color.BLACK);
        marca_0.setTamano(CR.pcApxL(2.4f));
        objetos[20] = marca_0;

        Marca marca_ejeY = new Marca("Eje y", xEjeY - CR.pcApxL(3f), yPoleasFijas + largoEjeY + CR.pcApxL(3.5f));
        marca_ejeY.setColor(Color.BLACK);
        marca_ejeY.setTamano(CR.pcApxL(2f));
        objetos[21] = marca_ejeY;

        //desplegar la escena inicial
        pizarra.setEstadoEscena(objetos);

        escenaLista = true;

    }


    /*
    Cambia el estado (posición y rotación) de los cuerpos rígidos de
    la escena a partir de los desplazamientos ya calculados por
    ModeloFisico y almacenados en AlmacenDatosRAM.
    */
    public void cambiarEstadosEscenaPizarra() {

        //todavía no se conoce el tamaño real de la pizarra (primer
        //layout en curso): no hay objetos que mover aún
        if (!escenaLista) return;

        float dP_px = AlmacenDatosRAM.desplazamiento_P_en_pixeles;
        float d1_px = AlmacenDatosRAM.desplazamiento_m1_en_pixeles;
        float d2_px = AlmacenDatosRAM.desplazamiento_m2_en_pixeles;
        float d3_px = AlmacenDatosRAM.desplazamiento_m3_en_pixeles;

        float yP_actual = AlmacenDatosRAM.yP_en_pixeles;
        float y1_actual = AlmacenDatosRAM.y1_en_pixeles;
        float y2_actual = AlmacenDatosRAM.y2_en_pixeles;
        float y3_actual = AlmacenDatosRAM.y3_en_pixeles;

        float teta_fijas = AlmacenDatosRAM.teta_fijas;
        float teta_P = AlmacenDatosRAM.teta_P;

        //rotar las poleas fijas (giran con la cuerda superior)
        polea_izquierda.mover(teta_fijas);
        polea_derecha.mover(teta_fijas);

        //trasladar y rotar la polea móvil P
        polea_P.mover(0, dP_px, teta_P);

        //trasladar las tres masas
        masa_1.mover(0, d1_px);
        masa_2.mover(0, d2_px);
        masa_3.mover(0, d3_px);

        //mover la cuerda que sostiene a P (cuelga de la polea izquierda)
        cuerda_soporte_P.setPosicionFinal(xP, yP_actual - radioChica);

        //mover la cuerda que sostiene a m3 (cuelga de la polea derecha)
        cuerda_m3.setPosicionFinal(xM3, y3_actual - 0.5f * altoMasa);

        //mover las cuerdas de m1 y m2: ambos extremos se desplazan (P y el bloque)
        cuerda_P_m1.setPosicionInicial(xM1, yP_actual);
        cuerda_P_m1.setPosicionFinal(xM1, y1_actual - 0.5f * altoMasa);

        cuerda_P_m2.setPosicionInicial(xM2, yP_actual);
        cuerda_P_m2.setPosicionFinal(xM2, y2_actual - 0.5f * altoMasa);

        //reubicar las etiquetas: siguen a su bloque/polea en Y (en X no se mueven)
        marca_P.actualizarPosicion(xP - CR.pcApxL(3.5f), yP_actual - radioChica - CR.pcApxL(2f));
        marca_m1.actualizarPosicion(xM1 - CR.pcApxL(9f), y1_actual + CR.pcApxL(1f));
        marca_m2.actualizarPosicion(xM2 + CR.pcApxL(4f), y2_actual + CR.pcApxL(1f));
        marca_m3.actualizarPosicion(xM3 + CR.pcApxL(4f), y3_actual + CR.pcApxL(1f));

    }


    private void actualizarValoresIniciales() {

        AlmacenDatosRAM.m1 = m1;
        AlmacenDatosRAM.m2 = m2;
        AlmacenDatosRAM.m3 = m3;

    }


}

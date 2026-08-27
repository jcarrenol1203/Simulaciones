package com.curso_simulaciones.miquintaapp;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.curso_simulaciones.miquintaapp.componentes.GaugeSimple;

public class ActividadPrincipalMiQuintaApp extends Activity {
    private GaugeSimple tacometro_1, tacometro_2, tacometro_3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /*llamada al método para crear los elementos de la interfaz
        gráfica de usuario (GUI)*/
        crearElementosGui();
        /*para informar cómo se debe adaptar la GUI a la pantalla del
        dispositivo*/
        ViewGroup.LayoutParams parametro_layout_principal = new
                ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        /*pegar al contenedor la GUI: en el argumento se está llamando
        al método crearGui()*/
        this.setContentView(crearGui(), parametro_layout_principal);
    }

    /*crear los objetos de la interfaz gráfica de usuario (GUI)*/
    private void crearElementosGui() {
        // crear objeto GaugeSimple 1 (exactamente como en la Figura 46)
        tacometro_1 = new GaugeSimple(this);
        // cambiar atributos (propiedades)
        // darle color blanco al lienzo antes de pegar
        tacometro_1.setBackgroundColor(Color.WHITE);
        // configurar colores del fondo, de la aguja, de los números, de la escala y de las unidades
        tacometro_1.setColorFondo(Color.BLACK);
        tacometro_1.setColorAguja(Color.RED);
        tacometro_1.setColorEscala(Color.BLUE);
        tacometro_1.setColorNumeros(Color.WHITE);
        tacometro_1.setColorUnidades(Color.YELLOW);
        // asignar las unidades
        tacometro_1.setUnidades("Gauss");
        // asignar rangos
        tacometro_1.setRango(-20, 40);
        // asignar divisiones de escala
        tacometro_1.setDivisionesEscala(6);
        // asignar la medida
        tacometro_1.setMedida(32f);

        // crear objeto GaugeSimple 2 (diferentes colores, unidades y valor)
        tacometro_2 = new GaugeSimple(this);
        // cambiar atributos (propiedades)
        // darle color blanco al lienzo antes de pegar
        tacometro_2.setBackgroundColor(Color.WHITE);
        // configurar colores
        tacometro_2.setColorFondo(Color.rgb(10, 30, 25)); // Fondo verde oscuro
        tacometro_2.setColorAguja(Color.rgb(0, 210, 100)); // Aguja verde brillante
        tacometro_2.setColorEscala(Color.CYAN); // Sector principal cian
        tacometro_2.setColorNumeros(Color.WHITE);
        tacometro_2.setColorUnidades(Color.GREEN);
        // asignar las unidades
        tacometro_2.setUnidades("% HR");
        // asignar rangos
        tacometro_2.setRango(0, 100);
        // asignar divisiones de escala
        tacometro_2.setDivisionesEscala(4);
        // asignar la medida
        tacometro_2.setMedida(68f);

        // crear objeto GaugeSimple 3 (diferentes colores, unidades y valor)
        tacometro_3 = new GaugeSimple(this);
        // cambiar atributos (propiedades)
        // darle color blanco al lienzo antes de pegar
        tacometro_3.setBackgroundColor(Color.WHITE);
        // configurar colores
        tacometro_3.setColorFondo(Color.rgb(35, 25, 5)); // Fondo marrón oscuro
        tacometro_3.setColorAguja(Color.YELLOW); // Aguja amarilla
        tacometro_3.setColorEscala(Color.rgb(255, 140, 0)); // Sector principal naranja
        tacometro_3.setColorNumeros(Color.WHITE);
        tacometro_3.setColorUnidades(Color.YELLOW);
        // asignar las unidades
        tacometro_3.setUnidades("lux");
        // asignar rangos
        tacometro_3.setRango(0, 1000);
        // asignar divisiones de escala
        tacometro_3.setDivisionesEscala(4);
        // asignar la medida
        tacometro_3.setMedida(420f);
    }

    /*organizar la distribución de los objetos de de la GUI usando
    administradores de diseño*/
    private LinearLayout crearGui() {
        // administrador de diseño
        LinearLayout linear_principal = new LinearLayout(this);
        linear_principal.setOrientation(LinearLayout.HORIZONTAL);
        linear_principal.setGravity(Gravity.CENTER_HORIZONTAL);
        linear_principal.setGravity(Gravity.FILL);
        linear_principal.setBackgroundColor(Color.rgb(250, 150, 50));
        linear_principal.setWeightSum(3);

        LinearLayout linear_izquierdo = new LinearLayout(this);
        linear_izquierdo.setOrientation(LinearLayout.VERTICAL);
        linear_izquierdo.setGravity(Gravity.CENTER_HORIZONTAL);
        linear_izquierdo.setGravity(Gravity.FILL);
        linear_izquierdo.setBackgroundColor(Color.RED);
        linear_izquierdo.setWeightSum(1);

        LinearLayout linear_centro = new LinearLayout(this);
        linear_centro.setOrientation(LinearLayout.VERTICAL);
        linear_centro.setGravity(Gravity.CENTER_HORIZONTAL);
        linear_centro.setGravity(Gravity.FILL);
        linear_centro.setBackgroundColor(Color.BLUE);
        linear_centro.setWeightSum(1);

        LinearLayout linear_derecho = new LinearLayout(this);
        linear_derecho.setOrientation(LinearLayout.VERTICAL);
        linear_derecho.setGravity(Gravity.CENTER_HORIZONTAL);
        linear_derecho.setGravity(Gravity.FILL);
        linear_derecho.setBackgroundColor(Color.GREEN);
        linear_derecho.setWeightSum(1);

        // parametro para pegar los gauges
        LinearLayout.LayoutParams parametrosPegadaGauges = new
                LinearLayout.LayoutParams(android.view.ViewGroup.LayoutParams.MATCH_PARENT, 0);
        parametrosPegadaGauges.setMargins(20, 20, 20, 20);
        parametrosPegadaGauges.weight = 1.0f;

        // pegar gauges
        linear_izquierdo.addView(tacometro_1, parametrosPegadaGauges);
        linear_centro.addView(tacometro_2, parametrosPegadaGauges);
        linear_derecho.addView(tacometro_3, parametrosPegadaGauges);

        // parametro para pegar los linear al principal
        LinearLayout.LayoutParams parametrosPegadaLinear = new
                LinearLayout.LayoutParams(0, android.view.ViewGroup.LayoutParams.MATCH_PARENT);
        parametrosPegadaLinear.setMargins(20, 20, 20, 20);
        parametrosPegadaLinear.weight = 1.0f;

        linear_principal.addView(linear_izquierdo, parametrosPegadaLinear);
        linear_principal.addView(linear_centro, parametrosPegadaLinear);
        linear_principal.addView(linear_derecho, parametrosPegadaLinear);

        return linear_principal;
    }
}

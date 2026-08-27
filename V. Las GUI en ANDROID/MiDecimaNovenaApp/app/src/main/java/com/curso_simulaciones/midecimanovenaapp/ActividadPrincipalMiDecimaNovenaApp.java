package com.curso_simulaciones.midecimanovenaapp;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.curso_simulaciones.midecimanovenaapp.actividades_secundarias.ActividadSecundaria_1;
import com.curso_simulaciones.midecimanovenaapp.actividades_secundarias.ActividadSecundaria_2;
import com.curso_simulaciones.midecimanovenaapp.actividades_secundarias.ActividadSecundaria_3;
import com.curso_simulaciones.midecimanovenaapp.datos.AlmacenDatosRAM;

public class ActividadPrincipalMiDecimaNovenaApp extends Activity {

    private Button botonUno, botonDos, botonTres, botonCuatro;
    private ImageView imagenArriba, imagenAbajo;
    private int tamanoLetraResolucionIncluida;

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

        //para administrar los eventos
        eventos();


    } //fin del método onCreate


    private void gestionarResolucion() {

        //independencia de la resolución de la pantalla
        DisplayMetrics displayMetrics = this.getApplicationContext().getResources().getDisplayMetrics();
        int alto = displayMetrics.heightPixels;
        int ancho = displayMetrics.widthPixels;
        int dimensionReferencia;

        //tomar el menor valor entre alto y ancho de pantalla
        if (alto > ancho) {
            dimensionReferencia = ancho;
        } else {
            dimensionReferencia = alto;
        }

        //una estimación de un buen tamaño
        int tamanoLetra = dimensionReferencia / 20;

        //tamano de letra para usar acomodado a la resolución de pantalla
        tamanoLetraResolucionIncluida = (int) (tamanoLetra / displayMetrics.scaledDensity);

        //guardar en el almacen de datos para que otras clases la accedan fácilmente
        AlmacenDatosRAM.tamanoLetraResolucionIncluida = tamanoLetraResolucionIncluida;

    }//fin método gestionarResolucion()


    /*método responsable de la creación de los elementos de la GUI*/
    private void crearElementosGUI() {

        //imagen de la parte superior (70% de la pantalla)
        imagenArriba = new ImageView(this);
        imagenArriba.setImageResource(R.drawable.imagen_principal_arriba);
        imagenArriba.setScaleType(ImageView.ScaleType.CENTER_CROP);

        //imagen de la parte inferior izquierda (80% del 30% inferior)
        imagenAbajo = new ImageView(this);
        imagenAbajo.setImageResource(R.drawable.imagen_principal_abajo);
        imagenAbajo.setScaleType(ImageView.ScaleType.CENTER_CROP);

        botonUno = new Button(this);
        botonUno.setTextSize(TypedValue.COMPLEX_UNIT_SP, tamanoLetraResolucionIncluida);
        botonUno.setText("UNO");
        botonUno.getBackground().setColorFilter(Color.rgb(220, 156, 80), PorterDuff.Mode.MULTIPLY);

        botonDos = new Button(this);
        botonDos.setTextSize(TypedValue.COMPLEX_UNIT_SP, tamanoLetraResolucionIncluida);
        botonDos.setText("DOS");
        botonDos.getBackground().setColorFilter(Color.rgb(220, 156, 80), PorterDuff.Mode.MULTIPLY);

        botonTres = new Button(this);
        botonTres.setTextSize(TypedValue.COMPLEX_UNIT_SP, tamanoLetraResolucionIncluida);
        botonTres.setText("TRES");
        botonTres.getBackground().setColorFilter(Color.rgb(220, 156, 80), PorterDuff.Mode.MULTIPLY);
        botonTres.setEnabled(false);

        botonCuatro = new Button(this);
        botonCuatro.setTextSize(TypedValue.COMPLEX_UNIT_SP, tamanoLetraResolucionIncluida);
        botonCuatro.setText("CUATRO");
        botonCuatro.getBackground().setColorFilter(Color.rgb(220, 156, 80), PorterDuff.Mode.MULTIPLY);
        botonCuatro.setEnabled(false);

    }//fin método crearElementosGUI


    /*método responsable de administrar el diseño de la GUI*/
    private LinearLayout crearGUI() {

        LinearLayout linearPrincipal = new LinearLayout(this);
        //los componentes se agregarán verticalmente
        linearPrincipal.setOrientation(LinearLayout.VERTICAL);
        //para definir los pesos de las filas que se agregaran
        linearPrincipal.setWeightSum(10.0f);

        //fila de arriba: imagen (70%)
        LinearLayout linearArriba = new LinearLayout(this);

        //fila de abajo: imagen + botones (30%)
        LinearLayout linearAbajo = new LinearLayout(this);
        //los componentes se agregarán horizontalmente
        linearAbajo.setOrientation(LinearLayout.HORIZONTAL);
        //para definir los pesos de las columnas que se agregaran
        linearAbajo.setWeightSum(10.0f);

        //pegado de las dos filas a linear_principal
        //peso vertical: 0 como segundo argumento
        LinearLayout.LayoutParams parametros_pegado_filas = new LinearLayout.LayoutParams(android.view.ViewGroup.LayoutParams.MATCH_PARENT, 0);
        parametros_pegado_filas.weight = 7.0f;
        linearPrincipal.addView(linearArriba, parametros_pegado_filas);

        LinearLayout.LayoutParams parametros_pegado_fila_abajo = new LinearLayout.LayoutParams(android.view.ViewGroup.LayoutParams.MATCH_PARENT, 0);
        parametros_pegado_fila_abajo.weight = 3.0f;
        linearPrincipal.addView(linearAbajo, parametros_pegado_fila_abajo);

        //pegar la imagen de arriba a linearArriba (ocupa todo el contenedor)
        linearArriba.addView(imagenArriba, new LinearLayout.LayoutParams(android.view.ViewGroup.LayoutParams.MATCH_PARENT, android.view.ViewGroup.LayoutParams.MATCH_PARENT));

        //columna izquierda de linearAbajo: imagen (80%)
        LinearLayout linearImagenAbajo = new LinearLayout(this);

        //columna derecha de linearAbajo: los 4 botones (20%)
        LinearLayout linearBotones = new LinearLayout(this);
        //los botones se agregarán verticalmente
        linearBotones.setOrientation(LinearLayout.VERTICAL);
        //para definir los pesos de los botones que se agregaran
        linearBotones.setWeightSum(4.0f);

        //pegado de las dos columnas a linear_abajo
        //peso horizontal: 0 como primer argumento
        LinearLayout.LayoutParams parametros_pegado_imagen_abajo = new LinearLayout.LayoutParams(0, android.view.ViewGroup.LayoutParams.MATCH_PARENT);
        parametros_pegado_imagen_abajo.weight = 8.0f;
        linearAbajo.addView(linearImagenAbajo, parametros_pegado_imagen_abajo);

        LinearLayout.LayoutParams parametros_pegado_botones_columna = new LinearLayout.LayoutParams(0, android.view.ViewGroup.LayoutParams.MATCH_PARENT);
        parametros_pegado_botones_columna.weight = 2.0f;
        linearAbajo.addView(linearBotones, parametros_pegado_botones_columna);

        //pegar la imagen de abajo a linearImagenAbajo (ocupa todo el contenedor)
        linearImagenAbajo.addView(imagenAbajo, new LinearLayout.LayoutParams(android.view.ViewGroup.LayoutParams.MATCH_PARENT, android.view.ViewGroup.LayoutParams.MATCH_PARENT));

        //pegado de los 4 botones a linearBotones (cada uno con peso 1.0 del total de 4.0)
        LinearLayout.LayoutParams parametros_pegado_botones = new LinearLayout.LayoutParams(android.view.ViewGroup.LayoutParams.MATCH_PARENT, 0);
        parametros_pegado_botones.weight = 1.0f;
        linearBotones.addView(botonUno, parametros_pegado_botones);
        linearBotones.addView(botonDos, parametros_pegado_botones);
        linearBotones.addView(botonTres, parametros_pegado_botones);
        linearBotones.addView(botonCuatro, parametros_pegado_botones);

        return linearPrincipal;

    }//fin método crearGUI


    /*Administra los eventos de la GUI*/
    private void eventos() {

        //evento del boton con etiqueta UNO
        botonUno.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                lanzarActividadSecundaria_1();
            }
        });

        //evento del boton con etiqueta DOS
        botonDos.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                lanzarActividadSecundaria_2();
            }
        });

        //evento del boton con etiqueta TRES
        botonTres.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                lanzarActividadSecundaria_3();
            }
        });

        //el botón CUATRO no tiene ninguna acción asignada

    }//fin método eventos

    //métodos que lanzan las actividades secundarias
    private void lanzarActividadSecundaria_1() {
        Intent intent = new Intent(this, ActividadSecundaria_1.class);
        startActivity(intent);
    }

    private void lanzarActividadSecundaria_2() {
        Intent intent = new Intent(this, ActividadSecundaria_2.class);
        startActivity(intent);
    }

    private void lanzarActividadSecundaria_3() {
        Intent intent = new Intent(this, ActividadSecundaria_3.class);
        startActivity(intent);
    }


    /* Métodos automáticos*/
    protected void onResume() {
        super.onResume();
        if (AlmacenDatosRAM.habilitar_boton_tres == true) {
            botonTres.setEnabled(true);
        } else {
            botonTres.setEnabled(false);
        }
    }//fin del método onResume

    protected void onDestroy() {
        //Volver los valores de los datos a su estado por defecto
        AlmacenDatosRAM.nombreImagenUno = "Xxxx";
        AlmacenDatosRAM.nombreImagenDos = "Xxxx";
        AlmacenDatosRAM.habilitar_boton_tres = false;
        this.finish();
        super.onDestroy();
    }//fin del método onDestroy

}

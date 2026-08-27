package com.curso_simulaciones.midecimanovenaapp.actividades_secundarias;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.curso_simulaciones.midecimanovenaapp.R;
import com.curso_simulaciones.midecimanovenaapp.datos.AlmacenDatosRAM;

public class ActividadSecundaria_2 extends Activity {

    private int tamanoLetraResolucionIncluida, margenesResolucionIncluida;

    private ImageView imagen;
    private TextView textNombre;
    private EditText editNombreImagen;


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

        tamanoLetraResolucionIncluida = (int) (0.8f * AlmacenDatosRAM.tamanoLetraResolucionIncluida);
        margenesResolucionIncluida = (int) (1.2f * AlmacenDatosRAM.tamanoLetraResolucionIncluida);

    }//fin método gestionarResolucion()


    /*método responsable de la creación de los elementos de la GUI*/
    private void crearElementosGUI() {

        //etiqueta NOMBRE
        textNombre = new TextView(this);
        textNombre.setBackgroundColor(Color.rgb(255, 153, 0));
        textNombre.setGravity(android.view.Gravity.CENTER);
        textNombre.setTextSize(tamanoLetraResolucionIncluida);
        textNombre.setText("NOMBRE");
        textNombre.setTextColor(Color.BLACK);

        //campo para digitar el nombre de la imagen
        editNombreImagen = new EditText(this);
        editNombreImagen.setBackgroundColor(Color.WHITE);
        editNombreImagen.setTextSize(tamanoLetraResolucionIncluida);
        editNombreImagen.setTextColor(Color.BLACK);
        editNombreImagen.setText(AlmacenDatosRAM.nombreImagenDos);

        //imagen que ocupa el 90% inferior de la pantalla
        imagen = new ImageView(this);
        imagen.setImageResource(R.drawable.imagen_secundaria_dos);
        imagen.setScaleType(ImageView.ScaleType.CENTER_CROP);

    }//fin método crearElementosGUI


    /*método responsable de administrar el diseño de la GUI*/
    private LinearLayout crearGUI() {

        LinearLayout linearPrincipal = new LinearLayout(this);
        //los componentes se agregarán verticalmente
        linearPrincipal.setOrientation(LinearLayout.VERTICAL);
        //para definir los pesos de las filas que se agregaran
        linearPrincipal.setWeightSum(10.0f);
        //fondo verde y márgen interno para que se vea como "marco" (igual al mockup)
        linearPrincipal.setBackgroundColor(Color.GREEN);
        linearPrincipal.setPadding(margenesResolucionIncluida, margenesResolucionIncluida, margenesResolucionIncluida, margenesResolucionIncluida);

        //fila de la etiqueta + campo de texto (10%)
        LinearLayout linearNombre = new LinearLayout(this);
        linearNombre.setOrientation(LinearLayout.HORIZONTAL);
        linearNombre.setWeightSum(3.0f);
        linearNombre.setBackgroundColor(Color.YELLOW);

        //fila de la imagen (90%)
        LinearLayout linearImagen = new LinearLayout(this);
        linearImagen.setBackgroundColor(Color.WHITE);

        //pegado de las dos filas a linear_principal
        //deja un espacio (se ve verde) entre las dos filas, igual al mockup
        LinearLayout.LayoutParams parametros_pegado_nombre = new LinearLayout.LayoutParams(android.view.ViewGroup.LayoutParams.MATCH_PARENT, 0);
        parametros_pegado_nombre.weight = 1.0f;
        parametros_pegado_nombre.setMargins(0, 0, 0, margenesResolucionIncluida);
        linearPrincipal.addView(linearNombre, parametros_pegado_nombre);

        LinearLayout.LayoutParams parametros_pegado_imagen = new LinearLayout.LayoutParams(android.view.ViewGroup.LayoutParams.MATCH_PARENT, 0);
        parametros_pegado_imagen.weight = 9.0f;
        linearPrincipal.addView(linearImagen, parametros_pegado_imagen);

        //pegar la etiqueta y el campo de texto a linearNombre
        //con márgenes pequeños para que se vea el amarillo de fondo entre las cajas, igual al mockup
        int margenChico = margenesResolucionIncluida;
        //margen vertical más grande para que el alto de las cajas quede en 3/4 del que tenían
        int margenVertical = (int) (margenChico * 1.8f);
        LinearLayout.LayoutParams parametros_pegado_text_nombre = new LinearLayout.LayoutParams(0, android.view.ViewGroup.LayoutParams.MATCH_PARENT);
        parametros_pegado_text_nombre.weight = 1.0f;
        parametros_pegado_text_nombre.setMargins(margenChico, margenVertical, margenChico, margenVertical);
        LinearLayout.LayoutParams parametros_pegado_edit_nombre = new LinearLayout.LayoutParams(0, android.view.ViewGroup.LayoutParams.MATCH_PARENT);
        parametros_pegado_edit_nombre.weight = 2.0f;
        parametros_pegado_edit_nombre.setMargins(margenChico, margenVertical, margenChico, margenVertical);
        linearNombre.addView(textNombre, parametros_pegado_text_nombre);
        linearNombre.addView(editNombreImagen, parametros_pegado_edit_nombre);

        //pegar la imagen a linearImagen (ocupa todo el contenedor)
        linearImagen.addView(imagen, new LinearLayout.LayoutParams(android.view.ViewGroup.LayoutParams.MATCH_PARENT, android.view.ViewGroup.LayoutParams.MATCH_PARENT));

        return linearPrincipal;

    }//fin método crearGUI


    /*Administra los eventos de la GUI*/
    private void eventos() {


    }//fin método eventos


    /* Este método es automático*/
    protected void onPause() {
        AlmacenDatosRAM.nombreImagenDos = editNombreImagen.getText().toString();
        AlmacenDatosRAM.habilitar_boton_tres = true;
        super.onPause();
    }//fin del método onPause

}//fin Actividad

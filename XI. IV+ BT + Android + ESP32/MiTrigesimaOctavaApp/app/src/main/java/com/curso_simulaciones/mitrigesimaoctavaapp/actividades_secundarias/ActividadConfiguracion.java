package com.curso_simulaciones.mitrigesimaoctavaapp.actividades_secundarias;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.text.method.DigitsKeyListener;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.curso_simulaciones.mitrigesimaoctavaapp.datos.AlmacenDatosRAM;

public class ActividadConfiguracion extends Activity {

    private EditText edit_text_numero_datos;
    private TextView text_numero_datos;
    //variable tamaño de las letras basado en resolución de pantalla
    private int tamanoLetraResolucionIncluida;

    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);


        gestionarResolucion();


        //para crear elementos de la GUI
        crearElementosGUI();

        //para informar cómo se debe pegar el adminitrador de
        //diseño obtenido con el método GUI
        ViewGroup.LayoutParams parametro_layout_principal = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

        //pegar el contenedor con la GUI
        this.setContentView(crearGUI(), parametro_layout_principal);

        //Eventos GUI
        eventosGui();

    } //fin del método onCreate


    private void gestionarResolucion(){

        //tamano de letra para usar acomodado a la resolución de pantalla
        tamanoLetraResolucionIncluida = (int)(0.6* AlmacenDatosRAM.tamanoLetraResolucionIncluida);

    }

    private void eventosGui() {


    }



    /*método responsable de la creación de los elementos de la GUI*/
    private void crearElementosGUI(){

        text_numero_datos = new TextView(this);
        text_numero_datos.setGravity(Gravity.FILL_VERTICAL);
        text_numero_datos.setTextSize(tamanoLetraResolucionIncluida);
        text_numero_datos.setText("  NÚMERO DE DATOS (máximo 1000)");
        text_numero_datos.setTextColor(Color.BLACK);

        edit_text_numero_datos = new EditText(this);
        //Despliega teclado solo con números enteros positivos.
        edit_text_numero_datos.setKeyListener(DigitsKeyListener.getInstance(false, false));
        edit_text_numero_datos.setTextSize(tamanoLetraResolucionIncluida);
        edit_text_numero_datos.setText("" + AlmacenDatosRAM.nDatos);


    }


    /*método responsable de administrar el diseño de la GUI*/
    private LinearLayout crearGUI() {

        //LinearLayoutPrincipal
        LinearLayout linear_principal = new LinearLayout(this);
        linear_principal.setOrientation(LinearLayout.VERTICAL);
        linear_principal.setBackgroundColor(Color.WHITE);
        linear_principal.setWeightSum(10.0f);

        //LinearLayout primera fila
        LinearLayout linearLayoutPrimeraFila = new LinearLayout(this);
        linearLayoutPrimeraFila.setBackgroundColor(Color.YELLOW);
        linearLayoutPrimeraFila.setWeightSum(2f);
        linearLayoutPrimeraFila.setOrientation(LinearLayout.HORIZONTAL);

        //LinearLayout segunda fila
        LinearLayout linearLayoutSegundaFila = new LinearLayout(this);
        linearLayoutSegundaFila.setBackgroundColor(Color.YELLOW);
        linearLayoutSegundaFila.setWeightSum(2f);
        linearLayoutSegundaFila.setOrientation(LinearLayout.HORIZONTAL);


        //LinearLayout tercera fila
        LinearLayout linearLayoutTerceraFila = new LinearLayout(this);
        linearLayoutTerceraFila.setBackgroundColor(Color.BLACK);
        linearLayoutTerceraFila.setOrientation(LinearLayout.VERTICAL);

        //pegar las filas en el principal
        LinearLayout.LayoutParams parametrosPrimerasdosFilas = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,0);
        parametrosPrimerasdosFilas.weight = 0.5f;
        linearLayoutPrimeraFila.setLayoutParams(parametrosPrimerasdosFilas);
        linearLayoutSegundaFila.setLayoutParams(parametrosPrimerasdosFilas);


        LinearLayout.LayoutParams parametrosTerceraFila = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,0);
        parametrosTerceraFila.weight = 9.0f;
        linearLayoutTerceraFila.setLayoutParams(parametrosTerceraFila);

        linear_principal.addView(linearLayoutPrimeraFila);
        linear_principal.addView(linearLayoutSegundaFila);
        linear_principal.addView(linearLayoutTerceraFila);

        //pegar elementos en dos primeras filas
        LinearLayout.LayoutParams parametros_pegado_elementos_dos_primeras_filas_I = new LinearLayout.LayoutParams(0, android.view.ViewGroup.LayoutParams.MATCH_PARENT);
        parametros_pegado_elementos_dos_primeras_filas_I .weight = 1.0f;
        LinearLayout.LayoutParams parametros_pegado_elementos_dos_primeras_filas_D = new LinearLayout.LayoutParams(0, android.view.ViewGroup.LayoutParams.MATCH_PARENT);
        parametros_pegado_elementos_dos_primeras_filas_D .weight = 1.0f;


        //segunda fila
        text_numero_datos.setLayoutParams(parametros_pegado_elementos_dos_primeras_filas_I);
        edit_text_numero_datos.setLayoutParams(parametros_pegado_elementos_dos_primeras_filas_D);
        linearLayoutSegundaFila.addView(text_numero_datos);
        linearLayoutSegundaFila.addView(edit_text_numero_datos);


        return linear_principal;
    }


    /*
      Guardar el estado automáticamente cuando se salga
      de esta pantalla
    */
    protected void onPause() {
        super.onPause();

        String string_numero_datos = edit_text_numero_datos.getText().toString();
        int numero_datos = Integer.parseInt(string_numero_datos);
        AlmacenDatosRAM.nDatos = numero_datos;


    }



}

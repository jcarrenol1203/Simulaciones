package com.curso_simulaciones.micuadragesimaterceraapp.actividades_secundarias;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Build;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.RequiresApi;

import com.curso_simulaciones.micuadragesimaterceraapp.comunicaciones.ClientePubSubMQTT;
import com.curso_simulaciones.micuadragesimaterceraapp.datos.AlmacenDatosRAM;
import com.curso_simulaciones.micuadragesimaterceraapp.gui_auxiliares.DialogoSalir;
import com.curso_simulaciones.micuadragesimaterceraapp.utilidades.TableroColor;

import org.json.JSONException;
import org.json.JSONObject;

public class ActividadComoClientePubMQTT extends Activity implements Runnable  {


    private int tamanoLetraResolucionIncluida;

    //Objetos GUI necesarios
    private TextView textRojo, textVerde, textAzul;
    private TextView textviewAviso;
    private Button botonConectar;
    private SeekBar seekBarRojo, seekBarVerde, seekBarAzul;
    private TableroColor tablero;

    //valores de las variables
    private int rojo, verde, azul;

    private ClientePubSubMQTT cliente;
    private Thread hilo;


    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        gestionarResolucion();

        //para crear elementos de la GUI
        crearElementosGUI();

        /*
        Para informar cómo se debe pegar el administrador de
        diseño LinearLayout obtenido con el método crearGui()
        */
        ViewGroup.LayoutParams parametro_layout_principal = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);


        //pegar el contenedor con la GUI
        this.setContentView(crearGUI(), parametro_layout_principal);

        eventos();

        crearCliente();

        hilo = new Thread(this);


    }//fin del método onCreate

    private void gestionarResolucion() {

        //tamano de letra para usar acomodado a la resolución de pantalla
        tamanoLetraResolucionIncluida = (int) (0.8 * AlmacenDatosRAM.tamanoLetraResolucionIncluida);

    }

    /*método responsable de la creación de los elementos de la GUI*/
    private void crearElementosGUI() {

        botonConectar = new Button(this);
        botonConectar.setTextSize(TypedValue.COMPLEX_UNIT_SP, tamanoLetraResolucionIncluida);
        botonConectar.setText("CONECTAR");
        botonConectar.getBackground().setColorFilter(Color.rgb(183, 216, 199), PorterDuff.Mode.MULTIPLY);

        textRojo = new TextView(this);
        textRojo.setTextSize(TypedValue.COMPLEX_UNIT_SP, tamanoLetraResolucionIncluida);
        textRojo.setGravity(Gravity.CENTER);
        textRojo.setBackgroundColor(Color.BLACK);
        String marca_frecancia_x = "ROJO\n 0 A 255";//con salto de línea
        textRojo.setText(marca_frecancia_x);

        textVerde = new TextView(this);
        textVerde.setTextSize(TypedValue.COMPLEX_UNIT_SP, tamanoLetraResolucionIncluida);
        textVerde.setGravity(Gravity.CENTER);
        textVerde.setBackgroundColor(Color.BLACK);
        String marca_frecuencia_y = "VERDE\n 0 A 255";//con salto de línea
        textVerde.setText(marca_frecuencia_y);

        textAzul = new TextView(this);
        textAzul.setTextSize(TypedValue.COMPLEX_UNIT_SP, tamanoLetraResolucionIncluida);
        textAzul.setGravity(Gravity.CENTER);
        textAzul.setBackgroundColor(Color.BLACK);
        String marca_angulo = "AZUL\n 0 A 255";//con salto de línea
        textAzul.setText(marca_angulo);

        seekBarRojo = new SeekBar(this);
        seekBarRojo.setMax(255);
        seekBarRojo.setScaleY(0.2f);
        seekBarRojo.setProgress(100);
        rojo = seekBarRojo.getProgress();

        seekBarVerde = new SeekBar(this);
        seekBarVerde.setMax(255);
        seekBarVerde.setScaleY(0.2f);
        seekBarVerde.setProgress(200);
        verde = seekBarVerde.getProgress();

        seekBarAzul = new SeekBar(this);
        seekBarAzul.setMax(255);
        seekBarAzul.setScaleY(0.2f);
        seekBarAzul.setProgress(60);
        azul = seekBarAzul.getProgress();

        textviewAviso = new TextView(this);
        textviewAviso.setGravity(Gravity.FILL_VERTICAL);
        textviewAviso.setBackgroundColor(Color.rgb(183,216,199));
        textviewAviso.setTextSize(0.8f*tamanoLetraResolucionIncluida);
        textviewAviso.setText(" ");
        textviewAviso.setTextColor(Color.BLACK);

        tablero = new TableroColor(this);

    }//fin crearElemnetosGUI




    /*método responsable de administrar el diseño de la GUI*/
    private LinearLayout crearGUI() {

        LinearLayout linearPrincipal = new LinearLayout(this);
        linearPrincipal.setOrientation(LinearLayout.VERTICAL);
        linearPrincipal.setBackgroundColor(Color.YELLOW);
        linearPrincipal.setWeightSum(10.0f);

        //el linear principal arriba
        LinearLayout linearPrincipalArriba = new LinearLayout(this);
        linearPrincipalArriba.setOrientation(LinearLayout.HORIZONTAL);
        linearPrincipalArriba.setBackgroundColor(Color.YELLOW);

        //el linear principal abajo
        LinearLayout linearPrincipalAbajo = new LinearLayout(this);
        linearPrincipalAbajo.setOrientation(LinearLayout.HORIZONTAL);
        linearPrincipalAbajo.setBackgroundColor(Color.rgb(183,216,199));
        linearPrincipalAbajo.setWeightSum(1.0f);


        //linear secundario izquierda
        LinearLayout linear_izquierda = new LinearLayout(this);
        linear_izquierda.setBackgroundColor(Color.WHITE);
        linear_izquierda.setWeightSum(1.0f);

        //linear secundario derecha
        LinearLayout linear_derecha = new LinearLayout(this);
        linear_derecha.setBackgroundColor(Color.YELLOW);
        linear_derecha.setOrientation(LinearLayout.VERTICAL);
        linear_derecha.setWeightSum(6.0f);

        //pegar estos secundarios al princial
        LinearLayout.LayoutParams parametros_pegado_izquierdo = new LinearLayout.LayoutParams(0, android.view.ViewGroup.LayoutParams.MATCH_PARENT);
        //ocupará el 80% de linear_principal
        parametros_pegado_izquierdo.weight = 8.0f;
        parametros_pegado_izquierdo.setMargins(5, 5, 5, 5);
        linearPrincipalArriba.addView(linear_izquierda, parametros_pegado_izquierdo);

        //pegar estos secundarios al princial
        LinearLayout.LayoutParams parametros_pegado_derecho = new LinearLayout.LayoutParams(0, android.view.ViewGroup.LayoutParams.MATCH_PARENT);
        //ocupará el 20% de linear_principal
        parametros_pegado_derecho.weight = 2.0f;
        parametros_pegado_derecho.setMargins(5, 5, 5, 5);
        linearPrincipalArriba.addView(linear_derecha, parametros_pegado_derecho);

        //linear secundario derecha
        LinearLayout linear_1 = new LinearLayout(this);
        linear_1.setBackgroundColor(Color.BLUE);
        linear_1.setOrientation(LinearLayout.VERTICAL);
        linear_1.setWeightSum(1.0f);

        //linear secundario derecha
        LinearLayout linear_2 = new LinearLayout(this);
        linear_2.setBackgroundColor(Color.YELLOW);
        linear_2.setOrientation(LinearLayout.VERTICAL);
        linear_2.setWeightSum(1.0f);

        //linear secundario derecha
        LinearLayout linear_3 = new LinearLayout(this);
        linear_3.setBackgroundColor(Color.YELLOW);
        linear_3.setOrientation(LinearLayout.VERTICAL);
        linear_3.setWeightSum(1.0f);

        //linear secundario derecha
        LinearLayout linear_4 = new LinearLayout(this);
        linear_4.setBackgroundColor(Color.YELLOW);
        linear_4.setOrientation(LinearLayout.VERTICAL);
        linear_4.setWeightSum(1.0f);

        //linear secundario derecha
        LinearLayout linear_5 = new LinearLayout(this);
        linear_5.setBackgroundColor(Color.YELLOW);
        linear_5.setOrientation(LinearLayout.VERTICAL);
        linear_5.setWeightSum(1.0f);

        //linear secundario derecha
        LinearLayout linear_6 = new LinearLayout(this);
        linear_6.setBackgroundColor(Color.YELLOW);
        linear_6.setOrientation(LinearLayout.VERTICAL);
        linear_6.setWeightSum(1.0f);

        LinearLayout.LayoutParams parametros_pegado_linears = new LinearLayout.LayoutParams(android.view.ViewGroup.LayoutParams.MATCH_PARENT, 0);
        parametros_pegado_linears.weight = 1.0f;
        parametros_pegado_linears.setMargins(5, 5, 5, 5);

        linear_derecha.addView(linear_1, parametros_pegado_linears);
        linear_derecha.addView(linear_2, parametros_pegado_linears);
        linear_derecha.addView(linear_3, parametros_pegado_linears);
        linear_derecha.addView(linear_4, parametros_pegado_linears);
        linear_derecha.addView(linear_5, parametros_pegado_linears);
        linear_derecha.addView(linear_6, parametros_pegado_linears);

        LinearLayout.LayoutParams parametros_pegado_componentes_text = new LinearLayout.LayoutParams(android.view.ViewGroup.LayoutParams.MATCH_PARENT, 0);
        parametros_pegado_componentes_text.weight = 1.0f;
        parametros_pegado_componentes_text.setMargins(5, 5, 5, 5);

        int indent_y = (int) (0.4f * linear_1.getHeight());
        LinearLayout.LayoutParams parametros_pegado_componentes_seek = new LinearLayout.LayoutParams(android.view.ViewGroup.LayoutParams.MATCH_PARENT, 0);
        parametros_pegado_componentes_seek.weight = 1.0f;
        parametros_pegado_componentes_seek.setMargins(5, (int) indent_y, 5, (int) indent_y);

        linear_1.addView(textRojo, parametros_pegado_componentes_text);
        linear_2.addView(seekBarRojo, parametros_pegado_componentes_seek);

        linear_3.addView(textVerde, parametros_pegado_componentes_text);
        linear_4.addView(seekBarVerde, parametros_pegado_componentes_seek);

        linear_5.addView(textAzul, parametros_pegado_componentes_text);
        linear_6.addView(seekBarAzul, parametros_pegado_componentes_seek);

        //pegar la pizarra al linear izquierdo
        linear_izquierda.setOrientation(LinearLayout.VERTICAL);
        linear_izquierda.addView(tablero, parametros_pegado_componentes_text);//pegado igual al text


        //pegar liear arriba y abajo
        LinearLayout.LayoutParams parametrosPegadoBotonesAbajo = new LinearLayout.LayoutParams(0, android.view.ViewGroup.LayoutParams.MATCH_PARENT);
        parametrosPegadoBotonesAbajo.weight = 1.0f;
        linearPrincipalAbajo.addView(botonConectar, parametrosPegadoBotonesAbajo);

        //linear intermedio que da el estado de la conexxión bluetooth
        LinearLayout linearPrincipalIntermedia = new LinearLayout(this);
        LinearLayout.LayoutParams parametrosPegadoTextView = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT);
        parametrosPegadoTextView.weight = 1.0f;
        linearPrincipalIntermedia.addView(textviewAviso, parametrosPegadoTextView);

        //pegar linear arriba, intermedia y abajo
        LinearLayout.LayoutParams parametrosPegadoArriba = new LinearLayout.LayoutParams(android.view.ViewGroup.LayoutParams.MATCH_PARENT, 0);
        parametrosPegadoArriba.weight = 8.6f;

        LinearLayout.LayoutParams parametrosPegadoIntermedia = new LinearLayout.LayoutParams(android.view.ViewGroup.LayoutParams.MATCH_PARENT, 0);
        parametrosPegadoIntermedia.weight = 0.4f;

        LinearLayout.LayoutParams parametrosPegadoAbajo = new LinearLayout.LayoutParams(android.view.ViewGroup.LayoutParams.MATCH_PARENT, 0);
        parametrosPegadoAbajo.weight = 1.0f;

        linearPrincipal.addView(linearPrincipalArriba, parametrosPegadoArriba);
        linearPrincipal.addView(linearPrincipalIntermedia, parametrosPegadoIntermedia);
        linearPrincipal.addView(linearPrincipalAbajo, parametrosPegadoAbajo);

        return linearPrincipal;

    }//fin gui


    private void eventos() {


        //evento cliente
        botonConectar.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {

                if (botonConectar.getText() == "CONECTAR") {
                    botonConectar.setText("EMPEZAR");
                    //conectar cliente
                    cliente.conectar();
                    AlmacenDatosRAM.estado_conexion_nube=2;
                    actualizarAviso();


                } else {


                    hilo.start();
                    botonConectar.setEnabled(false);

                }

            }
        });



        //eventos seekbar
        seekBarRojo.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int progressChanged = 0;

            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                progressChanged = progress;
                rojo = progressChanged;
                tablero.setColoRojo(rojo);

            }

            public void onStartTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
            }

            public void onStopTrackingTouch(SeekBar seekBar) {

            }

        });


        seekBarVerde.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int progressChanged = 0;

            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                progressChanged = progress;
                verde = progressChanged;
                tablero.setColoVerde(verde);

            }

            public void onStartTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub

            }

            public void onStopTrackingTouch(SeekBar seekBar) {

            }

        });


        seekBarAzul.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int progressChanged = 0;

            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                progressChanged = progress;
                azul = (progressChanged);
                tablero.setColoAzul(azul);

            }

            public void onStartTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
            }

            public void onStopTrackingTouch(SeekBar seekBar) {

            }

        });

    }//fin eventos


    public void crearCliente() {

        cliente = new ClientePubSubMQTT(this);


    }


    protected void onResume() {
        super.onResume();
        AlmacenDatosRAM.estado_conexion_nube=1;
        actualizarAviso();

    }




    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            // Esto es lo que hace mi botón al pulsar ir a atrás
            //alerta();
            DialogoSalir dialogo_salir=new DialogoSalir(this);
            dialogo_salir.mostrarPopMenuCoeficientes();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }


    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void run() {

        while (true) {

            try {
                Thread.sleep(200);

            } catch (InterruptedException e) {
                e.printStackTrace();
            }


            //escribir IoT
            escribir();


        }


    }


    /*
    comunicaciones IoT PUB
     */


    //lo cambie por este
    private void escribir(){

        //escribir (enviar) datos al cliente
        String dato = getStringJSON();
        byte[] dato_en_byte = dato.getBytes();
        if (dato_en_byte != null) {
            cliente.setEnviarMensajes(dato_en_byte);
            AlmacenDatosRAM.estado_conexion_nube=4;
            actualizarAviso();

        } else {

            AlmacenDatosRAM.estado_conexion_nube=3;
            actualizarAviso();

        }
    }


    private String getStringJSON(){

        JSONObject obj = new JSONObject();

        try {

            obj.put("r", new Integer(rojo));
            obj.put("g", new Integer(verde));
            obj.put("b", new Integer(azul));

        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        //convertir a String
        return obj.toString();

    }


    private void actualizarAviso(){

        //text_aviso.setText(" Estado conexión IoT:" + AlmacenDatosRAM.conectado_PubSub)
        if(AlmacenDatosRAM.estado_conexion_nube==1){

            AlmacenDatosRAM.conectado_PubSub= " Hacer clic en CONECTAR para acceder al BROKER...";
            String aviso = AlmacenDatosRAM.conectado_PubSub;
            textviewAviso.setText(aviso);

        }

        if(AlmacenDatosRAM.estado_conexion_nube==2){

            AlmacenDatosRAM.conectado_PubSub= " Hacer clic en EMPEZAR para publicar datos del BROKER...";
            String aviso = AlmacenDatosRAM.conectado_PubSub;
            textviewAviso.setText(aviso);

        }


        if(AlmacenDatosRAM.estado_conexion_nube==3){

            AlmacenDatosRAM.conectado_PubSub= "  No se están enviando datos ...";
            String aviso = AlmacenDatosRAM.conectado_PubSub;
            textviewAviso.setText(aviso);

        }

        if(AlmacenDatosRAM.estado_conexion_nube==4){

            AlmacenDatosRAM.conectado_PubSub= "  Enviando datos ...";
            String aviso = AlmacenDatosRAM.conectado_PubSub;
            textviewAviso.setText(aviso);

        }

    }


}
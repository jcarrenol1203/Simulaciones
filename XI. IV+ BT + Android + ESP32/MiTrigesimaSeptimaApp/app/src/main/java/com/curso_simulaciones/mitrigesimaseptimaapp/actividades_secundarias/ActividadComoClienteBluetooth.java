package com.curso_simulaciones.mitrigesimaseptimaapp.actividades_secundarias;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.os.Handler;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import com.curso_simulaciones.mitrigesimaseptimaapp.comunicaciones.ClienteBluetooth;
import com.curso_simulaciones.mitrigesimaseptimaapp.datos.AlmacenDatosRAM;
import com.curso_simulaciones.mitrigesimaseptimaapp.utilidades.TableroColor;

public class ActividadComoClienteBluetooth extends Activity implements Runnable {

    //Objetos GUI necesarios
    private TextView text_rojo, text_verde, text_azul;
    private TextView text_aviso;
    private Button botonConectar, botonBuscar;
    private int COLOR_1 = Color.rgb(220, 156, 80);
    private SeekBar seek_bar_rojo, seek_bar_verde, seek_bar_azul;
    private TableroColor tablero;

    //valores de las variables
    private int rojo, verde, azul;

    //variable tamaño de las letras basado en resolución de pantalla
    private int tamanoLetraResolucionIncluida;

    //hilo para actualizar tabla
    private final Handler myHandler = new Handler();
    private long periodo_muestreo = 100;//pausas de 100 ms
    private ClienteBluetooth cliente;

    /*
    hilo resposable de estar pendiente
    del intercambio de datos con el servidor
    */
    private Thread hilo;
    private boolean corriendo;

    //para formato de datos a aneviar en JSON
    private JSONObject obj;

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

    }//fin onCreate

    private void gestionarResolucion() {
        //tamano de letra para usar acomodado a la resolución de pantalla
        tamanoLetraResolucionIncluida = (int) (0.8 * AlmacenDatosRAM.tamanoLetraResolucionIncluida);
    }

    /*método responsable de la creación de los elementos de la GUI*/
    private void crearElementosGUI() {

        botonBuscar = new Button(this);
        botonBuscar.setTextSize(TypedValue.COMPLEX_UNIT_SP, tamanoLetraResolucionIncluida);
        botonBuscar.setText("BUSCAR");
        botonBuscar.getBackground().setColorFilter(Color.rgb(183, 216, 199), PorterDuff.Mode.MULTIPLY);

        botonConectar = new Button(this);
        botonConectar.setTextSize(TypedValue.COMPLEX_UNIT_SP, tamanoLetraResolucionIncluida);
        botonConectar.setText("CONECTAR");
        botonConectar.getBackground().setColorFilter(Color.rgb(183, 216, 199), PorterDuff.Mode.MULTIPLY);

        text_rojo = new TextView(this);
        text_rojo.setTextSize(TypedValue.COMPLEX_UNIT_SP, tamanoLetraResolucionIncluida);
        text_rojo.setGravity(Gravity.CENTER);
        text_rojo.setBackgroundColor(Color.BLACK);
        String marca_frecancia_x = "ROJO\n 0 A 255";//con salto de línea
        text_rojo.setText(marca_frecancia_x);

        text_verde = new TextView(this);
        text_verde.setTextSize(TypedValue.COMPLEX_UNIT_SP, tamanoLetraResolucionIncluida);
        text_verde.setGravity(Gravity.CENTER);
        text_verde.setBackgroundColor(Color.BLACK);
        String marca_frecuencia_y = "VERDE\n 0 A 255";//con salto de línea
        text_verde.setText(marca_frecuencia_y);

        text_azul = new TextView(this);
        text_azul.setTextSize(TypedValue.COMPLEX_UNIT_SP, tamanoLetraResolucionIncluida);
        text_azul.setGravity(Gravity.CENTER);
        text_azul.setBackgroundColor(Color.BLACK);
        String marca_angulo = "AZUL\n 0 A 255";//con salto de línea
        text_azul.setText(marca_angulo);

        seek_bar_rojo = new SeekBar(this);
        seek_bar_rojo.setMax(255);
        seek_bar_rojo.setScaleY(0.2f);
        seek_bar_rojo.setProgress(100);
        rojo = seek_bar_rojo.getProgress();

        seek_bar_verde = new SeekBar(this);
        seek_bar_verde.setMax(255);
        seek_bar_verde.setScaleY(0.2f);
        seek_bar_verde.setProgress(200);
        verde = seek_bar_verde.getProgress();

        seek_bar_azul = new SeekBar(this);
        seek_bar_azul.setMax(255);
        seek_bar_azul.setScaleY(0.2f);
        seek_bar_azul.setProgress(60);
        azul = seek_bar_azul.getProgress();

        botonConectar.setEnabled(false);

        text_aviso = new TextView(this);
        text_aviso.setGravity(Gravity.FILL_VERTICAL);
        text_aviso.setBackgroundColor(Color.rgb(183, 216, 199));
        text_aviso.setTextSize(tamanoLetraResolucionIncluida);
        text_aviso.setText(" Buscar dispositivo fuente de sensores para emparejar...");
        text_aviso.setTextColor(Color.BLACK);

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
        linearPrincipalAbajo.setBackgroundColor(Color.rgb(183, 216, 199));
        linearPrincipalAbajo.setWeightSum(2.0f);

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
        parametros_pegado_componentes_seek.setMargins(5, indent_y, 5, indent_y);

        linear_1.addView(text_rojo, parametros_pegado_componentes_text);
        linear_2.addView(seek_bar_rojo, parametros_pegado_componentes_seek);
        linear_3.addView(text_verde, parametros_pegado_componentes_text);
        linear_4.addView(seek_bar_verde, parametros_pegado_componentes_seek);
        linear_5.addView(text_azul, parametros_pegado_componentes_text);
        linear_6.addView(seek_bar_azul, parametros_pegado_componentes_seek);

        //pegar la pizarra al linear izquierdo
        linear_izquierda.setOrientation(LinearLayout.VERTICAL);
        linear_izquierda.addView(tablero, parametros_pegado_componentes_text);//pegado igual al text

        //pegar liear arriba y abajo
        LinearLayout.LayoutParams parametrosPegadoBotonesAbajo = new LinearLayout.LayoutParams(0, android.view.ViewGroup.LayoutParams.MATCH_PARENT);
        parametrosPegadoBotonesAbajo.weight = 1.0f;
        linearPrincipalAbajo.addView(botonBuscar, parametrosPegadoBotonesAbajo);
        linearPrincipalAbajo.addView(botonConectar, parametrosPegadoBotonesAbajo);

        //linear intermedio que da el estado de la conexxión bluetooth
        LinearLayout linearPrincipalIntermedia = new LinearLayout(this);
        LinearLayout.LayoutParams parametrosPegadoTextView = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT);
        parametrosPegadoTextView.weight = 1.0f;
        linearPrincipalIntermedia.addView(text_aviso, parametrosPegadoTextView);

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

        //eventos botones
        botonBuscar.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                lanzarBuscandoDispositivos();
                AlmacenDatosRAM.estado_conexion_bluetooth = 1;
                botonConectar.setEnabled(true);
                botonBuscar.setEnabled(false);
            }
        });

        botonConectar.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (botonConectar.getText() == "CONECTAR") {
                    botonConectar.setText("EMPEZAR");
                    botonBuscar.setEnabled(false);
                    crearCliente();
                    AlmacenDatosRAM.estado_conexion_bluetooth = 2;
                    hacerTrabajoDuro();
                } else {
                    botonConectar.setEnabled(false);
                    cliente.conectarSocketCliente(ActividadComoClienteBluetooth.this);
                    empezarHilo();
                }
            }
        });

        //eventos seekbar
        seek_bar_rojo.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
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

        seek_bar_verde.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
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

        seek_bar_azul.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
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

    private void lanzarBuscandoDispositivos() {
        Intent intent = new Intent(this, ActividadEscaneoDispositivos.class);
        startActivity(intent);
    }

    public void empezarHilo() {
        hilo = new Thread(this);
        hilo.start();
    }

    private void crearCliente() {
        //dirección del dispositivo elegido para emparejar
        String direccion = AlmacenDatosRAM.direccion;
        cliente = new ClienteBluetooth();
        /*
         el cliente instancia un dispositivo Bluetooth
         (BluetoohDevice) a partir de la dirección
         de este.Luego abre un socket de tipo  BuletoothSocket
         a partir del dispositivo enlazado.
         */
        cliente.abrirSocketCliente(direccion);
        empezarComunicacionConServidor();
    }

    private void empezarComunicacionConServidor() {
        /*
        Aceptada la conexión abre los flujos de entrada
        y salida de este socket por donde fluirán (tubería)
        los datos desde y hacia el servidor
        */
        cliente.abrirFlujoEntrada();
        cliente.abrirFlujoSalida();
    }

    private void terminarComunicacionConServidor() {
        cliente.cerrarFlujoEntrada();
        cliente.cerrarFlujoSalida();
        cliente.cerrarSocketCliente();
    }

    @Override
    public void run() {
        corriendo = true;
        while (corriendo) {
            try {
                Thread.sleep(periodo_muestreo);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            escribir();
            hacerTrabajoDuro();
        }
    }

    protected void onResume() {
        super.onResume();
        if (AlmacenDatosRAM.estado_conexion_bluetooth == 1) {
            if (AlmacenDatosRAM.direccion == "NO CONECTADO") {
                AlmacenDatosRAM.conexion_bluetooth = "  Buscar dispositivo fuente de sensores para emparejar...";
            } else {
                AlmacenDatosRAM.conexion_bluetooth = "  Emparejado con " + AlmacenDatosRAM.direccion;
            }
            hacerTrabajoDuro();
        }
    }

    protected void onPause() {
        super.onPause();
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            finish();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    //lo cambie por este
    private void escribir() {
        //escribir (enviar) datos al cliente
        String dato = getStringJSON();
        byte[] dato_en_byte = dato.getBytes();
        if (dato_en_byte != null) {
            cliente.escribirBytes(dato_en_byte);
        }
    }

    private String getStringJSON() {
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

    protected void onDestroy() {
        super.onDestroy();
        if (hilo != null) {
            detener();
            hilo = null;
        }
        if (cliente != null) {
            terminarComunicacionConServidor();
        }
    }

    public void detener() {
        corriendo = false;
        hilo = null;
    }

    /*
    Para el asunto de la table es mejor usar un
    hilo manejador para que no se reviente la aplicación
    */
    public void hacerTrabajoDuro() {
        //.... realizar el trabajo duro
        //Actualiza la UI usando el handler y el runnable
        myHandler.post(updateRunnable);
    }

    final Runnable updateRunnable = new Runnable() {
        public void run() {
            actualizarAviso();
        }
    };

    private void actualizarAviso() {
        if (AlmacenDatosRAM.estado_conexion_bluetooth == 1) {
            String aviso = AlmacenDatosRAM.conexion_bluetooth;
            text_aviso.setText(aviso);
        }
        if (AlmacenDatosRAM.estado_conexion_bluetooth == 2) {
            String aviso = AlmacenDatosRAM.conexion_bluetooth;
            text_aviso.setText(aviso);
        }
        if (AlmacenDatosRAM.estado_conexion_bluetooth == 3) {
            String aviso = AlmacenDatosRAM.conexion_bluetooth;
            text_aviso.setText(aviso);
        }
    }

}

package com.curso_simulaciones.mitrigesimanovenaapp.actividades_secundarias;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.curso_simulaciones.mitrigesimanovenaapp.comunicaciones.ClienteBluetooth;
import com.curso_simulaciones.mitrigesimanovenaapp.datos.AlmacenDatosRAM;
import com.curso_simulaciones.mitrigesimanovenaapp.utilidades.Distanciometro;
import com.curso_simulaciones.mitrigesimanovenaapp.utilidades.Graficador;
import com.curso_simulaciones.mitrigesimanovenaapp.utilidades.TablaSimple;
import com.github.mikephil.charting.data.Entry;

import org.json.JSONException;
import org.json.JSONObject;

public class ActividadComoClienteBluetooth extends Activity implements Runnable {

    // Objetos GUI necesarios
    private LinearLayout linear_layout_segunda_fila;
    private Distanciometro distanciometro;
    private TablaSimple tabla;
    private Graficador graficador;
    private TextView text_aviso;
    private Button botonConectar, botonBuscar, boton_tabla_grafica;

    // Variable tamaño de las letras basado en resolución de pantalla
    private int tamanoLetraResolucionIncluida;

    // Handler para actualizar la interfaz gráfica en el hilo principal
    private final Handler myHandler = new Handler(Looper.getMainLooper());

    int n = -1;
    int tiempo_base = 0;
    int tiempo_anterior;
    int tiempo_real;
    int t_r_base;

    private ClienteBluetooth cliente;
    private Thread hilo;
    private boolean corriendo;
    private float medida;
    private int numero_datos = 0;
    private JSONObject obj;

    // Color RGB elegido según el rango de distancia, para enviarlo de vuelta al ESP32
    private int rojo, verde, azul;
    private int contadorGrafica = 0;

    private LinearLayout.LayoutParams parametros_tabla_grafica;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        gestionarResolucion();
        crearElementosGUI();

        ViewGroup.LayoutParams parametro_layout_principal = new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        );

        this.setContentView(crearGUI(), parametro_layout_principal);
        eventos();
    }

    private void gestionarResolucion() {
        tamanoLetraResolucionIncluida = (int)(0.8 * AlmacenDatosRAM.tamanoLetraResolucionIncluida);
    }

    private void crearElementosGUI() {
        // Distanciómetro / Tacómetro
        distanciometro = new Distanciometro(this);
        distanciometro.setRango(0, 100);
        distanciometro.setUnidades("cm");

        // Tabla
        tabla = new TablaSimple(this);
        tabla.setEtiquetaColumnas("Tiempo (s)", AlmacenDatosRAM.unidades);

        // Graficador
        graficador = new Graficador(this);
        graficador.setTituloEjeX("Tiempo (s)");
        graficador.setTituloEjeY(AlmacenDatosRAM.unidades);
        graficador.setGrosorLinea(2f);
        graficador.setColorLinea(Color.RED);
        graficador.setColorValores(Color.YELLOW);
        graficador.setColorMarcadores(Color.GREEN);
        graficador.setColorFondo(Color.BLACK);
        graficador.setColorTextoEjes(Color.WHITE);

        // Botones
        botonBuscar = new Button(this);
        botonBuscar.setTextSize(TypedValue.COMPLEX_UNIT_SP, tamanoLetraResolucionIncluida);
        botonBuscar.setText("BUSCAR");
        botonBuscar.getBackground().setColorFilter(Color.rgb(183, 216, 199), PorterDuff.Mode.MULTIPLY);

        botonConectar = new Button(this);
        botonConectar.setTextSize(TypedValue.COMPLEX_UNIT_SP, tamanoLetraResolucionIncluida);
        botonConectar.setText("CONECTAR");
        botonConectar.getBackground().setColorFilter(Color.rgb(183, 216, 199), PorterDuff.Mode.MULTIPLY);
        botonConectar.setEnabled(false);

        boton_tabla_grafica = new Button(this);
        boton_tabla_grafica.setTextSize(TypedValue.COMPLEX_UNIT_SP, tamanoLetraResolucionIncluida);
        boton_tabla_grafica.setText("GRAFICA");
        boton_tabla_grafica.getBackground().setColorFilter(Color.rgb(183, 216, 199), PorterDuff.Mode.MULTIPLY);
        boton_tabla_grafica.setEnabled(false);

        text_aviso = new TextView(this);
        text_aviso.setGravity(Gravity.FILL_VERTICAL);
        text_aviso.setBackgroundColor(Color.rgb(183, 216, 199));
        text_aviso.setTextSize(0.8f * tamanoLetraResolucionIncluida);
        text_aviso.setText(" Buscar dispositivo fuente de sensores para emparejar...");
        text_aviso.setTextColor(Color.BLACK);
    }

    private LinearLayout crearGUI() {
        LinearLayout linear_layout_principal = new LinearLayout(this);
        linear_layout_principal.setOrientation(LinearLayout.VERTICAL);
        linear_layout_principal.setBackgroundColor(Color.rgb(183, 216, 199));
        linear_layout_principal.setWeightSum(10.0f);

        LinearLayout linear_layout_primera_fila = new LinearLayout(this);
        linear_layout_primera_fila.setOrientation(LinearLayout.VERTICAL);
        linear_layout_primera_fila.setGravity(Gravity.FILL);
        linear_layout_primera_fila.setBackgroundColor(Color.rgb(245, 245, 245));

        linear_layout_segunda_fila = new LinearLayout(this);
        linear_layout_segunda_fila.setOrientation(LinearLayout.VERTICAL);
        linear_layout_segunda_fila.setGravity(Gravity.FILL);
        linear_layout_segunda_fila.setBackgroundColor(Color.rgb(245, 245, 245));
        linear_layout_segunda_fila.setWeightSum(1.0f);

        LinearLayout linear_layout_tercera_fila = new LinearLayout(this);
        linear_layout_tercera_fila.setBackgroundColor(Color.RED);

        LinearLayout linear_layout_cuarta_fila = new LinearLayout(this);
        linear_layout_cuarta_fila.setOrientation(LinearLayout.HORIZONTAL);
        linear_layout_cuarta_fila.setBackgroundColor(Color.rgb(183, 216, 199));
        linear_layout_cuarta_fila.setWeightSum(3.0f);

        // Parámetros de pegado
        LinearLayout.LayoutParams parametros_primera_fila = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0);
        parametros_primera_fila.weight = 4.25f;
        parametros_primera_fila.setMargins(20, 20, 20, 20);
        linear_layout_primera_fila.setLayoutParams(parametros_primera_fila);
        linear_layout_principal.addView(linear_layout_primera_fila);

        LinearLayout.LayoutParams parametros_segunda_fila = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0);
        parametros_segunda_fila.weight = 4.25f;
        parametros_segunda_fila.setMargins(20, 20, 20, 20);
        linear_layout_segunda_fila.setLayoutParams(parametros_segunda_fila);
        linear_layout_principal.addView(linear_layout_segunda_fila);

        LinearLayout.LayoutParams parametros_tercera_fila = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0);
        parametros_tercera_fila.weight = 0.5f;
        parametros_tercera_fila.setMargins(20, 0, 20, 0);
        linear_layout_tercera_fila.setLayoutParams(parametros_tercera_fila);
        linear_layout_principal.addView(linear_layout_tercera_fila);

        LinearLayout.LayoutParams parametros_cuarta_fila = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0);
        parametros_cuarta_fila.weight = 1.0f;
        parametros_cuarta_fila.setMargins(20, 20, 20, 20);
        linear_layout_cuarta_fila.setLayoutParams(parametros_cuarta_fila);
        linear_layout_principal.addView(linear_layout_cuarta_fila);

        linear_layout_primera_fila.addView(distanciometro);

        parametros_tabla_grafica = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0);
        parametros_tabla_grafica.weight = 1.0f;
        linear_layout_segunda_fila.addView(tabla, parametros_tabla_grafica);

        LinearLayout.LayoutParams parametrosPegadoTextView = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT);
        parametrosPegadoTextView.weight = 1.0f;
        linear_layout_tercera_fila.addView(text_aviso, parametrosPegadoTextView);

        LinearLayout.LayoutParams parametros_botones_cuarta_fila = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT);
        parametros_botones_cuarta_fila.weight = 1.0f;
        botonBuscar.setLayoutParams(parametros_botones_cuarta_fila);
        botonConectar.setLayoutParams(parametros_botones_cuarta_fila);
        boton_tabla_grafica.setLayoutParams(parametros_botones_cuarta_fila);

        linear_layout_cuarta_fila.addView(botonBuscar);
        linear_layout_cuarta_fila.addView(botonConectar);
        linear_layout_cuarta_fila.addView(boton_tabla_grafica);

        return linear_layout_principal;
    }

    private void eventos() {
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
                if (botonConectar.getText().toString().equals("CONECTAR")) {
                    botonConectar.setText("EMPEZAR");
                    botonBuscar.setEnabled(false);
                    crearCliente();
                    // Enviamos el contexto de la actividad
                    cliente.conectarSocketCliente(ActividadComoClienteBluetooth.this);
                    AlmacenDatosRAM.estado_conexion_bluetooth = 2;
                    hacerTrabajoDuro_2();
                } else {
                    borrarDatos();
                    botonConectar.setEnabled(false);
                    boton_tabla_grafica.setEnabled(true);
                    empezarHilo();
                }
            }
        });

        boton_tabla_grafica.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (boton_tabla_grafica.getText().toString().equals("TABLA")) {
                    boton_tabla_grafica.setText("GRAFICA");
                    linear_layout_segunda_fila.removeView(graficador);
                    linear_layout_segunda_fila.addView(tabla, parametros_tabla_grafica);
                } else {
                    boton_tabla_grafica.setText("TABLA");
                    linear_layout_segunda_fila.removeView(tabla);
                    linear_layout_segunda_fila.addView(graficador, parametros_tabla_grafica);
                }
            }
        });
    }

    private void lanzarBuscandoDispositivos() {
        Intent intent = new Intent(this, ActividadEscaneoDispositivos.class);
        startActivity(intent);
    }

    public void empezarHilo() {
        hilo = new Thread(this);
        hilo.start();
    }

    private void crearCliente() {
        String direccion = AlmacenDatosRAM.direccion;
        cliente = new ClienteBluetooth();
        cliente.abrirSocketCliente(direccion);
        empezarComunicacionConServidor();
    }

    private void empezarComunicacionConServidor() {
        cliente.abrirFlujoEntrada();
        cliente.abrirFlujoSalida();
    }

    private void terminarComunicacionConServidor() {
        cliente.cerrarFlujoEntrada();
        cliente.cerrarFlujoSalida();
        cliente.cerrarSocketCliente();
    }

    private void borrarDatos() {
        AlmacenDatosRAM.datos.clear();
        contadorGrafica = 0;
        n = -1;
        numero_datos = 0;
        tabla.borrar();
    }

    @Override
    public void run() {
        corriendo = true;
        while (corriendo) {
            try {
                Thread.sleep(AlmacenDatosRAM.periodo_muestreo);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            leer();
        }
    }

    private void leer() {
        String nuevo_dato_string = cliente.leerString();

        if (nuevo_dato_string != null && !nuevo_dato_string.isEmpty()) {
            convertirStrigJson(nuevo_dato_string);

            // Enviamos el color de vuelta al ESP32 inmediatamente después de leer la distancia
            escribirColorHaciaESP32();

            myHandler.post(new Runnable() {
                @Override
                public void run() {
                    actualizarTacometroTabla();
                    distanciometro.setMedida(medida);
                }
            });

            hacerTrabajoDuro_1();

            AlmacenDatosRAM.estado_conexion_bluetooth = 3;
            AlmacenDatosRAM.conexion_bluetooth = "  Recibiendo datos ...";
            hacerTrabajoDuro_2();
        }
    }

    public void convertirStrigJson(String datoString) {
        try {
            // Ponemos TODO dentro del bloque seguro
            obj = new JSONObject(datoString);

            AlmacenDatosRAM.unidades = obj.getString("unidad");
            AlmacenDatosRAM.tiempo = "" + obj.getInt("tiempo");
            AlmacenDatosRAM.periodo_muestreo = (int)(0.4 * obj.getInt("periodo"));
            medida = (float) obj.getDouble("valor");

        } catch (Exception e) {
            // Si el JSON llega incompleto o mutilado, evitamos que la app colapse.
            // Simplemente, ignoramos este paquete defectuoso.
            e.printStackTrace();
        }
    }

    // Empaqueta el color elegido según la distancia y lo escribe hacia el ESP32
    private void escribirColorHaciaESP32() {
        elegirColor();

        JSONObject jsonColor = new JSONObject();
        try {
            jsonColor.put("r", rojo);
            jsonColor.put("g", verde);
            jsonColor.put("b", azul);

            if (cliente != null) {
                cliente.escribirString(jsonColor.toString());
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    // Define el color RGB según el rango de distancia medido
    private void elegirColor() {
        if (medida == 0.0f) {
            // SIN ECO (el HC-SR04 no detectó nada dentro del timeout) -> AZUL
            rojo = 0;
            verde = 0;
            azul = 255;
        } else if (medida <= 20.0f) {
            // CERCA -> ROJO
            rojo = 255;
            verde = 0;
            azul = 0;
        } else if (medida > 20.0f && medida <= 100.0f) {
            // MEDIO -> VERDE
            rojo = 0;
            verde = 255;
            azul = 0;
        } else {
            // LEJOS -> AZUL
            rojo = 0;
            verde = 0;
            azul = 255;
        }
    }

    private void actualizarTacometroTabla() {
        distanciometro.setUnidades(AlmacenDatosRAM.unidades);
        distanciometro.cambiarEscala(medida);
        tabla.setEtiquetaColumnas("Tiempo en s", "Medida en " + AlmacenDatosRAM.unidades);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (AlmacenDatosRAM.estado_conexion_bluetooth == 1) {
            if (AlmacenDatosRAM.direccion.equals("NO CONECTADO")) {
                AlmacenDatosRAM.conexion_bluetooth = "  Buscar dispositivo fuente de sensores para emparejar...";
            } else {
                AlmacenDatosRAM.conexion_bluetooth = "  Emparejado con " + AlmacenDatosRAM.direccion;
            }
            hacerTrabajoDuro_2();
        }
    }

    @Override
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

    private void hacerTrabajoDuro_1() {
        myHandler.post(updateRunnable_1);
    }

    final Runnable updateRunnable_1 = new Runnable() {
        public void run() {
            n = n + 1;
            if (n == 0) {
                try {
                    tiempo_base = Integer.parseInt(AlmacenDatosRAM.tiempo);
                } catch (NumberFormatException e) {
                    tiempo_base = 0;
                }
            }

            try {
                tiempo_real = Integer.parseInt(AlmacenDatosRAM.tiempo) - tiempo_base;
            } catch (NumberFormatException e) {
                tiempo_real = 0;
            }

            if (tiempo_real != tiempo_anterior && numero_datos < AlmacenDatosRAM.nDatos) {
                if (numero_datos == 0) {
                    t_r_base = tiempo_real;
                }

                tiempo_real = tiempo_real - t_r_base;
                numero_datos = numero_datos + 1;
                float t_r = (float) Math.round(tiempo_real * 0.001 * 100f) / 100f;

                tabla.enviarDatos(t_r, medida);

                graficador.setTituloEjeY(AlmacenDatosRAM.unidades);

                // Protección de memoria: no dejar crecer la gráfica indefinidamente
                contadorGrafica++;
                if (contadorGrafica > AlmacenDatosRAM.nDatos) {
                    if (!AlmacenDatosRAM.datos.isEmpty()) {
                        AlmacenDatosRAM.datos.remove(0);
                    }
                }
                AlmacenDatosRAM.datos.add(new Entry(t_r, medida));
                graficador.setDatos(AlmacenDatosRAM.datos);

                tiempo_anterior = tiempo_real;
            }

            if (numero_datos + 1 > AlmacenDatosRAM.nDatos) {
                botonConectar.setEnabled(true);
            }
        }
    };

    public void hacerTrabajoDuro_2() {
        myHandler.post(updateRunnable_2);
    }

    final Runnable updateRunnable_2 = new Runnable() {
        public void run() {
            actualizarAviso();
        }
    };

    private void actualizarAviso() {
        if (AlmacenDatosRAM.estado_conexion_bluetooth >= 1 && AlmacenDatosRAM.estado_conexion_bluetooth <= 3) {
            text_aviso.setText(AlmacenDatosRAM.conexion_bluetooth);
        }
    }
}

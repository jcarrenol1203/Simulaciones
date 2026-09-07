package com.curso_simulaciones.mitrigesimacuartaapp.utilidades;

import android.content.Context;
import android.graphics.Color;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.curso_simulaciones.mitrigesimacuartaapp.datos.AlmacenDatosRAM;

public class TablaSimple extends LinearLayout {

    private ScrollView panelScroll;
    private TableLayout table;
    private int tamanoLetraResolucionIncluida;
    private int dimensionReferencia;
    private Context context;
    private int contador = 0;
    private String etiquetaTiempo="tiempo";
    private String etiquetaAx="ax";
    private String etiquetaAy="ay";
    private String etiquetaAz="az";
    private String etiquetaA="a";
    private float tiempo, ax, ay, az, a;
    //seis columnas: # de dato, tiempo, ax, ay, az, a
    private final int[] colorColumnas = {
            Color.YELLOW,
            Color.CYAN,
            Color.rgb(255, 150, 150),
            Color.rgb(150, 255, 150),
            Color.rgb(150, 150, 255),
            Color.RED
    };

    /**
     * Cosntructor de TablaSimple
     * @param context
     */
    public TablaSimple(Context context) {
        super(context);
        this.context=context;
        gestionarResolucion();
        this.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        gui();
    }

    private void gestionarResolucion(){
        /*
        El alto en la actividad principal (PORTRAI)
        corresponde al ancho aquí (LANSCAPE
        */
        dimensionReferencia = (int)(0.4f* AlmacenDatosRAM.alto);
        //con seis columnas en vez de tres se reduce la letra para que quepan
        tamanoLetraResolucionIncluida = (int)(0.42* AlmacenDatosRAM.tamanoLetraResolucionIncluida);
    }//fin método gestionarResolucion()

    private void gui(){
        panelScroll = new ScrollView(context);
        table=new TableLayout(context);
        LinearLayout linearLayoutPrincipal = new LinearLayout(context);
        linearLayoutPrincipal.setOrientation(LinearLayout.VERTICAL);
        linearLayoutPrincipal.setBackgroundColor(Color.BLACK);
        LinearLayout.LayoutParams parametroPegado = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT);
        panelScroll.addView(table);
        linearLayoutPrincipal.addView(panelScroll);
        this.addView(linearLayoutPrincipal, parametroPegado);
    }

    /**
     * Modifica las etiquetas de las columnas (la de "# de dato" queda fija)
     * @param etiquetaTiempo
     * @param etiquetaAx
     * @param etiquetaAy
     * @param etiquetaAz
     * @param etiquetaA
     */
    public void setEtiquetaColumnas(String etiquetaTiempo, String etiquetaAx, String etiquetaAy, String etiquetaAz, String etiquetaA){
        this.etiquetaTiempo=etiquetaTiempo;
        this.etiquetaAx=etiquetaAx;
        this.etiquetaAy=etiquetaAy;
        this.etiquetaAz=etiquetaAz;
        this.etiquetaA=etiquetaA;
    }

    /**
     * Envía los datos a la tabla
     * @param tiempo
     * @param ax
     * @param ay
     * @param az
     * @param a
     */
    public void enviarDatos(float tiempo, float ax, float ay, float az, float a){
        this.tiempo=tiempo;
        this.ax=ax;
        this.ay=ay;
        this.az=az;
        this.a=a;
        contador=contador+1;
        incrementarFila();
    }

    /**
     * Borra los datos enviados a la tabla (el encabezado se vuelve a mostrar)
     */
    public void borrar(){
        removerFilas();
    }

    /**
     * Pinta la fila de encabezados con las etiquetas de columna.
     * Es independiente de enviarDatos(): antes la primera llamada a
     * enviarDatos() hacía las veces de encabezado y esa muestra real
     * nunca se veía en pantalla (aunque sí quedaba escrita en el .txt).
     */
    public void mostrarEncabezado(){
        agregarFila("# de DATO", etiquetaTiempo, etiquetaAx, etiquetaAy, etiquetaAz, etiquetaA);
    }

    private TextView crearCeldaTexto(String texto, int color, int anchoCelda){
        TextView celda = new TextView(context);
        celda.setTextSize(TypedValue.COMPLEX_UNIT_SP, tamanoLetraResolucionIncluida);
        TableRow.LayoutParams layoutTexto = new TableRow.LayoutParams(anchoCelda, TableRow.LayoutParams.WRAP_CONTENT);
        celda.setLayoutParams(layoutTexto);
        celda.setText(texto);
        celda.setTextColor(color);
        celda.setGravity(Gravity.CENTER_HORIZONTAL);
        //con seis columnas angostas, evitar que un número negativo envuelva a dos líneas
        celda.setSingleLine(true);
        celda.setEllipsize(TextUtils.TruncateAt.END);
        return celda;
    }

    private void incrementarFila() {
        //toda llamada a enviarDatos() es una fila de datos real (ninguna se reserva para encabezado)
        agregarFila("DATO " + contador, "" + tiempo, "" + ax, "" + ay, "" + az, "" + a);
    }

    private void agregarFila(String textoNumeroDato, String textoTiempo, String textoAx, String textoAy, String textoAz, String textoA) {
        //crear nueva TableRow
        TableRow fila = new TableRow(context);
        int anchoCelda = (int)((dimensionReferencia)/6.0);

        fila.setGravity(Gravity.CENTER_HORIZONTAL);
        fila.addView(crearCeldaTexto(textoNumeroDato, colorColumnas[0], anchoCelda));
        fila.addView(crearCeldaTexto(textoTiempo, colorColumnas[1], anchoCelda));
        fila.addView(crearCeldaTexto(textoAx, colorColumnas[2], anchoCelda));
        fila.addView(crearCeldaTexto(textoAy, colorColumnas[3], anchoCelda));
        fila.addView(crearCeldaTexto(textoAz, colorColumnas[4], anchoCelda));
        fila.addView(crearCeldaTexto(textoA, colorColumnas[5], anchoCelda));

        //Adicionar TabRow a la Tabla
        table.addView( fila, new TableLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
    }

    private void removerFilas() {
        table.removeAllViews();
        contador=0;
        mostrarEncabezado();
    }

}

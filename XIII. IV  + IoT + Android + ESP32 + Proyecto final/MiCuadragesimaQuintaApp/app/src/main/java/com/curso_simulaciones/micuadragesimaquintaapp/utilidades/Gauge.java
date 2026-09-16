package com.curso_simulaciones.micuadragesimaquintaapp.utilidades;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.View;

public class Gauge extends View {

    private float largo;
    private float minimo = 0;
    private float maximo = 100f;
    private float medida = 0.0f;//tomar como medida inicial
    private String unidades = "UNIDADES";

    //color de los sectores
    private int colorPrimerTercio = Color.rgb(200, 200, 0);
    private int colorSegundoTercio = Color.rgb(0, 180, 0);
    private int colorTercerTercio = Color.RED;

    //color del marco
    private int colorFondoTacometro = Color.rgb(240,240,240);
    private int colorBordeTacometro= Color.BLACK;

    //color franja dinámica
    private int colorFranjaDinamica = Color.RED;


    private int angPrimertercio = 100;
    private int angSegundoTercio = 100;
    private int angTercerTercio = 40;

    private int colorLineas = Color.BLACK;
    private int colorNumeros =Color.BLACK;

    private int colorNumerosDesplieggue = Color.WHITE;
    private int colorFondoCajaMedida = Color.BLACK;

    private int numeroDivisiones= 25;
    private int separacionDivisionesGrandes = 5;

    //posición vertical (relativa a "largo") del número desplegado; ajustable
    //por instancia para que un gauge grande con otros gauges pegados debajo
    //pueda subir el número y no se crucen
    //-0.075f = punto medio entre las unidades (a -0.15f*largo) y el centro
    //de la aguja (0), para que el número quede justo entre ambos
    private float posicionVerticalMedida = -0.075f;

    //marca "IoT.PhysicsSensor": si se muestra, y con qué color (antes no
    //tenía color propio y heredaba el último usado, por eso se veía casi
    //invisible al cambiar el color del número desplegado a blanco)
    private boolean mostrarMarcaEmpresa = true;
    private int colorMarcaEmpresa = Color.BLACK;

    //ángulo de inicio y barrido total (en el sistema de canvas.rotate, donde
    //0° = arriba) de los ticks/números/aguja. Por defecto 240°/240° (igual
    //que siempre), pero un gauge con otras vistas pegadas debajo puede
    //reducir el barrido para que la aguja y los números nunca lleguen a esa
    //zona baja, sin importar el valor medido.
    private float anguloInicioEscala = 240f;
    private float sweepEscala = 240f;


    /**
     * Constructor de Gauge
     */
    public Gauge(Context context) {

        super(context);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB) {
            this.setLayerType(View.LAYER_TYPE_SOFTWARE, null);

        }
    }

    /**
     * Modifica el rango de medicion
     * desde minimo hasta maximo
     *
     * @param minimo
     * @param maximo
     */
    public void setRango(float minimo, float maximo) {

        this.minimo = minimo;
        this.maximo = maximo;

    }

    /**
     * Reajusta el rango del gauge según la magnitud de la medida, para que la
     * aguja siempre se mueva dentro de una escala legible en vez de quedarse
     * comprimida contra un extremo. Genérico para cualquier sensor: cada
     * instancia de Gauge le pasa su propio arreglo de umbrales (ordenado de
     * menor a mayor) y este método escoge el primero que alcanza a cubrir la
     * medida actual.
     *
     * @param medida   valor medido
     * @param umbrales límites superiores posibles de la escala, ascendentes
     */
    public void cambiarEscala(float medida, float[] umbrales) {

        float minimo = 0f;
        float maximo = umbrales[umbrales.length - 1];

        for (float umbral : umbrales) {
            if (medida <= umbral) {
                maximo = umbral;
                break;
            }
        }

        this.setRango(minimo, maximo);

    }

    public void setSeparacionesDivisionesGrandes(int separacionDivisionesGrandes){

        this.separacionDivisionesGrandes = separacionDivisionesGrandes;


    }


    private void setNumeroDivisiones(int numeroDivisiones){

        this.numeroDivisiones =numeroDivisiones;


    }


    /**
     * Modifica el valor medido
     *
     * @param medida
     */
    public void setMedida(float medida) {

        this.medida = medida;

    }


    /**
     * Regresa el valor medido
     *
     * @return medida
     */
    public float getMedida() {

        return medida;
    }


    /**
     * Modifica las unidades del instrumento virtual
     *
     * @param unidades
     */
    public void setUnidades(String unidades) {

        this.unidades = unidades;

    }

    /**
     * Modifica el color del borde del marco
     * @param colorBordeMarco
     */

    public void setColorBordeTacometro(int colorBordeMarco){

        this.colorBordeTacometro = colorBordeMarco;

    }


    /**
     * Modifica los colores de los sectores circulares
     *
     * @param colorPrimerTercio
     * @param colorSegundoTercio
     * @param colorTercerTercio
     */
    public void setColorSectores(int colorPrimerTercio, int colorSegundoTercio, int colorTercerTercio) {

        this.colorPrimerTercio = colorPrimerTercio;
        this.colorSegundoTercio = colorSegundoTercio;
        this.colorTercerTercio = colorTercerTercio;

    }

    /**
     * Modifica los angulos de los sectores circulares
     * Deben sumar 250 grados
     *
     * @param angPrimerTercio
     * @param angSegundoTercio
     * @param angTercerTercio
     */
    public void setAngulosSectores(int angPrimerTercio, int angSegundoTercio, int angTercerTercio) {
        this.angPrimertercio = angPrimerTercio;
        this.angSegundoTercio = angSegundoTercio;
        this.angTercerTercio = angTercerTercio;

    }



    /**
     * Modifica el color de fondo del tacometro
     *
     * @param colorFondoTacometro
     */
    public void setColorFondoTacometro(int colorFondoTacometro) {

        this.colorFondoTacometro = colorFondoTacometro;


    }


    /**
     * Modifica el color de las lineas del tacometro
     *
     * @param color_lineas
     */
    public void setColorLineasTacometro(int color_lineas) {

        this.colorLineas = color_lineas;


    }


    public void setColorNumeros(int colorNumeros){

        this.colorNumeros = colorNumeros;

    }


    /**
     * Modifica el color del numero que se despliega
     *
     * @param colorNumerosDesplieggue
     */

    public void setColorNumeroDespliegue(int colorNumerosDesplieggue) {

        this.colorNumerosDesplieggue = colorNumerosDesplieggue;

    }

    /**
     * Modifica el color de fondo del recuadro detrás del número desplegado.
     *
     * @param colorFondoCajaMedida
     */
    public void setColorFondoCajaMedida(int colorFondoCajaMedida) {

        this.colorFondoCajaMedida = colorFondoCajaMedida;

    }

    /**
     * Modifica la posición vertical del número desplegado, como fracción de
     * "largo" (0 = centro del gauge; valores positivos bajan el número,
     * negativos lo suben). Por defecto es 0.2f. Útil cuando el gauge tiene
     * otras vistas pegadas justo debajo y el número se cruza con ellas.
     *
     * @param posicionVerticalMedida
     */
    public void setPosicionVerticalMedida(float posicionVerticalMedida) {

        this.posicionVerticalMedida = posicionVerticalMedida;

    }

    /**
     * Modifica el ángulo de inicio y el barrido angular total de los ticks,
     * números y aguja (en grados, sistema de canvas.rotate: 0°=arriba,
     * aumenta en sentido horario). Por defecto 240°/240°, centrado arriba.
     * Reducir el barrido concentra toda la escala (y por tanto la aguja,
     * para cualquier valor medido) más cerca de la parte superior del
     * gauge, útil cuando hay otras vistas pegadas en la parte baja.
     *
     * @param anguloInicioEscala
     * @param sweepEscala
     */
    public void setEscalaAngular(float anguloInicioEscala, float sweepEscala) {

        this.anguloInicioEscala = anguloInicioEscala;
        this.sweepEscala = sweepEscala;

    }

    /**
     * Modifica si se dibuja o no la marca "IoT.PhysicsSensor".
     *
     * @param mostrarMarcaEmpresa
     */
    public void setMostrarMarcaEmpresa(boolean mostrarMarcaEmpresa) {

        this.mostrarMarcaEmpresa = mostrarMarcaEmpresa;

    }

    /**
     * Modifica el color de la marca "IoT.PhysicsSensor".
     *
     * @param colorMarcaEmpresa
     */
    public void setColorMarcaEmpresa(int colorMarcaEmpresa) {

        this.colorMarcaEmpresa = colorMarcaEmpresa;

    }


    /**
     * Modifica el color de la franja dinámica
     * @param colorFranjaDinamica
     */
    public void setColorFranjaDinámica(int colorFranjaDinamica) {

        this.colorFranjaDinamica = colorFranjaDinamica;


    }


    /**
     * @param canvas
     */

    //método para dibujar
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        /*
        se graba el estado actual del canvas
        para al final restaurarlo
        */
        canvas.save();


         /*
         La vista tendra las mismas dimensiones de su
         contenedor
         */
        float ancho = this.getWidth();//ancho de la vista
        float alto = this.getHeight();//alto de la vista

        /*
         Se define la variable largo como el 80%
         del menor valor entre alto y largo del
         contenedor
         */

        if (ancho > alto) {

            largo = 0.8f * alto;

        } else {

            largo = 0.8f * ancho;


        }

        /*
          se hace tralación del (0,0) al centro
          del contenedor
        */
        canvas.translate(0.5f * ancho, 0.5f * alto);

        //configurando el pincel
        Paint pincel = new Paint();
        //evita efecto sierra
        pincel.setAntiAlias(true);
        //tamaño texto
        pincel.setTextSize(0.05f * largo);
        //para mejor manejo de la métrica de texto
        pincel.setLinearText(true);
        //para efectos de buen escalado de bitmaps
        pincel.setFilterBitmap(true);
        //para buen manejo de gradientes de color
        pincel.setDither(true);



        //dibujar fondo del tacómetro
        //marco borde: circulo no relleno
        pincel.setStyle(Paint.Style.STROKE);
        //grosor y color
        pincel.setStrokeWidth(0.02f*largo);
        pincel.setColor(colorBordeTacometro);
        canvas.drawCircle(0, 0, 0.5f * largo, pincel);
        //marco fondo: círculo relleno
        pincel.setStyle(Paint.Style.FILL);
        pincel.setColor(colorFondoTacometro);
        canvas.drawCircle(0, 0, 0.48f * largo, pincel);



        //dibujar los tres segementos circulares
        float esquinaSuperiorIzquierdaX = -0.45f * largo;
        float esquinaSuperiorIzquierdaY = -0.45f * largo;
        float esquinaInferiorDerechaX = 0.45f * largo;
        float esquinaInferiorDerechaY = 0.45f * largo;

        RectF rect = new RectF(esquinaSuperiorIzquierdaX, esquinaSuperiorIzquierdaY,
                esquinaInferiorDerechaX, esquinaInferiorDerechaY);

        //grosor líneas
        pincel.setStrokeWidth(0.02f*largo);
        //arcos
        pincel.setStyle(Paint.Style.STROKE);

        //los 3 sectores de color están calibrados (100+100+40=240) para el
        //barrido "de fábrica" de 240°; si esta instancia usa un barrido
        //distinto (setEscalaAngular), se escalan proporcionalmente para
        //que sigan sumando exactamente sweepEscala -y arrancan en el mismo
        //ángulo que los ticks/aguja (convertido a coordenadas de drawArc)-
        //así el amarillo/rojo nunca sobran ni faltan respecto al 0/máximo.
        float anguloInicioEscalaEnDrawArc = ((anguloInicioEscala - 90) % 360 + 360) % 360;
        float factorEscalaSectores = sweepEscala / 240f;
        float angPrimerTercioEscalado = angPrimertercio * factorEscalaSectores;
        float angSegundoTercioEscalado = angSegundoTercio * factorEscalaSectores;
        float angTercerTercioEscalado = angTercerTercio * factorEscalaSectores;

        pincel.setColor(colorPrimerTercio);
        //con argumento false solo dibuja el arco y no el sector circular
        canvas.drawArc(rect, anguloInicioEscalaEnDrawArc, angPrimerTercioEscalado, false, pincel);
        pincel.setColor(colorSegundoTercio);
        canvas.drawArc(rect, anguloInicioEscalaEnDrawArc + angPrimerTercioEscalado, angSegundoTercioEscalado, false, pincel);
        pincel.setColor(colorTercerTercio);
        canvas.drawArc(rect, anguloInicioEscalaEnDrawArc + angPrimerTercioEscalado + angSegundoTercioEscalado, angTercerTercioEscalado, false, pincel);


        //dibujar la escala
        float indent = (float) (0.05 * largo);
        float posicionY = (float) (0.5 * largo);



         /*
         Divisiones grandes, pequeñas y números
         Se dibuja primero la división vertical.
         Luego se repite rotando de a 50 grados comenzando
         en 235 grados.
          */
        pincel.setStyle(Paint.Style.FILL);


        for (int i = 0; i < numeroDivisiones +1; i = i + 1) {//6
            // float anguloRotacion = 235 + 50 * i;
            float salto = sweepEscala/numeroDivisiones;
            //float anguloRotacion = 235 + 50 * i;
            float anguloRotacion = anguloInicioEscala + salto * i;
            canvas.save();
            canvas.rotate(anguloRotacion, 0, 0);
            pincel.setColor(colorLineas);

            if(i%separacionDivisionesGrandes ==0) {
                //dibujar líneas grandes
                pincel.setStrokeWidth(0.01f * largo);
                canvas.drawLine(0, -posicionY, 0, -posicionY + indent, pincel);

                //dibujar los números
                float valorIncrementoMarcas = (maximo - minimo) /numeroDivisiones;
                int valorMarca = (int) (minimo + valorIncrementoMarcas * i);
                String numero = "" + valorMarca;

                //ancho de la cadena del número
                float anchoCadenaNumero = pincel.measureText(numero);

                //dibuja números rotados
                //endereza los números a orientación horizontal
                canvas.rotate(-anguloRotacion, 0, -posicionY + 2.5f * indent);
                pincel.setColor(colorNumeros);
                canvas.drawText(numero, -0.5f * anchoCadenaNumero, -posicionY + 2.5f * indent, pincel);
            } else {
                //divisiones pequeñas
                pincel.setStrokeWidth(0.005f * largo);
                canvas.drawLine(0, -posicionY, 0, -posicionY + (float) (0.6 * indent), pincel);

            }

            canvas.restore();

        }

        /*
        dibujar la aguja
        */
        //aqui empieza dibujo de la aguja
        //calcular angulo para ubicar la aguja de acuerdo al valor medido
        float angulo_rotacion_medida = anguloInicioEscala + (sweepEscala / (maximo - minimo)) * (medida - minimo);
        //Dibujar aguja
        pincel.setStrokeWidth(0.005f * largo);
        pincel.setColor(Color.RED);
        canvas.rotate(angulo_rotacion_medida, 0, 0);
        float b = (float) (1.5f* indent);
        canvas.drawLine(0, -posicionY, 0, b, pincel);
        canvas.rotate(-angulo_rotacion_medida, 0, 0);
        //centro de la aguja: punto sólido rojo (antes era un anillo hueco
        //-relleno del color de fondo más un borde delgado- que casi no se
        //veía)
        pincel.setStyle(Paint.Style.FILL);
        pincel.setColor(Color.RED);
        canvas.drawCircle(0, 0, (float) (0.5 * indent), pincel);
        //aquí termina dibujo de la aguja




        //franja dinámica
        float a = (float) 0.03* largo;
        rect = new RectF(esquinaSuperiorIzquierdaX - a, esquinaSuperiorIzquierdaY - a,
                esquinaInferiorDerechaX + a, esquinaInferiorDerechaY + a);
        pincel.setStyle(Paint.Style.STROKE);
        pincel.setColor(colorFranjaDinamica);
        pincel.setStrokeWidth(0.01f*largo);
        //anguloInicioEscalaEnDrawArc ya se calculó arriba, junto a los
        //sectores de color (misma conversión canvas.rotate -> drawArc)
        canvas.drawArc(rect, anguloInicioEscalaEnDrawArc, angulo_rotacion_medida - anguloInicioEscala, false, pincel);


        //Dibujar las unidades
        pincel.setStyle(Paint.Style.FILL);
        pincel.setColor(colorLineas);
        pincel.setTextSize(0.08f * largo);
        float anchoCadenaUnidades = pincel.measureText(unidades);
        canvas.drawText(unidades, -0.5f * anchoCadenaUnidades,- 0.15f * largo, pincel);
        //aqui termina dibujo de las unidades

        //aqui despliegue de la medida, en un recuadro con esquinas
        //redondeadas (fondo configurable, texto configurable) y con un solo
        //decimal para que no se vea un número interminable
        pincel.setTextSize(0.06f * largo); //0.1f * 0.6 = 40% más chico
        String textoMedida = String.format(java.util.Locale.US, "%.1f", medida);
        float anchoCadenaNumero = pincel.measureText(textoMedida);
        float yBaselineMedida = posicionVerticalMedida * largo;

        Paint.FontMetrics metricasTexto = pincel.getFontMetrics();
        float paddingCajaMedida = 0.35f * pincel.getTextSize();
        float anchoCajaMedida = anchoCadenaNumero + 2f * paddingCajaMedida;
        float altoCajaMedida = (metricasTexto.descent - metricasTexto.ascent) + paddingCajaMedida;
        float centroVerticalCajaMedida = yBaselineMedida + (metricasTexto.ascent + metricasTexto.descent) / 2f;

        RectF rectCajaMedida = new RectF(
                -0.5f * anchoCajaMedida, centroVerticalCajaMedida - 0.5f * altoCajaMedida,
                0.5f * anchoCajaMedida, centroVerticalCajaMedida + 0.5f * altoCajaMedida);
        pincel.setStyle(Paint.Style.FILL);
        pincel.setColor(colorFondoCajaMedida);
        canvas.drawRoundRect(rectCajaMedida, 0.15f * altoCajaMedida, 0.15f * altoCajaMedida, pincel);

        pincel.setColor(colorNumerosDesplieggue);
        canvas.drawText(textoMedida, -0.5f * anchoCadenaNumero, yBaselineMedida, pincel);

        //marcar empresa
        if (mostrarMarcaEmpresa) {
            String empresa = "IoT.PhysicsSensor";
            pincel.setTextSize(0.05f * largo);
            pincel.setColor(colorMarcaEmpresa);
            float anchoCadenaNombreEmpresa = pincel.measureText(empresa);
            canvas.drawText(empresa, -0.5f * anchoCadenaNombreEmpresa, 0.35f * largo, pincel);
        }



        //se restaura el canvas al estado incial
        //el que se garbó al principio de este método
        canvas.restore();

        //para efectos de animación
        invalidate();

    }//fin onDraw


}
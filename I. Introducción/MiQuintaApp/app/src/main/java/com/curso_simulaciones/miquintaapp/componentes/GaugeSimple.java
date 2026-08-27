package com.curso_simulaciones.miquintaapp.componentes;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.View;

/**
 * Gauge circular responsivo exacto al diseño de la Figura 46.
 */
public class GaugeSimple extends View {
    private float minimo = -20f;
    private float maximo = 40f;
    private float medida = 32f;
    private int divisionesPrincipales = 6;
    private String unidades = "Gauss";
    private int colorFondo = Color.BLACK;
    private int colorAguja = Color.RED;
    private int colorNumeros = Color.WHITE;
    private int colorEscala = Color.BLUE;
    private int colorUnidades = Color.YELLOW;
    private final Paint pincel = new Paint(Paint.ANTI_ALIAS_FLAG);

    public GaugeSimple(Context context) {
        super(context);
        pincel.setStrokeCap(Paint.Cap.ROUND);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB) {
            this.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }
    }

    /**
     * Modifica el rango de medición (mínimo y máximo)
     */
    public void setRango(float minimo, float maximo) {
        if (maximo <= minimo) throw new IllegalArgumentException("El máximo debe ser mayor que el mínimo");
        this.minimo = minimo;
        this.maximo = maximo;
        invalidate();
    }

    /**
     * Modifica el valor medido
     */
    public void setMedida(float medida) {
        this.medida = medida;
        invalidate();
    }

    /**
     * Regresa el valor medido
     */
    public float getMedida() {
        return medida;
    }

    /**
     * Modifica las unidades
     */
    public void setUnidades(String unidades) {
        this.unidades = unidades;
        invalidate();
    }

    /**
     * Define en cuántos intervalos se divide la escala numérica
     */
    public void setDivisionesEscala(int divisiones) {
        if (divisiones < 1) throw new IllegalArgumentException("Debe haber al menos una división");
        this.divisionesPrincipales = divisiones;
        invalidate();
    }

    /**
     * Modifica el color de fondo
     */
    public void setColorFondo(int color) {
        this.colorFondo = color;
        invalidate();
    }

    /**
     * Modifica el color de la aguja y del borde exterior
     */
    public void setColorAguja(int color) {
        this.colorAguja = color;
        invalidate();
    }

    /**
     * Modifica el color de los números
     */
    public void setColorNumeros(int color) {
        this.colorNumeros = color;
        invalidate();
    }

    /**
     * Modifica el color principal de la escala (tercer sector)
     */
    public void setColorEscala(int color) {
        this.colorEscala = color;
        invalidate();
    }

    /**
     * Modifica el color de las unidades y de la caja digital
     */
    public void setColorUnidades(int color) {
        this.colorUnidades = color;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.save();

        float ancho = this.getWidth();
        float alto = this.getHeight();
        float largo;
        if (ancho > alto) {
            largo = 0.8f * alto;
        } else {
            largo = 0.8f * ancho;
        }

        // Traslación al centro del contenedor
        canvas.translate(0.5f * ancho, 0.5f * alto);

        float radio = 0.5f * largo;

        // Configuración de las propiedades de dibujo del pincel
        pincel.setAntiAlias(true);
        pincel.setLinearText(true);
        pincel.setFilterBitmap(true);
        pincel.setDither(true);

        // 1. Dibujar el fondo del tacómetro (círculo relleno)
        pincel.setStyle(Paint.Style.FILL);
        pincel.setColor(colorFondo);
        canvas.drawCircle(0, 0, radio, pincel);

        // 2. Dibujar el anillo exterior del color de la aguja (rojo por defecto)
        pincel.setStyle(Paint.Style.STROKE);
        pincel.setColor(colorAguja);
        pincel.setStrokeWidth(0.02f * largo);
        canvas.drawCircle(0, 0, radio - 0.01f * largo, pincel);

        // 3. Dibujar el arco de la escala (espesor del trazo 0.02 * largo)
        float radioArc = 0.78f * radio;
        RectF rectArc = new RectF(-radioArc, -radioArc, radioArc, radioArc);
        pincel.setStyle(Paint.Style.STROKE);
        pincel.setStrokeWidth(0.02f * largo);

        // Ángulo de barrido total es de 270 grados
        float anguloDiv = 270f / divisionesPrincipales;

        // Sector 1: Verde (de la primera a la segunda división)
        pincel.setColor(Color.GREEN);
        canvas.drawArc(rectArc, 180f, anguloDiv, false, pincel);

        // Sector 2: Amarillo (de la segunda a la tercera división)
        pincel.setColor(Color.YELLOW);
        canvas.drawArc(rectArc, 180f + anguloDiv, anguloDiv, false, pincel);

        // Sector 3: Color Escala (resto del arco hasta el final)
        pincel.setColor(colorEscala);
        canvas.drawArc(rectArc, 180f + 2f * anguloDiv, 270f - 2f * anguloDiv, false, pincel);

        // 4. Dibujar las divisiones principales (ticks) y los números
        float radioTexto = 0.52f * radio;
        float longitudTick = 0.06f * radio;

        for (int i = 0; i <= divisionesPrincipales; i++) {
            float anguloRotacion = 180f + anguloDiv * i;
            double rad = Math.toRadians(anguloRotacion);

            float valor = minimo + (maximo - minimo) * i / divisionesPrincipales;

            // Determinar color de la marca de división (tick)
            int colorTick = colorNumeros;
            if (Math.abs(valor) < 0.001f) {
                colorTick = colorAguja; // Marca en 0 es roja
            } else if (i == 0) {
                colorTick = colorAguja; // Primera marca es roja
            } else if (i == divisionesPrincipales) {
                colorTick = colorUnidades; // Última marca es del color de las unidades (amarilla)
            }

            // Dibujar línea del tick
            pincel.setColor(colorTick);
            pincel.setStyle(Paint.Style.STROKE);
            pincel.setStrokeWidth(0.008f * largo);
            float xStart = (float) (radioArc * Math.cos(rad));
            float yStart = (float) (radioArc * Math.sin(rad));
            float xEnd = (float) ((radioArc - longitudTick) * Math.cos(rad));
            float yEnd = (float) ((radioArc - longitudTick) * Math.sin(rad));
            canvas.drawLine(xStart, yStart, xEnd, yEnd, pincel);

            // Dibujar número de la división
            pincel.setStyle(Paint.Style.FILL);
            pincel.setTextSize(0.06f * largo);
            pincel.setTextAlign(Paint.Align.CENTER);

            // El número 0 se pinta de rojo
            if (Math.abs(valor) < 0.001f) {
                pincel.setColor(colorAguja);
            } else {
                pincel.setColor(colorNumeros);
            }

            String numeroStr = formatear(valor);
            float xText = (float) (radioTexto * Math.cos(rad));
            float yText = (float) (radioTexto * Math.sin(rad));
            float yPosText = yText - (pincel.descent() + pincel.ascent()) / 2f;
            canvas.drawText(numeroStr, xText, yPosText, pincel);
        }

        // 5. Dibujar la aguja indicadora (cruza de lado a lado)
        float proporcion = (medida - minimo) / (maximo - minimo);
        proporcion = Math.max(0f, Math.min(1f, proporcion)); // limitar entre 0 y 1 por seguridad
        float anguloAguja = 180f + 270f * proporcion;
        double radAguja = Math.toRadians(anguloAguja);

        pincel.setColor(colorAguja);
        pincel.setStyle(Paint.Style.STROKE);
        pincel.setStrokeWidth(0.008f * largo);

        // La aguja tiene un contrapeso de 0.2 * radio y llega hasta 0.72 * radio en la punta
        float xNeedleStart = (float) ((-0.2f * radio) * Math.cos(radAguja));
        float yNeedleStart = (float) ((-0.2f * radio) * Math.sin(radAguja));
        float xNeedleEnd = (float) ((0.72f * radio) * Math.cos(radAguja));
        float yNeedleEnd = (float) ((0.72f * radio) * Math.sin(radAguja));
        canvas.drawLine(xNeedleStart, yNeedleStart, xNeedleEnd, yNeedleEnd, pincel);

        // 6. Dibujar el botón central (hub) formado por dos anillos concéntricos
        // Anillo exterior
        pincel.setStyle(Paint.Style.STROKE);
        pincel.setStrokeWidth(0.025f * radio);
        pincel.setColor(colorAguja);
        canvas.drawCircle(0, 0, 0.11f * radio, pincel);

        // Botón central relleno
        pincel.setStyle(Paint.Style.FILL);
        canvas.drawCircle(0, 0, 0.045f * radio, pincel);

        // 7. Dibujar la pantalla digital en el cuadrante vacío (135 grados, inferior izquierdo)
        float widthBox = 0.34f * radio;
        float heightBox = 0.30f * radio;
        double radBox = Math.toRadians(135);
        float cx = (float) ((0.52f * radio) * Math.cos(radBox));
        float cy = (float) ((0.52f * radio) * Math.sin(radBox));
        RectF boxRect = new RectF(cx - widthBox / 2, cy - heightBox / 2, cx + widthBox / 2, cy + heightBox / 2);

        // Caja de visualización digital con bordes redondeados
        pincel.setStyle(Paint.Style.STROKE);
        pincel.setColor(colorUnidades);
        pincel.setStrokeWidth(0.005f * largo);
        canvas.drawRoundRect(boxRect, 0.05f * radio, 0.05f * radio, pincel);

        // Medida en la caja
        pincel.setStyle(Paint.Style.FILL);
        pincel.setColor(colorUnidades);
        pincel.setTextSize(0.11f * radio);
        pincel.setTextAlign(Paint.Align.CENTER);

        String medidaStr = formatear(medida);
        canvas.drawText(medidaStr, cx, cy - 0.01f * radio - (pincel.descent() + pincel.ascent()) / 2f, pincel);

        // Unidades en la caja
        pincel.setTextSize(0.055f * radio);
        canvas.drawText(unidades, cx, cy + 0.08f * radio - (pincel.descent() + pincel.ascent()) / 2f, pincel);

        canvas.restore();

        // Llamar a invalidate() para animaciones o refresco continuo
        invalidate();
    }

    private String formatear(float valor) {
        return Math.abs(valor - Math.round(valor)) < 0.001f ? String.valueOf(Math.round(valor)) : String.format("%.1f", valor);
    }
}

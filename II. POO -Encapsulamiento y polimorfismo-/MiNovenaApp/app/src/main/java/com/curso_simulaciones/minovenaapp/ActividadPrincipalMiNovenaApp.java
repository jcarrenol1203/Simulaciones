package com.curso_simulaciones.minovenaapp;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.curso_simulaciones.minovenaapp.objetos_laboratorio.Rueda;
import com.curso_simulaciones.minovenaapp.vista.CR;
import com.curso_simulaciones.minovenaapp.vista.Pizarra;

public class ActividadPrincipalMiNovenaApp extends Activity implements Runnable {
    private Pizarra pizarra;
    private final Rueda[] ruedas = new Rueda[4];
    private final long periodoMuestreo = 50L;
    private float tiempo;
    private Thread hilo;

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
        
        hilo = new Thread(this);
        hilo.start();
    }

    /*crear los objetos de la interfaz gráfica de usuario (GUI)*/
    private void crearElementosGui() {
        pizarra = new Pizarra(this);
        pizarra.setBackgroundColor(Color.WHITE);
    }

    /*organizar la distribución de los objetos de de la GUI usando
    administradores de diseño*/
    private LinearLayout crearGui() {
        // el linear principal que ocupará toda la pantalla
        LinearLayout linearPrincipal = new LinearLayout(this);
        linearPrincipal.setOrientation(LinearLayout.VERTICAL);
        linearPrincipal.setBackgroundColor(Color.BLACK);

        // Parametros para pegar la pizarra a pantalla completa con márgenes
        LinearLayout.LayoutParams parametrosPegado = new
                LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        parametrosPegado.setMargins(50, 50, 50, 50);
        
        // pegar la pizarra directamente al contenedor principal sin barra inferior
        linearPrincipal.addView(pizarra, parametrosPegado);

        return linearPrincipal;
    }

    @Override
    public void run() {
        boolean sinInicializar = true;
        while (!Thread.currentThread().isInterrupted()) {
            try {
                Thread.sleep(periodoMuestreo);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }

            // La pizarra debe estar medida antes de convertir porcentajes a píxeles.
            if (sinInicializar && pizarra.getWidth() != 0 && pizarra.getHeight() != 0) {
                crearRuedasConResponsividad();
                sinInicializar = false;
            }
            if (!sinInicializar) {
                tiempo += 0.05f;
                cambiarEstadosEscenaPizarra(tiempo);
            }
        }
    }

    private void crearRuedasConResponsividad() {
        CR.anchoPizarra = pizarra.getWidth();
        CR.altoPizarra = pizarra.getHeight();
        crearObjetosLaboratorio();
    }

    private void crearObjetosLaboratorio() {
        // Los cuatro objetos Rueda con diferentes colores y tamaños responsivos
        ruedas[0] = new Rueda(CR.pcApxX(100f), CR.pcApxY(100f), CR.pcApxL(12f)); // Esquina inferior derecha
        ruedas[0].setColor(Color.RED);
        
        ruedas[1] = new Rueda(CR.pcApxX(50f), CR.pcApxY(50f), CR.pcApxL(9f)); // Centro
        ruedas[1].setColor(Color.BLUE);
        
        ruedas[2] = new Rueda(CR.pcApxX(0f), CR.pcApxY(25f), CR.pcApxL(7f)); // Fila al 25% alto
        ruedas[2].setColor(Color.rgb(0, 140, 0)); // Verde
        
        ruedas[3] = new Rueda(CR.pcApxX(0f), CR.pcApxY(75f), CR.pcApxL(10f)); // Fila al 75% alto
        ruedas[3].setColor(Color.MAGENTA);
        
        pizarra.setEstadoEscena(ruedas);
    }

    private void cambiarEstadosEscenaPizarra(float tiempo) {
        // Rueda 1: rota con velocidad constante en la esquina inferior derecha
        ruedas[0].mover(80f * tiempo);
        
        // Rueda 2: rota con velocidad constante en el centro
        ruedas[1].mover(-120f * tiempo);
        
        // Desplazamiento horizontal de izquierda a derecha
        float desplazamientoX = CR.pcApxX(5f * tiempo);
        
        // Rueda 3: se traslada a la derecha a 0.25 de la altura sin rotar
        ruedas[2].mover(desplazamientoX, 0f);
        
        // Rueda 4: se traslada a la derecha a 0.75 de la altura y rota
        ruedas[3].mover(desplazamientoX, 0f, 160f * tiempo);
    }

    @Override
    protected void onDestroy() {
        if (hilo != null) hilo.interrupt();
        super.onDestroy();
    }
}

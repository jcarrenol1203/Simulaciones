package com.curso_simulaciones.mioctavaapp;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.curso_simulaciones.mioctavaapp.objetos_laboratorio.Polea;
import com.curso_simulaciones.mioctavaapp.vista.CR;
import com.curso_simulaciones.mioctavaapp.vista.Pizarra;

public class ActividadPrincipalMiOctavaApp extends Activity implements Runnable {
    private Pizarra pizarra;
    private final Polea[] poleas = new Polea[10];
    private final long periodoMuestreo = 50L;
    private float tiempo;
    private Thread hilo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        crearElementosGui();
        setContentView(crearGui(), new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        hilo = new Thread(this);
        hilo.start();
    }

    private void crearElementosGui() {
        pizarra = new Pizarra(this);
        pizarra.setBackgroundColor(Color.WHITE);
    }

    private LinearLayout crearGui() {
        LinearLayout linearPrincipal = new LinearLayout(this);
        linearPrincipal.setOrientation(LinearLayout.VERTICAL);
        linearPrincipal.setBackgroundColor(Color.BLACK);
        linearPrincipal.setWeightSum(10f);

        LinearLayout linearArriba = new LinearLayout(this);
        LinearLayout linearAbajo = new LinearLayout(this);
        linearAbajo.setBackgroundColor(Color.YELLOW);

        LinearLayout.LayoutParams parametrosArriba = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 0, 8.5f);
        parametrosArriba.setMargins(50, 50, 50, 50);
        linearPrincipal.addView(linearArriba, parametrosArriba);

        LinearLayout.LayoutParams parametrosAbajo = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 0, 1.5f);
        linearPrincipal.addView(linearAbajo, parametrosAbajo);

        linearArriba.addView(pizarra);
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
                crearPoleasConResponsividad();
                sinInicializar = false;
            }
            if (!sinInicializar) {
                tiempo += 0.05f;
                cambiarEstadosEscenaPizarra(tiempo);
            }
        }
    }

    private void crearPoleasConResponsividad() {
        CR.anchoPizarra = pizarra.getWidth();
        CR.altoPizarra = pizarra.getHeight();
        crearObjetosLaboratorio();
    }

    private void crearObjetosLaboratorio() {
        float radio = CR.pcApxL(10f);
        poleas[0] = new Polea(CR.pcApxX(0f), CR.pcApxY(0f), radio);
        poleas[1] = new Polea(CR.pcApxX(100f), CR.pcApxY(0f), radio);
        poleas[1].setColorPolea(Color.BLACK);
        poleas[2] = new Polea(CR.pcApxX(0f), CR.pcApxY(100f), radio);
        poleas[2].setColorPolea(Color.BLUE);
        poleas[3] = new Polea(CR.pcApxX(100f), CR.pcApxY(100f), radio);
        poleas[3].setColorPolea(Color.MAGENTA);
        poleas[4] = new Polea(CR.pcApxX(50f), CR.pcApxY(50f), radio);
        poleas[4].setColorPolea(Color.rgb(200, 200, 0));
        poleas[5] = new Polea(CR.pcApxX(0f), CR.pcApxY(25f), radio);
        poleas[5].setColorPolea(Color.BLACK);
        poleas[6] = new Polea(CR.pcApxX(0f), CR.pcApxY(75f), radio);
        poleas[6].setColorPolea(Color.BLACK);
        pizarra.setEstadoEscena(poleas);
    }

    private void cambiarEstadosEscenaPizarra(float tiempo) {
        poleas[0].moverPolea(50f * tiempo);
        poleas[1].moverPolea(100f * tiempo);
        poleas[2].moverPolea(-50f * tiempo);
        poleas[3].moverPolea(-100f * tiempo);
        poleas[4].moverPolea(200f * tiempo);

        float desplazamientoX = CR.pcApxX(5f * tiempo);
        poleas[5].moverPolea(desplazamientoX, 0f);
        poleas[6].moverPolea(desplazamientoX, 0f, 200f * tiempo);
    }

    @Override
    protected void onDestroy() {
        if (hilo != null) hilo.interrupt();
        super.onDestroy();
    }
}

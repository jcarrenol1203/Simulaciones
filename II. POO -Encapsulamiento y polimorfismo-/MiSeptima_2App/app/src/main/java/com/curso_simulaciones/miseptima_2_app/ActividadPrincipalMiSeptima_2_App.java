package com.curso_simulaciones.miseptima_2_app;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.curso_simulaciones.miseptima_2_app.objetos_laboratorio.Polea;
import com.curso_simulaciones.miseptima_2_app.vista.Pizarra;

public class ActividadPrincipalMiSeptima_2_App extends Activity implements Runnable {
    private Pizarra pizarra;
    private Polea polea_1, polea_2, polea_3;
    private final Polea[] poleas = new Polea[10];
    private final long periodoMuestreo = 50L;
    private float tiempo;
    private Thread hilo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        crearElementosGui();
        crearObjetosLaboratorio();

        ViewGroup.LayoutParams parametroLayoutPrincipal = new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        );
        setContentView(crearGui(), parametroLayoutPrincipal);

        hilo = new Thread(this);
        hilo.start();
    }

    private void crearElementosGui() {
        pizarra = new Pizarra(this);
        pizarra.setBackgroundColor(Color.WHITE);
    }

    private void crearObjetosLaboratorio() {
        polea_1 = new Polea(100f, 100f, 50f);
        poleas[0] = polea_1;

        polea_2 = new Polea(100f, 250f, 100f);
        polea_2.setColorPolea(Color.BLACK);
        poleas[1] = polea_2;

        polea_3 = new Polea(100f, 450f, 50f);
        polea_3.setColorPolea(Color.MAGENTA);
        poleas[2] = polea_3;

        pizarra.setEstadoEscena(poleas);
    }

    private LinearLayout crearGui() {
        LinearLayout linearPrincipal = new LinearLayout(this);
        linearPrincipal.setOrientation(LinearLayout.VERTICAL);
        linearPrincipal.setGravity(Gravity.FILL);
        linearPrincipal.setBackgroundColor(Color.rgb(250, 150, 50));

        LinearLayout.LayoutParams parametrosPegada = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 0
        );
        parametrosPegada.setMargins(50, 50, 50, 50);
        parametrosPegada.weight = 1.0f;
        linearPrincipal.addView(pizarra, parametrosPegada);
        return linearPrincipal;
    }

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                Thread.sleep(periodoMuestreo);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
            tiempo += 0.1f;
            cambiarEstadosEscenaPizarra(tiempo);
        }
    }

    private void cambiarEstadosEscenaPizarra(float tiempo) {
        float desplazamientoX1 = 20f * tiempo;
        poleas[0].moverPolea(desplazamientoX1, 0f);

        float teta2 = 50f * tiempo;
        poleas[1].moverPolea(teta2);

        float desplazamientoX3 = 20f * tiempo;
        float teta3 = 50f * tiempo;
        poleas[2].moverPolea(desplazamientoX3, 0f, teta3);
    }

    @Override
    protected void onDestroy() {
        if (hilo != null) {
            hilo.interrupt();
        }
        super.onDestroy();
    }
}

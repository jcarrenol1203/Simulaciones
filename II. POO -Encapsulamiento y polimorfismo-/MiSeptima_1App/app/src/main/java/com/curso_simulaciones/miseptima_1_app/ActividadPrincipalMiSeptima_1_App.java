package com.curso_simulaciones.miseptima_1_app;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.curso_simulaciones.miseptima_1_app.objetos_laboratorio.Polea;
import com.curso_simulaciones.miseptima_1_app.vista.Pizarra;

public class ActividadPrincipalMiSeptima_1_App extends Activity {

    private Pizarra pizarra;
    private Polea polea_1, polea_2, polea_3;
    private final Polea[] poleas = new Polea[3];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        crearElementosGui();

        ViewGroup.LayoutParams parametroLayoutPrincipal = new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        );
        setContentView(crearGui(), parametroLayoutPrincipal);
        crearObjetosLaboratorio();
    }

    private void crearElementosGui() {
        pizarra = new Pizarra(this);
        pizarra.setBackgroundColor(Color.BLACK);
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

    private void crearObjetosLaboratorio() {
        polea_1 = new Polea();
        poleas[0] = polea_1;

        polea_2 = new Polea(600f, 200f, 150f);
        poleas[1] = polea_2;

        polea_3 = new Polea(600f, 600f, 150f);
        polea_3.setColorPolea(Color.GREEN);
        poleas[2] = polea_3;

        pizarra.setEstadoEscena(poleas);
    }
}

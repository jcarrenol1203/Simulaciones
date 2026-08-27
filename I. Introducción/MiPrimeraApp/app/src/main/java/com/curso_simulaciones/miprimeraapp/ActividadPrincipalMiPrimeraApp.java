package com.curso_simulaciones.miprimeraapp;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ActividadPrincipalMiPrimeraApp extends Activity {

    private TextView cadena;
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
/*llamada al méodo para crear los elementos de la interfaz grafica
de usuario (GUI)*/
        crearElementosGui();
/*para informar cómo se debe adaptar la GUI a la pantalla del
dispositivo*/
        ViewGroup.LayoutParams parametro_layout_principal = new
                ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
/*pegar al contenedor la GUI: en el argumento se está llamando al método
crearGui()*/
        LinearLayout contenedorPrincipal = crearGui();
        this.setContentView(contenedorPrincipal, parametro_layout_principal);
/*dejar espacio para la barra de estado y de navegación del sistema,
para que el texto no quede debajo de ellas (el padding va en el texto,
no en el contenedor, así el fondo azul sigue llegando hasta el borde)*/
        contenedorPrincipal.setOnApplyWindowInsetsListener((v, insets) -> {
            cadena.setPadding(insets.getSystemWindowInsetLeft(), insets.getSystemWindowInsetTop(),
                    insets.getSystemWindowInsetRight(), insets.getSystemWindowInsetBottom());
            return insets;
        });
    }
    //crear los objetos de la interfaz gráfica de usuario (GUI)
    private void crearElementosGui(){
        cadena=new TextView(this);
        cadena.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        cadena.setTextColor(Color.YELLOW);
        cadena.setText("HOLA MUNDO");
    }
    //organizar la distribución de los objetos de la GUI usando administradores de diseño
    private LinearLayout crearGui(){
        //administrador de diseño
        LinearLayout linear_principal = new LinearLayout(this);
        linear_principal.setOrientation(LinearLayout.VERTICAL);
        linear_principal.setGravity(Gravity.CENTER_HORIZONTAL);
        linear_principal.setGravity(Gravity.FILL);
        linear_principal.setBackgroundColor(Color.BLUE);
//pegar el objeto cadena (es tipo TextView)
        linear_principal.addView(cadena);
        return linear_principal;

    }
}

package com.curso_simulaciones.mivigesimaquintaapp.controlador;

import com.curso_simulaciones.mivigesimaquintaapp.datos.AlmacenDatosRAM;
import com.curso_simulaciones.mivigesimaquintaapp.modelo.ModeloFisico;

public class HiloAnimacion extends Thread {


    public boolean pausa = true;
    private boolean corriendo;
    private long periodo_muestreo = 100;
    public float tiempo = 0;

    private ModeloFisico modelo = new ModeloFisico();

    private ActividadControladora actividad;


    public HiloAnimacion(ActividadControladora actividad) {

        this.actividad = actividad;

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

            if (pausa == false) {
                tiempo = tiempo + 0.01f;
                setCalculos(tiempo);

                actualizarFisica();

            }

        }

    }

    private void setCalculos(float tiempo) {
        float m1 = AlmacenDatosRAM.m1;
        float m2 = AlmacenDatosRAM.m2;
        float m3 = AlmacenDatosRAM.m3;
        modelo.setCalculos(tiempo, m1, m2, m3);

    }


    private void actualizarFisica() {

        actividad.cambiarEstadosEscenaPizarra();

        /*
         Nota 1 del taller: la simulación NO debe permitir golpear los
         bloques (m1, m2, m3) contra las poleas ni contra el piso.
         Cuando alguno de ellos llega a uno de esos límites, la
         animación reinicia su repetición (se sueltan de nuevo desde
         el reposo, en su posición inicial).
        */

        float semiAlto = AlmacenDatosRAM.semiAltoMasa;
        float yPoleasFijas = AlmacenDatosRAM.yPoleasFijas_en_pixeles;
        float yPisoArriba = AlmacenDatosRAM.yPisoArriba_en_pixeles;
        float radioGrande = AlmacenDatosRAM.radioGrande;
        float radioChica = AlmacenDatosRAM.radioChica;

        float yP = AlmacenDatosRAM.yP_en_pixeles;
        float y1 = AlmacenDatosRAM.y1_en_pixeles;
        float y2 = AlmacenDatosRAM.y2_en_pixeles;
        float y3 = AlmacenDatosRAM.y3_en_pixeles;

        //m1 y m2 chocarían contra la polea móvil P si suben demasiado,
        //o contra el piso si bajan demasiado
        boolean m1_choca_P = (y1 - semiAlto) <= (yP + radioChica);
        boolean m1_choca_piso = (y1 + semiAlto) >= yPisoArriba;

        boolean m2_choca_P = (y2 - semiAlto) <= (yP + radioChica);
        boolean m2_choca_piso = (y2 + semiAlto) >= yPisoArriba;

        //m3 chocaría contra la polea fija derecha o contra el piso
        boolean m3_choca_polea = (y3 - semiAlto) <= (yPoleasFijas + radioGrande);
        boolean m3_choca_piso = (y3 + semiAlto) >= yPisoArriba;

        //resguardo adicional: que P mismo no choque contra la polea fija izquierda ni contra el piso
        boolean P_choca_polea = (yP - radioChica) <= (yPoleasFijas + radioGrande);
        boolean P_choca_piso = (yP + radioChica) >= yPisoArriba;

        if (m1_choca_P || m1_choca_piso || m2_choca_P || m2_choca_piso
                || m3_choca_polea || m3_choca_piso || P_choca_polea || P_choca_piso) {

            tiempo = 0.0f;

        }

    }


}

package com.curso_simulaciones.mivigesimaquintaapp.modelo;

import com.curso_simulaciones.mivigesimaquintaapp.datos.AlmacenDatosRAM;
import com.curso_simulaciones.mivigesimaquintaapp.vista.CR;

/*
 Modelo físico de la escena de MiVigesimaSegundaApp: dos poleas fijas
 ideales unidas por una cuerda superior; del lado izquierdo esa misma
 cuerda sostiene a una polea móvil P (ideal, sin masa); sobre P pasa
 una segunda cuerda ideal de la que cuelgan m1 y m2; del lado derecho
 la cuerda superior sostiene directamente a m3. Cuerdas inextensibles,
 poleas ideales (sin masa ni fricción). Los bloques parten del reposo.

 Deducción (ejes hacia abajo, Figura 12B):

 Ligadura 1 (cuerda superior, tensión T_sup uniforme):
   y3 + yP = cte   =>   a3 = -aP

 Ligadura 2 (cuerda sobre P, tensión T_inf uniforme):
   (y1 - yP) + (y2 - yP) = cte   =>   a1 + a2 = 2*aP

 Segunda ley de Newton (positivo hacia abajo):
   m3*a3 = m3*g - T_sup
   m1*a1 = m1*g - T_inf
   m2*a2 = m2*g - T_inf
   Polea P sin masa: T_sup = 2*T_inf

 Resolviendo el sistema (S = 1/m1 + 1/m2 + 4/m3):
   T_inf = 4g/S           T_sup = 2*T_inf = 8g/S
   a1 = g - T_inf/m1       a2 = g - T_inf/m2
   aP = (a1+a2)/2          a3 = -aP
 */
public class ModeloFisico {

    //gravedad, en m/s2 (SI)
    private final float g = 9.8f;

    private float factorConversion_metroApixel, factorConversion_pixelAmetro;

    public ModeloFisico() {

    }

    /*
      Dados los valores de las masas y el tiempo transcurrido desde que
      los bloques fueron soltados (v inicial = 0), calcula tensiones,
      aceleraciones, desplazamientos y posiciones, y los deja en
      AlmacenDatosRAM para que la Pizarra y la ActividadControladora
      los usen.
     */
    public void setCalculos(float tiempo, float m1, float m2, float m3) {

        factorConversion();

        float S = (1f / m1) + (1f / m2) + (4f / m3);

        float T_inf = 4f * g / S;
        float T_sup = 2f * T_inf;

        float a1 = g - T_inf / m1;
        float a2 = g - T_inf / m2;
        float aP = 0.5f * (a1 + a2);
        float a3 = -aP;

        //desplazamientos en metros desde la posición de reposo (v0 = 0)
        float d1 = 0.5f * a1 * tiempo * tiempo;
        float d2 = 0.5f * a2 * tiempo * tiempo;
        float d3 = 0.5f * a3 * tiempo * tiempo;
        float dP = 0.5f * aP * tiempo * tiempo;

        AlmacenDatosRAM.desplazamiento_m1_en_metros = d1;
        AlmacenDatosRAM.desplazamiento_m2_en_metros = d2;
        AlmacenDatosRAM.desplazamiento_m3_en_metros = d3;
        AlmacenDatosRAM.desplazamiento_P_en_metros = dP;

        //desplazamientos en pixeles
        float d1_px = factorConversion_metroApixel * d1;
        float d2_px = factorConversion_metroApixel * d2;
        float d3_px = factorConversion_metroApixel * d3;
        float dP_px = factorConversion_metroApixel * dP;

        AlmacenDatosRAM.desplazamiento_m1_en_pixeles = d1_px;
        AlmacenDatosRAM.desplazamiento_m2_en_pixeles = d2_px;
        AlmacenDatosRAM.desplazamiento_m3_en_pixeles = d3_px;
        AlmacenDatosRAM.desplazamiento_P_en_pixeles = dP_px;

        //posiciones actuales en pixeles
        float y1_px = AlmacenDatosRAM.yi1_en_pixeles + d1_px;
        float y2_px = AlmacenDatosRAM.yi2_en_pixeles + d2_px;
        float y3_px = AlmacenDatosRAM.yi3_en_pixeles + d3_px;
        float yP_px = AlmacenDatosRAM.yiP_en_pixeles + dP_px;

        AlmacenDatosRAM.y1_en_pixeles = y1_px;
        AlmacenDatosRAM.y2_en_pixeles = y2_px;
        AlmacenDatosRAM.y3_en_pixeles = y3_px;
        AlmacenDatosRAM.yP_en_pixeles = yP_px;

        //posiciones actuales en metros (origen: altura de las poleas fijas)
        AlmacenDatosRAM.y1_en_metros = factorConversion_pixelAmetro * y1_px;
        AlmacenDatosRAM.y2_en_metros = factorConversion_pixelAmetro * y2_px;
        AlmacenDatosRAM.y3_en_metros = factorConversion_pixelAmetro * y3_px;
        AlmacenDatosRAM.yP_en_metros = factorConversion_pixelAmetro * yP_px;

        /*
         rotación de las poleas fijas: giran lo que se desplaza la
         cuerda superior, es decir, lo que se desplaza P (en magnitud
         es igual a lo que se desplaza m3, con signo opuesto).

         Se antepone un signo "-": Canvas.rotate(grados) de Android
         gira en sentido HORARIO para grados positivos (a diferencia
         de la convención matemática habitual), así que para que la
         polea gire antihorario cuando la cuerda se desplaza hacia
         abajo por su lado (P bajando, arco = +dP_px) hay que pasarle
         un ángulo negativo.
        */
        float teta_fijas = -(float) Math.toDegrees(d1_px_seguro(dP_px, AlmacenDatosRAM.radioGrande));

        /*
         rotación de la polea móvil P: gira lo que se desplaza la
         cuerda de m1 (o m2) RELATIVA a P. Mismo signo "-" que arriba,
         por la misma razón (Canvas.rotate positivo = horario).
        */
        float teta_P = -(float) Math.toDegrees(d1_px_seguro(d1_px - dP_px, AlmacenDatosRAM.radioChica));

        AlmacenDatosRAM.teta_fijas = teta_fijas;
        AlmacenDatosRAM.teta_P = teta_P;

        //enviar resultados a AlmacenDatosRAM
        AlmacenDatosRAM.a1 = a1;
        AlmacenDatosRAM.a2 = a2;
        AlmacenDatosRAM.a3 = a3;
        AlmacenDatosRAM.aP = aP;

        AlmacenDatosRAM.T_inf = T_inf;
        AlmacenDatosRAM.T_sup = T_sup;

        AlmacenDatosRAM.tiempo = tiempo;

    }

    //evita dividir por un radio de cero (por si aún no se ha fijado la geometría)
    private float d1_px_seguro(float arco, float radio) {

        if (radio == 0f) return 0f;

        return arco / radio;

    }

    private void factorConversion() {

        /*
        Para dar una equivalencia de pixeles en metros se asume que
        el 100% del ALTO de la pantalla (en posición LANDSCAPE)
        equivale a 2 m, tal como lo pide la ayuda del taller
        (Figura 12B). Con base en esto:

        factorConversion_metroApixel = (ALTO en pixeles / 2 metros)
        factorConversion_pixelAmetro = (2 metros / ALTO en pixeles)
        */

        factorConversion_metroApixel = CR.pcApxY(100f) / 2f;

        factorConversion_pixelAmetro = 2f / CR.pcApxY(100f);

    }

}

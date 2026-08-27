package com.curso_simulaciones.mivigesimaquintaapp.datos;

public class AlmacenDatosRAM {


    public static float ancho_pantalla, alto_pantalla;
    public static int tamanoLetraResolucionIncluida;

    //masas en kg: m1 y m2 cuelgan de la polea móvil P, m3 cuelga de la polea fija derecha
    public static float m1, m2, m3;

    //radios de las poleas en pixeles
    public static float radioGrande, radioChica;

    //semi-alto del bloque (masa) en pixeles, usado para los límites de choque
    public static float semiAltoMasa;

    public static float tiempo;

    //aceleraciones en m/s2 (positivas hacia abajo, eje Y de la Figura 12B)
    public static float a1, a2, a3, aP;

    //tensiones en N: T_sup = cuerda que pasa por las dos poleas fijas (sostiene a P y a m3)
    //T_inf = cuerda que pasa por la polea móvil P (sostiene a m1 y a m2)
    public static float T_sup = 0, T_inf = 0;

    /*
     desplazamiento angular (grados) de rotación de las poleas:
     teta_fijas = misma rotación para la polea izquierda y la derecha (una sola cuerda)
     teta_P = rotación de la polea móvil P
     */
    public static float teta_fijas, teta_P;

    //posiciones X (constantes, en pixeles) de cada elemento móvil
    public static float x1_en_pixeles, x2_en_pixeles, x3_en_pixeles, xP_en_pixeles;

    //posiciones Y iniciales (en pixeles), medidas desde el origen (altura de las poleas fijas)
    public static float yi1_en_pixeles, yi2_en_pixeles, yi3_en_pixeles, yiP_en_pixeles;

    //posiciones Y actuales (en pixeles)
    public static float y1_en_pixeles, y2_en_pixeles, y3_en_pixeles, yP_en_pixeles;

    //posiciones Y actuales (en metros), origen en la altura de las poleas fijas, eje Y hacia abajo
    public static float y1_en_metros, y2_en_metros, y3_en_metros, yP_en_metros;

    //desplazamientos respecto de la posición inicial de cada corrida (soltados desde el reposo)
    public static float desplazamiento_m1_en_metros, desplazamiento_m2_en_metros, desplazamiento_m3_en_metros, desplazamiento_P_en_metros;
    public static float desplazamiento_m1_en_pixeles, desplazamiento_m2_en_pixeles, desplazamiento_m3_en_pixeles, desplazamiento_P_en_pixeles;

    //límites geométricos de la escena (en pixeles) usados para evitar que los bloques choquen
    //con las poleas o con el piso
    public static float yPoleasFijas_en_pixeles;
    public static float yPisoArriba_en_pixeles;
}

package com.curso_simulaciones.mitrigesimacuartaapp.datos;

public class AlmacenDatosRAM {

    public static int dimensioReferencia, alto, ancho;
    public static int tamanoLetraResolucionIncluida;
    public static float datoActual;
    public static boolean configurar;
    public static int periodoMuestreo=500;//en ms
    public static float tiempo;
    public static int nDatos=50;
    public static String path;

    //componentes del acelerómetro leídas en cada muestreo
    public static float ax;
    public static float ay;
    public static float az;
    public static float a;

}

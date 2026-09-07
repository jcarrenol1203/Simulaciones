package com.curso_simulaciones.mitrigesimanovenaapp.datos;

import java.util.ArrayList;

public class AlmacenDatosRAM {

    public static int ancho, alto, dimensionReferencia, tamanoLetraResolucionIncluida;

    public static String conexion_bluetooth;

    public static int estado_conexion_bluetooth=1;

    public static String direccion;


    public static int nDatos=20;

    public static int periodo_muestreo= 1000; // 1 segundo

    public static String unidades = "unidades";

    public static String tiempo;

    public static double[] x = new double[1000];

    public static double[] y = new double[1000];

     /*
      ArrayList es una clase que permite almacenar
      objetos con la diferencia respecto a los
      arreglos [], que ellla misma va cambiando
      dinámicamente su tamaño a medida que se le
      agregan elementos
     */

    public static ArrayList datos = new ArrayList<>();

}
package com.curso_simulaciones.minovenaapp.vista;

/** Convierte las medidas lógicas (porcentajes) en píxeles reales del canvas. */
public final class CR {
    // Ancho real de la pizarra; se asigna cuando Android ya la midió.
    public static float anchoPizarra;
    // Alto real de la pizarra; se asigna al mismo tiempo que el ancho.
    public static float altoPizarra;

    // Evita crear objetos CR: todos sus datos y métodos son estáticos.
    private CR() { }

    // Convierte un porcentaje horizontal, por ejemplo 50%, a píxeles.
    public static float pcApxX(float porcentajeX) { return porcentajeX * anchoPizarra / 100f; }
    // Convierte un porcentaje vertical a píxeles.
    public static float pcApxY(float porcentajeY) { return porcentajeY * altoPizarra / 100f; }
    // Usa el lado menor para que radios y longitudes no se deformen.
    public static float pcApxL(float porcentajeLongitud) {
        return porcentajeLongitud * Math.min(anchoPizarra, altoPizarra) / 100f;
    }
}

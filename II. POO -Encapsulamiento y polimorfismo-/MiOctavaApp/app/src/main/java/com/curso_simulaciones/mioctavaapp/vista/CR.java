package com.curso_simulaciones.mioctavaapp.vista;

/** Conversión entre porcentajes del canvas y píxeles. */
public final class CR {
    public static float anchoPizarra;
    public static float altoPizarra;

    private CR() { }

    public static float pcApxX(float pcX) { return pcX * anchoPizarra / 100f; }
    public static float pcApxY(float pcY) { return pcY * altoPizarra / 100f; }

    public static float pcApxL(float pcL) {
        return pcL * Math.min(anchoPizarra, altoPizarra) / 100f;
    }

    public static float pxXApc(float pxX) { return pxX * 100f / anchoPizarra; }
    public static float pxYApc(float pxY) { return pxY * 100f / altoPizarra; }

    public static float pxApcL(float pxL) {
        return pxL * 100f / Math.min(anchoPizarra, altoPizarra);
    }
}

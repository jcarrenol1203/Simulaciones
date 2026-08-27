package com.curso_simulaciones.midecimacuartaapp.elementos_del_espacio;

/**
 * ObjetoEspacial: raíz de TODA la jerarquía de este taller.
 *
 * Es "abstract" porque no representa ningún objeto espacial
 * concreto por sí misma (no existe "un objeto espacial" genérico
 * en la escena, solo existen Marcianos, Selenitas, Venusianos y
 * EstrellasFijas) - por lo tanto nunca se debe poder escribir
 * "new ObjetoEspacial()", y el compilador lo impide justamente
 * por llevar la palabra abstract.
 *
 * "implements Dibujable" significa que ObjetoEspacial (y por
 * herencia, TODO lo que descienda de ella) promete tener un
 * método dibujese(Canvas, Paint). Pero fíjate que esta clase
 * NO escribe el método dibujese() aquí (a propósito, según el
 * enunciado del taller) - eso es perfectamente válido porque
 * ObjetoEspacial también es abstracta: una clase abstracta
 * puede "heredar" una obligación de una interfaz y NO cumplirla
 * ella misma, dejando que sea alguna subclase (más abajo en el
 * árbol) quien finalmente la cumpla.
 *
 * Esta clase no tiene atributos ni métodos propios: su único
 * trabajo es servir de "tipo común" para que, más adelante,
 * Pizarra pueda guardar en un solo arreglo ObjetoEspacial[]
 * tanto Extraterrestres (Marciano/Selenita/Venusiano) como
 * EstrellasFijas, y dibujarlos a todos con un solo bucle
 * polimórfico, aunque no compartan ni un atributo entre sí.
 */
public abstract class ObjetoEspacial implements Dibujable {

    /**
     * Constructor por defecto.
     * No hay nada que inicializar aquí: esta clase no
     * declara atributos propios.
     */
    public ObjetoEspacial() {

    }
}
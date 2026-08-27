# CLAUDE.md

> Reglas generales del curso y seguimiento de teoría (dominada/pendiente): ver
> `../CLAUDE.md` en la raíz de `Simulaciones/`. Este archivo solo cubre el resumen técnico
> específico de los proyectos de esta carpeta (módulo 9, creación de una librería Android).

## Resumen técnico del módulo (basado en modulo_9.docx)

Este módulo construye una librería Java para Android llamada **`simulphysics`** (objetos
dibujables de laboratorio de física + un solucionador numérico), y luego la reutiliza desde
un segundo proyecto independiente. Son dos apps encadenadas:

### `MiVigesimaApp/` — construir la librería

Proyecto con tres paquetes de la app anfitriona (`controlador`, `vista`, `datos` —
`ActividadControladora`, `Pizarra`+`CR`, `AlmacenDatosRAM`, más
`ActividadPrincipalMiVigesimaApp` como lanzador) que sirve de banco de pruebas para construir
un **módulo de tipo Android Library** dentro del mismo proyecto, llamado `simulphysics`, con
dos paquetes:

- **`objetos_laboratorio`** — interfaz `Dibujable` (`dibujese(Canvas, Paint)`) → clase
  abstracta `ObjetoLaboratorio` (implementa `Dibujable`; color, posición, grosor de línea) →
  clase abstracta `CuerpoRigido` (agrega centro de masa, eje y ángulo de rotación, métodos
  `mover(...)`/`rotar(...)`). De ahí cuelgan las clases concretas: `Polea`, `Rueda`,
  `CuerpoRectangular` (y su hija `Masa`, que además dibuja una etiqueta de texto) heredan de
  `CuerpoRigido`; `Resorte`, `Flecha`, `Regla`, `Marca`, `Cuerda`, `Particula` heredan
  directo de `ObjetoLaboratorio` (no tienen cuerpo rígido rotable con eje, se posicionan más
  simple).
- **`metodos_numericos`** — `Estado` (t, x, v: value object del estado de una EDO de 2º
  orden) y `RungeKuttaOrdenDos` (clase abstracta): implementa el algoritmo de Runge-Kutta de
  orden 2 en `resolver(tf, Estado, h)` de forma genérica, dejando `f(x,v,t)` como método
  abstracto — quien extienda la clase solo debe definir la ecuación diferencial concreta.

Una vez compilada, la librería se extrae como `.jar` desde
`app/simulphysics/build/intermediates/compile_library_classes_jar/debug/bundleLibCompileToJarDebug/classes.jar`
y se renombra a `simulphysics.jar`.

### `MiVigesimaPrimeraApp/` — consumir la librería

Proyecto nuevo e independiente. `simulphysics.jar` se copia a `app/libs/` y se declara en
`build.gradle` (Module: app) con `implementation(files("libs/simulphysics.jar"))` — a
diferencia de módulos anteriores, aquí la reutilización es por `.jar` compilado, no por
código fuente compartido ni por módulo Gradle enlazado. Reconstruye la misma arquitectura
`controlador`/`vista`/`datos` de siempre, pero ahora `ActividadControladora` arma una escena
física (aparenta una máquina de Atwood: dos masas, dos poleas, cuerdas, reglas, flechas)
usando únicamente las clases de `simulphysics` — es la prueba de que la librería es
reutilizable fuera del proyecto donde se creó.

### `MiVigesimaSegundaApp/` — taller del módulo (resuelto)

Mismo esqueleto que `MiVigesimaPrimeraApp` (paquetes `controlador`/`vista`/`datos`,
`simulphysics.jar` copiado a `app/libs/`), pero con una escena física propia armada solo a
partir de la figura del taller (sin código dado en el docx): un suelo negro
(`CuerpoRectangular`), una columna amarilla parada sobre él (`CuerpoRectangular`), dos
poleas azules fijas ancladas a las esquinas superiores de la columna
(`setSoportePolea(true)`), una polea verde móvil `P` colgada a la izquierda mediante una
cuerda que baja de la polea izquierda, y tres masas (`Masa`, sin `setMarca` interno) —
`m1`/`m2` colgando de `P` a lados opuestos, `m3` colgando de la polea derecha. Las
etiquetas `P`, `m1`, `m2`, `m3` son objetos `Marca` aparte, puestos por fuera de cada
bloque/polea (a diferencia de `MiVigesimaPrimeraApp`, que sí usaba `setMarca` dentro del
bloque). Todo posicionado con porcentajes vía `CR` para mantener responsividad. Pendiente
real: el ícono de lanzador personalizado (el estudiante lo hace después) y probarla en
dispositivos físicos distintos.

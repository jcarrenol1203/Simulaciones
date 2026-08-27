# CLAUDE.md

> Reglas generales del curso y seguimiento de teoría (dominada/pendiente): ver
> `../CLAUDE.md` en la raíz de `Simulaciones/`. Este archivo solo cubre el resumen técnico
> específico de los dos proyectos de esta carpeta (módulo 6, herencia e interfaces).

## Resumen técnico del repositorio

Esta carpeta contiene dos proyectos Android/Gradle independientes que ilustran el mismo ejercicio antes y después de aplicar herencia:

- `MiDecimaApp/` — versión "antes": cada figura se guarda en su propio arreglo tipado (`Polea[]`, `Rueda[]`, `CuerpoRectangular[]`), y `Pizarra.setEstadoEscena(...)` recibe esos tres arreglos por separado.
- `MiDecimaPrimeraApp/` — versión "después": todas las figuras se guardan y dibujan a través de un único arreglo `CuerpoRigido[]`, y `Pizarra.setEstadoEscena(CuerpoRigido[])` recibe un solo parámetro. Esta es la que demuestra polimorfismo.

Son casi duplicados a propósito — comparar ambas carpetas es el objetivo del ejercicio, así que antes de "corregir" algo que parezca duplicado entre ambas, revisa si es justamente ese el punto pedagógico.

### Comandos comunes

Ejecutar desde dentro de cada proyecto (`MiDecimaApp/` o `MiDecimaPrimeraApp/`):

```bash
./gradlew assembleDebug        # compilar APK debug
./gradlew installDebug         # compilar e instalar en dispositivo/emulador conectado
./gradlew test                 # tests unitarios JVM (app/src/test)
./gradlew connectedAndroidTest # tests instrumentados en dispositivo/emulador (app/src/androidTest)
./gradlew clean
```

Test individual:
```bash
./gradlew test --tests "com.curso_simulaciones.midecimaprimeraapp.ExampleUnitTest"
```

Ambos proyectos usan el Gradle wrapper con Gradle 9.5.0 y toolchain JDK 21 (auto-provisto vía foojay). `compileSdk`/`targetSdk` = 36, `minSdk` = 24.

### Arquitectura (por capas)

Cada app es una sola `Activity` sin layouts XML (la UI se arma en código) con tres capas:

1. **Activity** (`ActividadPrincipal...App`) — implementa `Runnable` y corre un loop de animación en un `Thread` en segundo plano (`periodo_muestreo` = 50ms). Espera a que `Pizarra` tenga ancho/alto no nulos antes de crear los objetos (`crearObjetosConResponsividad()`), para poder ubicarlos como porcentaje del canvas. En cada tick, avanza `tiempo` y llama `cambiarEstadosEscenaPizarra(tiempo)`, que aplica el modelo cinemático de cada figura (MU, MCU, oscilación armónica) llamando `mover(...)`/`rotar(...)`.

2. **`objetos_laboratorio`** — jerarquía de herencia:
   - `CuerpoRigido` (clase base): posición del centro de masa, eje y ángulo de rotación, color; expone `mover(dx,dy)`, `mover(angulo)`, `mover(dx,dy,angulo)`, `rotar(ejeX,ejeY,angulo)` y `dibujese(Canvas,Paint)` (dibuja un círculo con la letra "R" por defecto).
   - `Rueda`, `Polea`, `CuerpoRectangular` extienden `CuerpoRigido`, agregan campos propios (radio, o largo/alto) y **sobreescriben `dibujese(...)`** para dibujarse distinto, reutilizando el estado de posición/rotación heredado.
   - En `MiDecimaPrimeraApp`, ese contrato compartido de `dibujese` es lo que permite que `Pizarra` recorra un solo `CuerpoRigido[]` de forma polimórfica.

3. **`vista`**:
   - `Pizarra` — `View` personalizada que guarda el arreglo de figuras actual y las redibuja en `onDraw`, llamando `invalidate()` al final de cada frame (así se mantiene el loop de animación visualmente).
   - `CR` — helpers estáticos de conversión porcentaje↔píxel (`pcApxX/Y`, `pxXApc/Y`, y para longitudes `pcApxL`/`pxApcL`, que usan el menor entre ancho y alto como referencia). Todas las posiciones/tamaños se definen en porcentaje y se convierten a píxeles una vez se conocen las dimensiones reales del canvas — así la escena es responsiva.


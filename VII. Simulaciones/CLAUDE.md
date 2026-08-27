# CLAUDE.md

> Reglas generales del curso y seguimiento de teoría (dominada/pendiente): ver
> `../CLAUDE.md` en la raíz de `Simulaciones/`. Este archivo solo cubre el resumen técnico
> específico de los proyectos de esta carpeta (módulo 10, simulaciones con hilo de
> animación y GUI interactiva).

## Resumen técnico del módulo (basado en modulo_10.docx)

Este módulo introduce la arquitectura `controlador/modelo/vista/datos` con un **hilo de
animación** (`HiloAnimacion extends Thread`) que recalcula la física cuadro a cuadro y una
GUI real (`SeekBar` + `Button`) para variar parámetros en caliente, además de un panel de
resultados dibujado directamente sobre el `Canvas` de la `Pizarra`. Son tres apps:

### `MiVigesimaTerceraApp2/` — máquina de Atwood con polea móvil (2 masas)

Ejemplo resuelto: `m1` cuelga de una polea fija; el otro extremo de esa cuerda sostiene el
eje de una segunda polea, móvil, sobre la que pasa una segunda cuerda con un extremo fijo
al techo y el otro con `m2`. `ModeloFisico` calcula aceleraciones/tensión en forma cerrada
(cinemática con `v0=0`); `HiloAnimacion` corre cada 100&nbsp;ms, acumula `tiempo` y llama a
`ActividadControladora.cambiarEstadosEscenaPizarra()`. Introduce el patrón de **reinicio
por choque**: si `y1` o `y2` (en pixeles) se salen del rango permitido de la regla, se hace
`tiempo = 0`, lo que — como la posición es `yi + ½·a·t²` — hace que el bloque vuelva a su
posición inicial y la animación "repita" desde el reposo. GUI dividida 80/20 (pizarra /
controles) con `LinearLayout` de pesos, dos `SeekBar` (m1, m2) y botones EMPEZAR↔NUEVO y
PAUSAR↔CONTINUAR.

### `MiVigesimaCuartaApp/` — tiro parabólico (referencia de panel de resultados)

Cinemática de proyectil con arrastre táctil para fijar la posición inicial
(`Pizarra.onTouch` + `dibujarEstadoInicial`). Aporta el patrón de **panel de valores**: la
`Pizarra` dibuja con `canvas.drawText(...)`, en monoespaciado, una columna de resultados
(desplazamientos, tiempo, posición, velocidades, aceleración) sobre el propio lienzo de la
escena, sin una `View` separada.

### `MiVigesimaQuintaApp/` — taller del módulo (resuelto): máquina de Atwood doble

Taller final (última sección de `modulo_10.docx`, Figura 12A/12B): "Hacer un proyecto...
que ilustre la simulación... permitir cambiar las masas... desplegar aceleraciones,
desplazamientos, posiciones y tensión... unidades SI". Combina los dos ejemplos
anteriores con la disposición física de **`MiVigesimaSegundaApp`** (módulo VI, sin
modificarla): dos poleas fijas unidas por una cuerda superior, una polea móvil `P`
colgada de la izquierda con `m1`/`m2` a sus lados, y `m3` colgando de la derecha —
geometría en porcentaje (`CR.pcApxL/X/Y`) copiada tal cual de
`MiVigesimaSegundaApp.crearObjetosLaboratorio()`.

- **Física** (`modelo/ModeloFisico.java`): máquina de Atwood doble (dos ligaduras de
  cuerda: `a3 = -aP` y `a1+a2 = 2·aP`, más `T_sup = 2·T_inf` por la polea móvil sin masa).
  Deducción completa, con el caso de equilibrio como verificación, en
  `documentacion/Analisis_Modelo_Fisico.md` (mismo contenido publicado como Artifact).
  Escala: 100% del alto de pantalla (landscape) = 2&nbsp;m, tal como pide la ayuda del
  taller.
- **GUI**: tres `SeekBar` (`m1`, `m2`, `m3`, 1–25&nbsp;kg) en vez de dos; cambiar cualquier
  masa reinicia `hilo.tiempo = 0` (una masa nueva es un experimento nuevo, se suelta de
  nuevo desde el reposo) — a diferencia de `MiVigesimaTerceraApp2`, donde cambiar la masa
  mientras está en pausa no reinicia el tiempo.
- **Choque**: el límite superior de `m1`/`m2` es la posición *actual* (no fija) de la
  propia polea móvil `P`, porque `P` también se mueve; `m3` usa el borde de la polea fija
  derecha. Ambos bloqueados también contra el piso; como resguardo extra, `P` no puede
  chocar contra la polea fija izquierda ni contra el piso.
- **Etiquetas**: `m1`/`m2`/`m3` usan `Masa.setMarca(...)` (la etiqueta viaja pegada al
  bloque en cada `mover(...)`), a diferencia de `MiVigesimaSegundaApp` que las ponía como
  objetos `Marca` externos. La etiqueta de `P` sí necesita reposicionarse cuadro a cuadro
  porque la polea se desplaza; para eso se creó `vista/EtiquetaMovil extends Marca` con un
  método `actualizarPosicion(x,y)` (la librería `simulphysics.Marca` no expone un setter de
  posición).
- Pendiente real (tareas del estudiante, no de código): ícono de lanzador personalizado y
  probar el `.apk` en varios dispositivos Android, tal como pide el taller.

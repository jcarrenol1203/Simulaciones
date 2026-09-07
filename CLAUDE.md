# CLAUDE.md

Curso "Simulaciones IV + IoT + DL" (Industria 5.0), Facultad de Ciencias, Depto. de Física,
Universidad Nacional de Colombia (sede Medellín), profesor Diego L. Aristizábal Ramírez.
Combina Android Studio con Java (simulaciones y POO), ESP32 (instrumentación virtual e IoT)
y conceptos básicos de Deep Learning. Es el último semestre de ingeniería física del
estudiante: tiene bases sólidas en electrónica digital e instrumentación virtual, nivel medio
en IA/IoT, pero este es su primer contacto real con POO y con Java.

## Estructura de la carpeta

Cada módulo romano es una unidad del curso con su(s) PDF/Word teórico(s) y varias apps
Android/Gradle (Java, sin layouts XML — la UI se arma en código):

- `I. Introducción/` — bases de Android Studio, primeras apps (MiPrimeraApp … MiQuintaApp).
- `II. POO -Encapsulamiento y polimorfismo-/` — MiSextaApp … MiNovenaApp.
- `III. POO -Herencia e Interface-/` — MiDecimaApp … MiDecimaCuartaApp. Tiene su propio
  `CLAUDE.md` con el resumen técnico de esos proyectos (arquitectura Activity/objetos_laboratorio/vista).
- `IV. Estructura de una app en Android/` — MiDecimaQuintaApp.
- `V. Las GUI en ANDROID/` — MiDecimaSextaApp … MiDecimaNovenaApp.
- `VI. Creación de Libreria/` — MiVigesimaApp, MiVigesimaPrimeraApp, MiVigesimaSegundaApp
  (módulo 9). Tiene su propio `CLAUDE.md` con el resumen técnico de la librería
  `simulphysics`.
- `VII. Simulaciones/` — MiVigesimaTerceraApp2 … MiVigesimaQuintaApp (módulo 10). Tiene su
  propio `CLAUDE.md` con el resumen técnico (poleas móviles, hilo de animación, GUI con
  deslizadores).
- `VIII. Sensores en dispositivos móviles/` — MiVigesimaSextaApp… (módulo 11), teórico en
  `modulo_11.docx`. Uso de `SensorManager`/`Sensor` para detectar y leer sensores del
  dispositivo (acelerómetro, giroscopio, gravedad, temperatura ambiente, luz, campo
  magnético, presión, proximidad, humedad relativa, etc.).
- `IX. Flujo de Datos/` — MiTrigesimaApp… (módulo 12), teórico en `modulo_12.docx`. Flujos
  (streams) de E/S en Java (`InputStream`/`OutputStream` de bytes, `Reader`/`Writer` de
  caracteres), lectura/escritura de archivos en almacenamiento interno y externo de Android
  (Scoped Storage desde Android 10/11), serialización.
- `X. IV + BT + Android/` — MiTrigesimaQuintaApp, MiTrigesimaSextaApp (módulo 13), teórico en
  `modulo_13.docx`. Comunicación cliente-servidor por Bluetooth clásico (`BluetoothSocket`,
  `BluetoothServerSocket`) entre dos dispositivos Android, usando la librería propia
  `comunicaciones.aar` (en `app/libs/`) para el escaneo/emparejamiento de dispositivos
  (`ScannerBluetooth`).
- `XI. IV+ BT + Android + ESP32/` — MiTrigesimaSeptimaApp… (módulo 14), teórico en
  `modulo_14.docx`. Patrón cliente-servidor por **BLE** entre un Android (cliente) y una
  placa **ESP32-S3** (servidor): servicios/características GATT con UUID propios,
  `BluetoothGatt`/`BluetoothGattCallback` en el lado Android, y firmware en C++/Arduino
  (`BLEDevice`, `BLEServer`, `ArduinoJson`) en el lado ESP32. El estudiante usa **Arduino
  IDE** para programar la ESP32-S3 (el sketch `.ino` vive en la raíz de su carpeta, mismo
  nombre que la carpeta, como exige Arduino IDE). El proyecto también tiene un scaffold de
  PlatformIO (`platformio.ini`, carpeta `src/`) dentro de `esp32-firmware/`, pero no es el
  que se usa para compilar/subir — solo Arduino IDE. **Enfoque actual**.

Dentro de cada módulo, la dinámica es: las **actividades** se resuelven copiando/adaptando
apps anteriores paso a paso siguiendo el módulo; las **tareas** son un reto adicional a partir
de lo ya hecho, del cual solo se tiene el enunciado (sin ejemplo resuelto) — ahí conviene
planear antes de escribir código en vez de ir directo a la solución.

Cuando dos apps de un mismo módulo son casi idénticas a propósito (p. ej. una versión "antes"
y otra "después" de aplicar un concepto), es intencional — el objetivo es compararlas, no
unificarlas. Antes de "corregir" una duplicación aparente entre dos apps, confirmar si ese es
justamente el punto pedagógico del ejercicio.

## Reglas de interacción (vibecoding)

El curso está pensado para vibecoding, pero el estudiante quiere entender qué se está
haciendo, no solo obtener código que funcione. Antes de escribir o modificar código, dar
primero la teoría del concepto involucrado (p. ej. qué hace `this` vs `super`, qué es una
clase abstracta, etc.), salvo que el punto ya esté marcado como "Dominada" abajo.

Cuando el estudiante diga "ya domino X", mover ese punto de "Pendiente" a "Dominada" en este
archivo (y dejar de explicarlo de más, salvo que pida repaso explícito). Si aparece un
concepto nuevo en una actividad, agregarlo a "Pendiente".

### Flujo de git al terminar una app

Repo ya inicializado y en GitHub: https://github.com/jcarrenol1203/Simulaciones (privado),
rama `main`, remoto `origin` configurado — no hace falta `git init` ni `gh repo create` de
nuevo. Cuando el estudiante dé por terminada y aprobada una app (actividad o tarea), el
siguiente paso es subirla a git y luego a GitHub, pero el estudiante quiere teclear los
comandos él mismo para practicar — **no ejecutar `git add`/`commit`/`push` en su lugar**. En
vez de eso, al final de la sesión de esa app, darle el paso a paso exacto de comandos de
terminal para que él los corra (típicamente `git add <rutas>`, `git commit -m "..."`,
`git push`), con una breve nota de qué hace cada uno.

## Seguimiento de teoría (global, válido para todos los módulos)

### Dominada
- Clases y objetos, atributos y métodos (clase = plantilla/tipo; objeto = instancia con
  `new`; método = operación ejecutada sobre un objeto concreto, con acceso implícito a sus
  atributos)
- Herencia (`extends`), jerarquías de clases (relación "es-un(a)"; herencia simple en Java;
  `protected` para que las hijas accedan a lo del padre)
- `this` vs `super` (`this` = referencia al objeto actual, usado sobre todo para desambiguar
  parámetro vs. atributo con mismo nombre; `super` = llama al constructor/método del padre,
  `super(...)` siempre primera línea del constructor)
- Polimorfismo (misma línea `objeto.metodo(...)` sobre una variable de tipo padre, pero cada
  objeto real ejecuta su propia versión sobrescrita del método — enlace dinámico en tiempo de
  ejecución; ejemplo: `cuerpos[i].dibujese(...)` en `Pizarra` con el arreglo `CuerpoRigido[]`)
- Constructores por defecto y sobrecargados (mismo nombre de la clase, sin retorno; el "sin
  parámetros" desaparece automáticamente en cuanto agregas uno propio con parámetros, por eso
  se escriben ambos a mano; constructores sobrecargados de una subclase pueden invocar
  distintos `super(...)` del padre)
- Encapsulamiento (`private`/`protected`/`public`, getters/setters — ejemplo de `mover()` en
  vez de un `setPosicion(x,y)` genérico, como control intencional del punto de entrada al
  estado)
- Clases abstractas e interfaces (abstracta = no instanciable, mezcla métodos concretos +
  abstractos obligatorios, herencia simple; interfaz = contrato puro sin estado, una clase
  puede implementar varias a la vez; ejemplo real ya en código: `ActividadPrincipal...
  implements Runnable`, usado por `Thread`)

### Pendiente / en construcción
- Sobreescritura de métodos (`@Override`) vs sobrecarga (explicado con ejemplos — falta
  confirmación explícita)
- Hilos (`Thread`, `Runnable`) para animación
- Vistas personalizadas de Android (`View`, `onDraw`, `invalidate()`)
- Creación y empaquetado de librerías Android en Java: módulo de tipo "Android Library"
  dentro del proyecto, documentación con javadoc, y consumo del `.jar` compilado
  (`classes.jar` → renombrado, copiado a `app/libs/`, referenciado con
  `implementation(files("libs/....jar"))`) desde otro proyecto independiente (módulo VI)
- Método numérico de Runge-Kutta de orden 2 para resolver EDOs de segundo orden
  (`RungeKuttaOrdenDos`: clase abstracta reutilizable que resuelve cualquier ecuación
  `d²x/dt² = f(x,v,t)` con solo implementar el método abstracto `f`; separa el algoritmo
  numérico del problema físico concreto — módulo VI)
- API de sensores (`SensorManager`, `Sensor`, `getDefaultSensor(tipo)`): consulta de
  sensores disponibles y sus propiedades (nombre, vendedor, rango, resolución, delay);
  variantes wake-up/non-wakeup de un mismo sensor físico — módulo VIII
- Flujos de datos (streams) en Java (`java.io`): jerarquía de bytes (`InputStream`/
  `OutputStream`) vs. caracteres (`Reader`/`Writer`), filtros que se anidan sobre un canal
  base (`BufferedReader`, `OutputStreamWriter`/`InputStreamReader` para pasar de bytes a
  caracteres), los 4 pasos crear/abrir/leer-o-escribir/cerrar canal, y por qué `flush()`
  antes de `close()` — módulo IX
- Almacenamiento interno vs. externo en Android (`openFileOutput`/`openFileInput` vs.
  `File`+`FileOutputStream`/`FileInputStream`), y Scoped Storage desde Android 10/11
  (`getExternalFilesDir(null)` como carpeta propia de la app sin permisos runtime, en vez
  de `Environment.getExternalStorageDirectory()`) — módulo IX
- Comunicación cliente-servidor por Bluetooth clásico (`BluetoothSocket`, flujos de
  entrada/salida sobre el socket RFCOMM) — módulo X
- Comunicación cliente-servidor por BLE Android↔ESP32 (`BluetoothGatt`,
  `BluetoothGattCallback`, servicios/características GATT identificados por UUID,
  naturaleza asíncrona de la conexión BLE vs. el socket bloqueante del Bluetooth clásico) —
  módulo XI

## Agentes/skills preferidos (plugin ECC)

Este repo tiene instalado globalmente el plugin ECC (marketplace, no por-proyecto). De sus
~68 agentes y ~286 skills, los siguientes son los que aplican al stack real de este curso
(Java puro, Gradle con Kotlin DSL, Android sin Kotlin, sin frameworks web/backend, sin BD):

- `java-coding-standards` (skill) — estándares de Java, aplica a los 223 `.java` del repo.
- `java-build-resolver` (agent) — errores de compilación/Gradle en Java (usar la parte
  genérica; ignorar sus sugerencias específicas de Spring Boot/Quarkus, que no aplican aquí).
- `gradle-build` (skill) — errores de Gradle específicos de Android.
- `android-clean-architecture` (agent) — única guía Android disponible en el plugin.
- `java-reviewer` (agent) — revisión de POO/estructura en Java (mismo caveat de Spring/Quarkus).

El resto queda en "biblioteca" (disponible pero no se carga por defecto): todo lo de
Kotlin/React/Django/Spring/bases de datos/seguridad/etc. no tiene evidencia en este repo.
Reevaluar cuando: (a) empiece código real de ESP32 (agentes de C++/Arduino), (b) empiece el
módulo de Deep Learning (agentes de PyTorch/MLE). Git ya está inicializado (ver "Flujo de git
al terminar una app" arriba), pero el estudiante ejecuta los comandos él mismo, así que
git-workflow/PR/code-review (basados en diff) siguen sin uso proactivo de mi parte.

## Entorno técnico

- Android SDK instalado por Android Studio en `~/Android/Sdk` (cada proyecto ya lo referencia
  vía su propio `local.properties`, generado automáticamente — no tocar a mano).
- No hay ningún AVD (emulador) creado todavía; para correr una app hay que crear uno desde
  Android Studio > Device Manager, o conectar un dispositivo físico con depuración USB.
- JDK 21 (`openjdk 21.0.11`) vía toolchain de Gradle (auto-provisto con foojay), Gradle 9.5.0.
- Comandos comunes, ejecutados dentro de la carpeta de cada app:
  ```bash
  ./gradlew assembleDebug        # compilar APK debug
  ./gradlew installDebug         # compilar e instalar en dispositivo/emulador conectado
  ./gradlew test                 # tests unitarios JVM
  ./gradlew connectedAndroidTest # tests instrumentados en dispositivo/emulador
  ```

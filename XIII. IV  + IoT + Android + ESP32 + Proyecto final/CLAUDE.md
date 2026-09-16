# CLAUDE.md

> Reglas generales del curso y seguimiento de teoría (dominada/pendiente): ver
> `../CLAUDE.md` en la raíz de `Simulaciones/`. Este archivo solo cubre el resumen técnico
> de lo que se tuvo en cuenta al construir MiCuadragesimaTerceraApp (módulo 16, MQTT
> Android↔ESP32-S3), a grandes rasgos — no línea por línea.

## `MiCuadragesimaTerceraApp/` — LED RGB, Android publica color por MQTT

Primera app del curso con patrón **publicador-suscriptor** (en vez de cliente-servidor
directo como en BLE del módulo XI): tanto el Android como la ESP32-S3 son *clientes* de un
mismo *broker* MQTT compartido por el curso; no hay conexión directa entre ellos. El Android
publica un JSON `{"r":.., "g":.., "b":..}` a un tópico y la ESP32-S3, suscrita a ese mismo
tópico, lo recibe y aplica el color a un LED RGB (mismo montaje físico GPIO 11/12/13 + blink
de verificación en GPIO 1 que en el módulo XI).

- **Librería Android — Paho, pero `MqttAsyncClient` directo, no `MqttAndroidClient`**: se usa
  el mismo fork mantenido por hannesa2 que en Bluetooth clásico
  (`com.github.hannesa2:paho.mqtt.android:3.3.5`, vía JitPack en `settings.gradle.kts`), pero
  la clase `ClientePubSubMQTT` implementa `MqttCallback`/`IMqttActionListener` sobre
  `MqttAsyncClient` en vez de usar el histórico `MqttAndroidClient` — ese último quedó
  inseguro desde Android 12 porque depende de un `AlarmReceiver` interno que el sistema ya no
  acepta.
- **La URL del broker necesita el esquema completo** (`tcp://45.56.74.248:1883`, no solo la
  IP): `MqttAsyncClient` soporta varios protocolos (`tcp://`, `ssl://`, `ws://`, `wss://`) en
  un solo string de conexión, a diferencia de `PubSubClient` en el ESP32, que recibe host y
  puerto como dos parámetros separados (`mqttCliente.setServer(mqtt_server, mqtt_port)`). El
  tópico, usuario y contraseña deben coincidir *exactamente* entre la pantalla de Ajustes de
  la app (`ActividadConfiguracion`, guardado en `SharedPreferences`) y las constantes del
  `.ino` — un tópico distinto entre ambos lados hace que el ESP32 nunca reciba nada.
- **Bug real encontrado: faltaba `package ...;` en `ClientePubSubMQTT.java`**: el archivo
  vivía físicamente en la carpeta `comunicaciones/` pero no tenía la declaración `package` al
  inicio. Java no infiere el paquete de la ruta de carpetas — sin esa línea, `javac` no
  reconoce el archivo como parte de `comunicaciones`, así que el `import` en
  `ActividadComoClientePubMQTT.java` fallaba con `package ... does not exist` en vez del
  típico `cannot find symbol`. Fix: agregar la línea `package` como primera línea de código
  del archivo (antes de los `import`).
- **Blink de verificación en GPIO 1 no bloqueante con `millis()`**: igual que en el módulo XI,
  pero aquí el motivo es distinto — no es solo estética del blink, sino que `loop()` también
  tiene que llamar a `mqttCliente.loop()` (la función que procesa mensajes MQTT entrantes y
  dispara el `callback()`). Con dos `delay(1000)` bloqueantes para el blink, `mqttCliente.
  loop()` solo corría una vez cada ~2 segundos, causando un retraso notorio entre mandar un
  color desde el celular y verlo reflejado en el LED. Se reemplazó por lógica de
  `millis()`/`tiempoAnteriorParpadeo`/`estadoParpadeo`, dejando `loop()` libre para llamar a
  `mqttCliente.loop()` en cada vuelta.
- **Pendiente, no arreglado a petición del estudiante (solo se pidió el fix de `millis()`)**:
  el hilo `hilo` de `ActividadComoClientePubMQTT` (`Thread`/`Runnable`, publica cada 200 ms)
  nunca se detiene — su `run()` es un `while(true)` sin condición de salida, y no hay
  `onDestroy()` que lo pare. Como un `Thread` en Java no está atado al ciclo de vida de la
  Activity que lo lanzó, salir de la pantalla (o volver a entrar y presionar "EMPEZAR" de
  nuevo) deja hilos "zombie" corriendo en segundo plano, cada uno publicando al mismo tópico
  con el color que tenía congelado al momento de crearse — varios hilos zombie a la vez
  producen justo el síntoma observado de que el LED alterna entre el color recién mandado y
  un color "por defecto" antiguo. Corregir cuando se retome esta app: bandera
  `volatile boolean` en vez de `while(true)`, apagada en `onDestroy()`.

## `MiCuadragesimaCuartaApp/` — luxómetro BH1750, ESP32 publica por MQTT

Segunda app del módulo 16: se invierte el rol respecto a la Tercera — ahora la ESP32-S3
**publica** (sensor GY-30/BH1750 de iluminancia por I2C) y el Android se **suscribe**,
desplegando la medida en gauge/tabla/gráfica. Construida copiando MiCuadragesimaTerceraApp
como base (mismo patrón de `AlmacenDatosRAM`/`ActividadConfiguracion`/`ClientePubSubMQTT`) y
adaptándola:

- **`Boton`, `DialogoSalir` y `ClientePubSubMQTT` sin cambios** (solo el `package`), tal como
  indica la guía — es la misma clase cliente MQTT de la app anterior, aquí simplemente se lee
  (`cliente.leerString()`) en vez de publicar.
- **`GaugeSimple`/`Graficador`/`Luxometro`/`TablaSimple` reutilizadas del módulo XI**
  (`MiTrigesimaOctavaApp`, que ya hace exactamente este mismo luxómetro por BLE) en vez de
  volver a transcribirlas de la guía — API y comportamiento verificados idénticos. También se
  reutilizó de ahí la imagen `luxometro_android.png` del fondo del menú principal.
- **`MPAndroidChart` agregado vía JitPack** (`com.github.PhilJay:MPAndroidChart:v3.1.0`) —
  necesario para `Graficador extends LineChart`; el repositorio JitPack ya estaba en
  `settings.gradle.kts` por Paho, así que no hizo falta agregarlo de nuevo.
- **Bug real de la guía en el `.ino`, corregido al transcribir**: `reconnect()` llama
  `mqttCliente.connect(clientId.c_str(), mqttUser, mqttPassword)`, pero las constantes
  declaradas más arriba se llaman `mqtt_usuario`/`mqtt_clave` — `mqttUser`/`mqttPassword` no
  existen en el sketch, así que tal cual la guía **no compila** en Arduino IDE (`'mqttUser'
  was not declared in this scope`). Se transcribió usando los nombres realmente declarados
  (`mqtt_usuario`, `mqtt_clave`) para que compile; si se prefiere la fidelidad literal a la
  guía (a costa de que falle la compilación, como el bug intencional que se dejó en
  MiTrigesimaSeptimaApp del módulo XI) hay que revertir ese cambio.
- **Compilación verificada** con `./gradlew assembleDebug` (Java + recursos + empaquetado
  APK) antes de entregar la app — a diferencia de MiCuadragesimaTerceraApp, aquí no hubo bugs
  de `import`/`package` que corregir después.
- Pendiente para cuando se pruebe con hardware real: reemplazar `ssid`/`password` (placeholder
  `"xxxxxx"`) en el `.ino`, y decidir un tópico propio (la guía deja `"topico"` por defecto,
  igual que el bug de tópico compartido que se vio en la Tercera).

## `MiCuadragesimaQuintaApp/` — DHT11 + HC-SR04 por MQTT (TAREA del módulo 16)

A diferencia de las dos anteriores (actividades con ejemplo resuelto en el docx), esta es la
**tarea** del módulo 16 — el docx solo trae el enunciado (Figuras 12-16), sin código. La
jerarquía de clases se sacó de la Figura 12 (imagen embebida del docx, no del texto): a
diferencia del patrón `GaugeSimple`+subclase por sensor usado en el módulo XI y en
MiCuadragesimaCuartaApp, aquí la guía solo lista **una clase `Gauge`** (sin
`Distanciometro`/`Termometro`/etc.) y una `Tabla` (renombrada de `TablaSimple`).

- **ESP32-S3 publica un solo JSON con 3 medidas**: `distancia` (HC-SR04, `trigPin=15`/
  `echoPin=16`, mismos pines que `MiTrigesimaNovenaApp` del módulo XI), `temperatura` y
  `humedad` (DHT11, `DATA` en GPIO 42). Un solo tópico, un solo mensaje por ciclo de muestreo
  — no hace falta multiplicar clientes/tópicos.
- **Corrección de la velocidad del sonido**: el enunciado pide explícitamente corregir la
  velocidad del sonido con temperatura y humedad (no solo temperatura). Se usó la fórmula
  empírica `v = 331.3 + 0.606*T + 0.0124*HR` (m/s) en vez de la constante fija `340` que tenía
  `MiTrigesimaNovenaApp`. El DHT11 se lee *antes* de medir la distancia para tener T/HR
  frescos en la fórmula; si una lectura puntual del DHT11 falla (`isnan`), se reutiliza el
  último valor válido en vez de propagar `NaN` al cálculo.
- **`Gauge.cambiarEscala(medida, umbrales[])` genérico**: en vez de 3 subclases con brackets
  hardcodeados (como `Distanciometro`/`Luxometro` en el módulo XI), se agregó un solo método a
  `Gauge` que recibe un arreglo de umbrales ascendentes y usa el primero que cubre la medida
  actual como `maximo` del rango. Cada instancia (distancia/temperatura/humedad) le pasa su
  propio arreglo — cumple "rango dinámico en los 3 gauges" sin triplicar lógica, y coincide
  con que la Figura 12 solo muestra una clase `Gauge`.
- **Cluster de 3 gauges superpuestos (Figuras 13-16 del docx)**: se logró con un `FrameLayout`
  — el gauge grande de distancia llena todo el contenedor, y una fila horizontal
  (`LinearLayout`) con los gauges pequeños de temperatura/humedad se pega encima con
  `gravity = BOTTOM|CENTER_HORIZONTAL`, quedando visualmente montada sobre el borde inferior
  del gauge grande (efecto "cluster" de instrumentos, sin necesidad de `RelativeLayout` con
  márgenes negativos).
- **Tabla y gráfica solo muestran distancia vs. tiempo** (así lo pide el enunciado
  explícitamente) — temperatura y humedad son de solo lectura en vivo en su gauge, sin
  historial ni tabulación.
- **DHT11 solo admite ~1 lectura/segundo** (límite del sensor, no del código) — el período de
  muestreo se dejó en 1000 ms, más lento que el de la Cuarta (500 ms), por esa razón.
- Blink de verificación en GPIO 1 a **250 ms** (distinto de 1000 ms de la Tercera y 500 ms de
  la Cuarta), mismo patrón no bloqueante con `millis()` ya usado en ambas.
- **Compilación verificada** con `./gradlew assembleDebug` — se creó un PNG placeholder de
  2×2 en `res/drawable/dht11_hcsr04_android.png` solo para que compilara (el `R.drawable`
  necesita el archivo real presente); el usuario lo reemplaza por la imagen definitiva al
  final, junto con el ícono personalizado que pide el enunciado para la entrega.
- Pendiente para cuando se pruebe con hardware real: reemplazar `ssid`/`password`
  (placeholder) en el `.ino`, reemplazar el drawable placeholder, y confirmar en terreno que
  los brackets elegidos para temperatura (`10/20/30/40/50 °C`) y humedad (`20/40/60/80/100
  %HR`) dan una escala legible con las condiciones reales del salón.

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

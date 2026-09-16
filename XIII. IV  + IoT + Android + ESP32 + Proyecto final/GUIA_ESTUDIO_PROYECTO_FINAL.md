# Guía de estudio — Proyecto final (módulo 16: IV + IoT + ESP32)

Este documento es para que **repases y te autoevalúes** antes de presentar el proyecto final,
no es un registro técnico de cambios (para eso está el `CLAUDE.md` de esta carpeta). Cubre las
3 apps completas:

- **MiCuadragesimaTerceraApp**: Android controla un LED RGB en la ESP32-S3 (Android PUBLICA).
- **MiCuadragesimaCuartaApp**: la ESP32-S3 mide iluminancia con un BH1750 (ESP32 PUBLICA,
  Android SUBSCRIBE).
- **MiCuadragesimaQuintaApp** (la tarea): la ESP32-S3 mide distancia (HC-SR04) y
  temperatura/humedad (DHT11), corrige la velocidad del sonido con esos datos, y publica todo
  junto (ESP32 PUBLICA, Android SUBSCRIBE).

Si al leer una sección sientes que la explicarías sin dudar, tilda mentalmente el ítem del
checklist final. Si no, ese es tu tema para repasar antes de presentar.

---

## 1. Frecuencias y temporización

Esto es lo primero que un evaluador puede preguntar mirando el código: "¿por qué este número
y no otro?".

| App | Qué mide/publica | Período de muestreo | Por qué ese valor |
|---|---|---|---|
| 43 (LED RGB) | Publica el color actual | 200 ms (hilo Android) | Suficientemente rápido para que el deslizador se sienta "en vivo" sin saturar el broker |
| 44 (BH1750) | Publica iluminancia | 500 ms (ESP32) | El BH1750 es rápido, 500 ms es solo para no inundar de mensajes |
| 45 (DHT11+HC-SR04) | Publica distancia+temp+humedad | 1000 ms (ESP32) | **Límite real del DHT11**: su datasheet permite ~1 lectura por segundo; pedirle más rápido da lecturas repetidas o erráticas. El HC-SR04 podría ir más rápido, pero como van en el mismo JSON, manda el sensor más lento. |

**Del lado Android**, en las apps que se suscriben (44 y 45), `ActividadComoClienteSubMQTT`
no espera pasivamente los mensajes: sondea `cliente.leerString()` cada
`periodo_muestreo = 0.4 * periodo` (el `periodo` que trae el propio JSON). Es decir, sondea a
más del doble de la frecuencia con la que llegan los datos reales, para no perderse ninguno
por mal sincronismo entre los dos hilos. **Pregúntate**: ¿por qué 0.4 y no 1.0? (pista:
teorema de muestreo — hay que sondear *más rápido* que la señal que se quiere capturar, no a
la misma velocidad, o se pueden saltar mensajes).

**El "keep-alive" de MQTT** (`options.setKeepAliveInterval(60)` en Android): es un latido que
el cliente y el broker se mandan cada 60 s para confirmarse mutuamente que la conexión sigue
viva, aunque no haya datos nuevos que enviar. Si uno de los dos deja de responder al
keep-alive, el otro asume que la conexión se cayó.

**GPIO 1: blink de verificación, no bloqueante**. Las 3 apps tienen un LED en el pin 1 que
parpadea todo el tiempo, sin relación con los sensores — es solo para confirmar a simple
vista que el firmware sigue corriendo (y con qué período, para distinguir de un vistazo cuál
placa es cuál si hay varias corriendo a la vez: 43 = 1000 ms, 44 = 500 ms, 45 = 250 ms).

- **¿Por qué con `millis()` y no con `delay()`?** `delay()` congela por completo el
  microcontrolador durante ese tiempo — nada más se ejecuta. Si pusieras
  `delay(1000)` dentro de `loop()`, el ESP32 no podría llamar a `mqttCliente.loop()`
  (la función que procesa mensajes MQTT entrantes/salientes) durante ese segundo entero, así
  que los mensajes se atrasarían o se perderían. `millis()` en cambio solo *consulta* cuánto
  tiempo ha pasado desde el arranque de la placa (un contador que corre solo, en segundo
  plano) — comparándolo contra la última vez que hiciste algo, puedes decidir "¿ya pasó el
  tiempo?" sin bloquear nada más. El patrón en las 3 apps es siempre el mismo:
  ```cpp
  unsigned long ahora = millis();
  if (ahora - tiempoAnterior >= periodo) {
      tiempoAnterior = ahora;
      // hacer la tarea periódica
  }
  ```
- Esto mismo aplica al muestreo de sensores en la 45: si el bucle bloqueara con `delay()`
  para el blink Y para el muestreo, `mqttCliente.loop()` casi no correría, y verías demoras de
  varios segundos entre que pasa algo físicamente y que el celular lo muestra (esto realmente
  pasó y se corrigió en la 43 y la 44 durante el desarrollo — repásalo si quieres un ejemplo
  concreto de "antes/después").

---

## 2. Cómo funciona cada sensor/actuador

### LED RGB (43)
No es un sensor, es un actuador: 3 pines de un LED RGB manejados por PWM
(`analogWrite`/`ledcWrite`), uno por canal de color. El "color" que mandas desde Android es un
JSON `{r,g,b}` con valores 0-255 por canal; el ESP32 simplemente aplica esos 3 valores de
ancho de pulso a los 3 pines.

### BH1750 — luxómetro (44)
Sensor **digital** de luz por **I2C** (dos hilos: SDA=datos, SCL=reloj, más VCC/GND) — a
diferencia de un fotorresistor analógico, el chip ya hace la conversión a lux internamente y
te la entrega por el bus I2C con una librería (`BH1750.h`). En el ESP32/ESP32-S3, el
periférico I2C **no está atado a pines fijos** (usa una "matriz de GPIO" interna que enruta
la señal a cualquier pin libre), por eso se pudo reasignar de los pines 11/12 sugeridos por la
guía a 8/9, sin recablear nada — a diferencia de UART o pines analógicos, que sí suelen tener
restricciones de hardware.

### DHT11 — humedad y temperatura (45)
Combina un sensor capacitivo de humedad con un termistor, y un microcontrolador interno que
ya digitaliza ambas lecturas. Se comunica por **un solo pin de datos** (protocolo de tiempos:
el ESP32 "despierta" al sensor bajando la línea ~18 ms, el DHT11 responde y luego manda 40
bits codificados como duración de pulsos — un pulso corto es un `0`, uno largo es un `1`).
Por eso es lento (máximo ~1 lectura/s) y por qué se usa una librería (`DHT.h`) en vez de leer
los pulsos a mano. El módulo de 3 pines que usas ya trae la resistencia de pull-up soldada,
por eso solo necesitas VCC/GND/DATA.

**Pregúntate**: ¿por qué el `.ino` guarda el último valor válido de temperatura/humedad si
`isnan()` detecta una lectura fallida, en vez de simplemente publicar ese dato malo o no
publicar nada ese ciclo? (pista: sin esa protección, un solo fallo de lectura del DHT11
propagaría un `NaN` a la fórmula de velocidad del sonido, arruinando también la medida de
distancia de ese ciclo).

### HC-SR04 — distancia por ultrasonido (45, y también módulo XI)
Manda un pulso ultrasónico (disparado por el pin `trig`) y mide, por el pin `echo`, cuánto
tiempo tarda en volver el eco tras rebotar en un obstáculo (`pulseIn`, con un *timeout* de
30000 µs para no colgar el programa si no hay ningún obstáculo en rango). La distancia se
calcula como:

```
distancia (cm) = (duración del eco en µs × velocidad del sonido en m/s) / 2 × 0.0001
```

El `/2` es porque el sonido recorre la distancia dos veces (ida y vuelta); el `0.0001`
convierte unidades (µs→s y m→cm a la vez). Si `pulseIn` no detecta ningún eco dentro del
timeout, devuelve `0` — hay que tratar ese caso aparte (no es "distancia = 0 cm", es
"no se detectó ningún obstáculo").

**Corrección de la velocidad del sonido (el punto central de la tarea)**: la velocidad del
sonido en el aire *no es una constante* — depende de la temperatura (el aire más caliente
transmite el sonido más rápido) y, en menor medida, de la humedad (el vapor de agua es menos
denso que el aire seco, así que más humedad también acelera un poco el sonido). La fórmula
empírica usada:

```
v = 331.3 + 0.606·T + 0.0124·HR   (m/s, T en °C, HR en %)
```

Sin esta corrección (usando una constante fija como 340 m/s, que es la velocidad aproximada
del sonido a ~15°C), la distancia calculada tendría un error sistemático que crece mientras
más se aleje la temperatura/humedad real de esas condiciones de referencia.

---

## 3. MQTT: el patrón publicador-suscriptor

**MQTT vs. lo que hiciste antes (BLE, módulo XI)**: en BLE había una relación directa
cliente-servidor — el Android se conectaba *directamente* al ESP32 (que hacía de servidor
BLE). En MQTT ninguno de los dos se conecta al otro: **ambos son clientes** de un tercer
sistema, el **broker** (acá, uno compartido por el curso en `45.56.74.248:1883`). Un cliente
*publica* mensajes a un **tópico** (una especie de canal con nombre), y otro cliente
*suscrito* a ese mismo tópico los recibe — sin que ninguno de los dos sepa nada de la
dirección de red del otro. Esto es lo que permite que, en la 44 y la 45, sea el ESP32 el que
publica y el celular el que se suscribe (justo al revés que en BLE, donde el celular siempre
era el que iniciaba la conexión).

**Las dos implementaciones de MQTT que usaste no son la misma librería**:
- **Android**: `MqttAsyncClient` de la librería Eclipse Paho (fork `hannesa2`, porque el
  histórico `MqttAndroidClient` quedó obsoleto/inseguro desde Android 12 por depender de un
  `AlarmReceiver` que el sistema ya no acepta).
- **ESP32**: `PubSubClient` (Arduino).

Aunque ambas hablan el mismo protocolo MQTT, sus **APIs son distintas** — vale la pena que
sepas señalar al menos estas dos diferencias si te preguntan:
1. **La URL de conexión**: en Android, `MqttAsyncClient` recibe un solo string con el esquema
   incluido (`tcp://45.56.74.248:1883`), porque soporta varios protocolos (`tcp://`, `ssl://`,
   `ws://`...). En el ESP32, `PubSubClient.setServer(host, puerto)` recibe host y puerto como
   dos parámetros separados, sin esquema.
2. **Sincronía**: `MqttAsyncClient` es asíncrono (usa *callbacks* como `onSuccess`/
   `onFailure`), mientras que `PubSubClient.connect()` en el `.ino` es bloqueante dentro de su
   propia llamada.

**JSON como formato de mensaje**: en Android se usa `org.json` (built-in de Android); en el
ESP32 se usa `ArduinoJson` (`StaticJsonDocument`, `serializeJson`/`deserializeJson`). Ambos
lados deben coincidir *exactamente* en los nombres de las claves del JSON (por ejemplo,
`"distancia"`, `"temperatura"`, `"humedad"` en la 45) — si un lado usa un nombre y el otro
otro distinto, no hay error de compilación, simplemente el dato nunca llega (se lee `null` o
lanza una excepción capturada silenciosamente).

---

## 4. Conceptos básicos de la app en Java/Android

Todo el curso construye la GUI **en código Java, sin XML** — repasa que entiendes por qué se
hace así aquí (control total y explícito de cada `LinearLayout`/`RelativeLayout`/`FrameLayout`
y sus `LayoutParams`, sin la capa de indirección de un archivo de layout).

- **`Activity` y su ciclo de vida**: `onCreate()` se ejecuta una sola vez al crear la pantalla
  (ahí se arma la GUI); `onResume()` cada vez que la pantalla vuelve a primer plano;
  `onPause()`/`onDestroy()` al salir. Un detalle importante que viste en la 43: un objeto
  `Thread` **no muere automáticamente** cuando la `Activity` que lo creó se destruye — si su
  `run()` tiene un `while(true)` sin condición de salida y nadie lo detiene explícitamente en
  `onDestroy()`, sigue publicando datos en segundo plano para siempre (un "hilo zombie").
- **`Thread`/`Runnable` para tareas periódicas**: patrón usado en las 3 apps —
  `Thread hilo = new Thread(this)` (la propia `Activity` implementa `Runnable`), y su
  `run()` hace `while(true) { Thread.sleep(periodo); tareaPeriodica(); }`.
- **`Handler.post()`**: un hilo en segundo plano (como el de arriba) **no puede tocar la UI
  directamente** — Android lo prohíbe. Por eso, cuando llega un dato nuevo, el hilo llama a
  `myHandler.post(updateRunnable)`, que reprograma esa actualización para que se ejecute en el
  hilo principal (el único que puede tocar vistas).
- **Vistas personalizadas (`View`, `onDraw`, `invalidate()`)**: los gauges (`Gauge`,
  antes `GaugeSimple`) son clases que extienden `View` y sobreescriben `onDraw(Canvas canvas)`
  para dibujar a mano (círculos, arcos, texto) con un objeto `Paint`. Llamar a `invalidate()`
  al final de `onDraw()` le pide al sistema que vuelva a dibujar esa vista en el siguiente
  refresco de pantalla — así la aguja se redibuja constantemente reflejando el valor actual.
- **El bug de `package`/`import` de la 43** (vale la pena que lo tengas claro, no solo el
  parche): en Java, el `package` de una clase **no se infiere de la carpeta** donde vive el
  archivo — hay que declararlo explícitamente como primera línea de código. Sin esa
  declaración, el compilador trata el archivo como si estuviera en el paquete raíz, y
  cualquier `import` que intente traerlo desde su paquete "real" falla con
  `package ... does not exist` en vez de un `cannot find symbol` (que es el error más típico
  cuando de verdad falta un `import`).

---

## 5. Checklist de autoevaluación

Marca los que ya te sientes seguro explicando sin mirar el código:

- [ ] ¿Por qué el DHT11 se muestrea a 1000 ms y no más rápido?
- [ ] ¿Por qué `millis()` en vez de `delay()` en el `loop()` del ESP32, y qué se rompe si usas
      `delay()`?
- [ ] ¿Qué es un *broker* MQTT y en qué se diferencia del patrón cliente-servidor que usaste
      con BLE?
- [ ] ¿Por qué la URL del broker en Android lleva `tcp://` y en el ESP32 no?
- [ ] ¿Cómo funciona el HC-SR04 (qué mide `pulseIn`, por qué hay que dividir entre 2, qué
      significa que devuelva `0`)?
- [ ] ¿Por qué la velocidad del sonido depende de la temperatura y la humedad, y qué fórmula
      usaste?
- [ ] ¿Cómo se comunica el DHT11 por un solo pin de datos?
- [ ] ¿Por qué el I2C del BH1750 se pudo reasignar de pines sin recablear nada?
- [ ] ¿Qué hace `Handler.post()` y por qué hace falta si ya tienes un `Thread` corriendo?
- [ ] ¿Qué es un hilo "zombie" y por qué puede pasar en Android si no lo detienes en
      `onDestroy()`?
- [ ] ¿Cómo funciona el rango dinámico del `Gauge` (`cambiarEscala`) y por qué se generalizó a
      una sola clase con un arreglo de umbrales en vez de una subclase por sensor?
- [ ] ¿Por qué el `package` de una clase Java no se infiere de la carpeta donde está el
      archivo?

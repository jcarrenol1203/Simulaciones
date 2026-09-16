# Módulo XI — BLE Android + ESP32-S3: resumen de las 3 apps

Documento de estudio para el estudiante (no son instrucciones para el asistente — para
eso está `../CLAUDE.md`). Cubre, a grandes rasgos, qué hace cada app, qué hace cada
clase/archivo, qué protocolo se usó y qué se tuvo en cuenta al construirlas — tanto en
Android Studio (Java) como en el firmware de la ESP32-S3 (Arduino IDE).

## Protocolo de comunicación (común a las 3 apps)

Todas usan **BLE (Bluetooth Low Energy)** con patrón **cliente-servidor**: el celular
Android es el **cliente** (`BluetoothGatt`), la ESP32-S3 es el **servidor**
(`BLEServer`). No es Bluetooth clásico (el de módulo X, `BluetoothSocket`) — BLE funciona
distinto:

- El servidor expone un **servicio GATT** (identificado por un `SERVICE_UUID`), que a su
  vez expone una o más **características** (identificadas por su propio UUID), cada una
  con un tipo de acceso: `WRITE` (el cliente escribe, el servidor recibe), `NOTIFY` (el
  servidor empuja datos, el cliente se suscribe) o ambos.
- Para recibir `NOTIFY`, el cliente debe escribir el **descriptor 2902**
  (`BluetoothGattDescriptor`, UUID estándar `00002902-...`) — es la forma de "suscribirse".
- La conexión es **asíncrona**: no hay un flujo bloqueante como en un `Socket` clásico;
  todo pasa por callbacks (`onConnectionStateChange`, `onServicesDiscovered`,
  `onCharacteristicChanged`, etc.).
- Se pide un `requestMtu(128)` al conectar, para que los paquetes JSON quepan completos
  en un solo envío BLE (el límite por defecto es más pequeño).
- El escaneo para encontrar el ESP32 se hace con `BluetoothLeScanner`/`ScanCallback`
  (BLE real), no con el `ScannerBluetooth` de la guía (ese es Bluetooth clásico y no ve
  dispositivos que solo hacen *advertising* BLE). La actividad `ActividadEscaneoDispositivos`
  (igual en las 3 apps) muestra en una lista todos los BLE cercanos para elegir el propio
  por nombre/MAC — útil si hay varios ESP32 del curso prendidos a la vez.
- El manifest agrega `android:usesPermissionFlags="neverForLocation"` al permiso
  `BLUETOOTH_SCAN`: sin este flag, Android exige también Ubicación activada y
  `ACCESS_FINE_LOCATION` para entregar resultados de escaneo BLE, y si falta los descarta
  en silencio (parecía que el escaneo "no encontraba nada").

---

## 1. MiTrigesimaSeptimaApp — LED RGB controlado desde el celular

**Qué hace:** el celular controla en tiempo real el color de un LED RGB físico conectado
a la ESP32, moviendo 3 deslizadores (rojo/verde/azul). Es **unidireccional**: solo el
Android escribe hacia el ESP32; el ESP32 nunca manda datos de vuelta.

**Protocolo:** una sola característica `WRITE`. El Android arma un JSON `{"r":.., "g":..,
"b":..}` y lo escribe; el ESP32 lo recibe en un callback y mueve el LED con PWM.

### Clases (Android Studio)

| Clase | Qué hace |
|---|---|
| `ActividadPrincipalMiTrigesimaSeptimaApp` | Pantalla de inicio (botones ENTRAR/SALIR), pide permisos de Bluetooth, activa el Bluetooth del celular. |
| `ActividadEscaneoDispositivos` | Escanea BLE y muestra la lista de dispositivos cercanos para elegir el ESP32. |
| `ActividadComoClienteBluetooth` | Pantalla principal de control: 3 `SeekBar` (R/G/B), un `TableroColor` con la vista previa, botones BUSCAR/CONECTAR. Cada vez que se mueve un slider, arma el JSON de color y lo envía por `ClienteBluetooth`. |
| `ClienteBluetooth` | Envoltorio de `BluetoothGatt`: abre/cierra la conexión, descubre el servicio, escribe bytes en la característica. |
| `TableroColor` | `View` personalizada que dibuja un rectángulo con el color RGB resultante más 3 barras con los valores numéricos de R, G y B — solo visual, no toca BLE. |
| `Boton` | `ImageView` reutilizable con imagen + texto, usada por los botones ENTRAR/SALIR (se repite igual en las 3 apps). |
| `AlmacenDatosRAM` | "Base de datos" en memoria estática: dirección MAC del ESP32, estado de conexión, tamaño de letra según resolución, etc. — compartida entre actividades. |

### Firmware ESP32 (Arduino IDE)

Archivo: `esp32-firmware/led_rgb_bluetooth_android_esp32/led_rgb_bluetooth_android_esp32.ino`

- Pines: LED RGB en **GPIO 13 (rojo) / 12 (verde) / 11 (azul)**, vía `ledcAttach`/
  `ledcWrite` (PWM). LED de verificación (blink) en **GPIO 1**, parpadea cada
  **1000 ms** todo el tiempo (independiente de si hay conexión) — es una adición nuestra
  para confirmar a simple vista que el programa está corriendo, no viene en la guía.
- `setup()`: inicializa los pines PWM, crea el servidor BLE, el servicio, la
  característica `WRITE`, y arranca el *advertising* con el nombre `PhysicsESP32_BLE`.
- `loop()`: solo actualiza el blink de verificación (no bloqueante, con `millis()`); todo
  lo demás ocurre por evento (`onWrite` del callback de la característica), no por
  sondeo.
- `MyServerCallbacks` (`onConnect`/`onDisconnect`): reinicia el *advertising* cuando el
  celular se desconecta — sin esto, el ESP32 deja de ser visible hasta reiniciar la
  placa manualmente (bug conocido de `BLEServer` en `arduino-esp32`).

### Qué se tuvo en cuenta

- Se dejó **intencionalmente** un bug de la guía original en
  `onRequestPermissionsResult` (usa `||` en vez de `&&`, por lo que en la práctica
  siempre "aprueba" los permisos aunque el usuario los niegue) — el estudiante pidió
  explícitamente no corregirlo, para que el código coincida con el material del curso.
- El resto del código Android sí se corrigió frente al primer intento fallido: el
  escaneo original (`ScannerBluetooth`, Bluetooth clásico) nunca encontraba al ESP32
  porque este solo anuncia por BLE.

---

## 2. MiTrigesimaOctavaApp — Luxómetro (sensor de luz BH1750)

**Qué hace:** el ESP32 mide la iluminancia con un sensor GY-30/BH1750 (I2C) y se la
manda al celular por BLE; el celular la muestra en un gauge tipo velocímetro, una tabla
y una gráfica en tiempo real. Es **unidireccional**: solo el ESP32 manda datos; el
Android solo lee.

**Protocolo:** una sola característica `NOTIFY` + descriptor 2902. El ESP32 arma un JSON
`{"unidad":"lux","periodo":..,"tiempo":..,"valor":..}` cada 500&nbsp;ms y lo notifica; el
Android lo recibe en `onCharacteristicChanged`.

### Clases (Android Studio)

| Clase | Qué hace |
|---|---|
| `ActividadPrincipalMiTrigesimaOctavaApp` | Pantalla de inicio (ENTRAR/SALIR/AJUSTAR), permisos y Bluetooth. |
| `ActividadEscaneoDispositivos` | Igual que en Séptima. |
| `ActividadConfiguracion` | Pantalla para que el estudiante configure `AlmacenDatosRAM.nDatos` (cuántas muestras guardar/graficar antes de detenerse). |
| `ActividadComoClienteBluetooth` | Pantalla principal: gauge (`Luxometro`), `TablaSimple`, `Graficador`, botones BUSCAR/CONECTAR/GRAFICA. Recibe cada JSON, lo convierte y actualiza gauge/tabla/gráfica. |
| `ClienteBluetooth` | Como en Séptima, pero configurado para **leer** (se suscribe a `NOTIFY`) en vez de escribir. |
| `Luxometro` | Extiende `GaugeSimple`; agrega `cambiarEscala()` para que el rango del gauge se ajuste automáticamente según la magnitud de lux medida. |
| `GaugeSimple` | Instrumento tipo velocímetro dibujado a mano (`onDraw`, `Canvas`, `drawArc`) — aguja, escala, zonas de color, texto de marca. Reutilizado (idéntico) del módulo VIII. |
| `Graficador` | Envoltorio de `MPAndroidChart` (`LineChart`) para graficar los datos en el tiempo. |
| `TablaSimple` | Tabla simple de texto con las últimas muestras (tiempo, valor). |
| `Boton`, `AlmacenDatosRAM` | Igual que en Séptima. |

### Firmware ESP32 (Arduino IDE)

Archivo: `esp32-firmware/luxometro_bluetooth_esp32/luxometro_bluetooth_esp32.ino`

- Sensor BH1750 por **I2C**, reasignado a **GPIO 8 (SDA) / GPIO 9 (SCL)** en vez de los
  11/12 de la guía, porque esos pines ya los usa el LED RGB de la app anterior en la
  misma placa física (el ESP32-S3 no ata el I2C a pines fijos — GPIO matrix — así que se
  puede reasignar sin recablear). LED de verificación en GPIO 1, blink cada **500 ms**.
- `setup()`: inicializa I2C y el sensor (`lightMeter.begin()`), crea el servidor BLE, el
  servicio y la característica `NOTIFY` + descriptor 2902, arranca el *advertising*.
- `loop()`: si hay celular conectado, lee el sensor y notifica el JSON cada 500&nbsp;ms;
  además actualiza el blink de verificación de forma no bloqueante (`millis()`), y
  reinicia el *advertising* tras una desconexión.

### Qué se tuvo en cuenta

- El blink se implementó con `millis()` (no `delay()`): un `delay()` extra en `loop()`
  sumaría tiempo real no reflejado en el contador `tiempo` que viaja en el JSON,
  desincronizando el eje de tiempo de la gráfica.
- El `GaugeSimple` de la guía (código literal del `.docx`) no se veía igual a la
  referencia visual (números rotados, sin marco, sin texto de marca) — se reemplazó por
  la versión ya usada en el módulo VIII, que sí coincide y es API-compatible.
- La librería `MPAndroidChart` (usada por `Graficador`) no está en Maven Central, se
  agregó el repositorio **JitPack** en `settings.gradle.kts`.
- Se recuperaron `configuracion.png`/`luxometro_android.png` de una captura dentro del
  `.docx` de la guía (son de baja resolución; reemplazar por los originales del profesor
  si están disponibles).

---

## 3. MiTrigesimaNovenaApp — Distanciómetro HC-SR04 + control de LED RGB

**Qué hace:** el ESP32 mide distancia a un obstáculo con un sensor ultrasónico HC-SR04 y
se la manda al celular (gauge/tabla/gráfica, igual que Octava); **además**, según el
rango de distancia, el celular calcula un color y se lo manda de vuelta al ESP32 para
encender el mismo LED RGB de la primera app. Es la única de las 3 **bidireccional**.

**Protocolo:** dos características — una `NOTIFY` (ESP32→Android, distancia) y una
`WRITE` (Android→ESP32, color), sobre el mismo servicio. El Android se suscribe a la
primera y escribe en la segunda cada vez que recibe un dato nuevo.

### Clases (Android Studio)

| Clase | Qué hace |
|---|---|
| `ActividadPrincipalMiTrigesimaNovenaApp` | Igual patrón que Octava (ENTRAR/SALIR/AJUSTAR). |
| `ActividadEscaneoDispositivos`, `ActividadConfiguracion`, `Boton`, `GaugeSimple`, `Graficador`, `TablaSimple`, `AlmacenDatosRAM` | Mismo código que Octava (reutilizadas sin cambios). |
| `Distanciometro` | Igual idea que `Luxometro`, pero con una escala de reescalado en pasos (0–20, 0–50, 0–100, 0–200, 0–300, 0–600 cm) según la magnitud medida. |
| `ClienteBluetooth` | Reescrita para manejar **dos** características a la vez: `caracteristicaLectura` (TX, se suscribe con el descriptor 2902) y `caracteristicaEscritura` (RX, método nuevo `escribirString()`). |
| `ActividadComoClienteBluetooth` | Al recibir cada distancia, además de actualizar gauge/tabla/gráfica, calcula el color (`elegirColor()`) y lo envía de vuelta (`escribirColorHaciaESP32()`). |

**Lógica de color** (`elegirColor()`, en `ActividadComoClienteBluetooth`):

| Condición | Color |
|---|---|
| `valor == 0` (el HC-SR04 no detectó ningún eco — nada en rango) | 🔵 Azul |
| distancia ≤ 20 cm | 🔴 Rojo (cerca) |
| 20 cm < distancia ≤ 100 cm | 🟢 Verde (medio) |
| distancia > 100 cm | 🔵 Azul (lejos) |

El caso `valor==0` se agregó después de probar con hardware real: sin él, "no hay
obstáculo" y "objeto pegado al sensor" producían el mismo color (rojo), porque ambos
caen en `distancia ≤ 20`.

### Firmware ESP32 (Arduino IDE)

Archivo: `esp32-firmware/distanciometro_bluetooth_esp32/distanciometro_bluetooth_esp32.ino`

- HC-SR04 en **GPIO 15 (TRIG) / GPIO 16 (ECHO)**, LED RGB en **GPIO 13/12/11** (mismo
  cableado físico que Séptima), LED de verificación en GPIO 1 con blink cada **250 ms**.
- `medirDistancia()`: dispara un pulso de 10&micro;s por TRIG y mide el tiempo de eco por
  ECHO (`pulseIn`, con timeout de 30000&micro;s para no congelar el ESP32 si no hay
  obstáculo); convierte el tiempo a centímetros con la velocidad del sonido. Si el
  timeout se cumple, `valor = 0`.
- `MyCallbacks` (`onWrite` de la característica RX): recibe el JSON `{"r":..,"g":..,"b":..}`
  desde el celular y mueve el LED con `ledcWrite`.
- `loop()`: si hay celular conectado, mide distancia y notifica el JSON cada
  100&nbsp;ms; actualiza el blink de forma no bloqueante; reinicia el *advertising*
  tras una desconexión.

### Qué se tuvo en cuenta

- Esta app **no es** otro luxómetro — se armó así por error en un primer intento
  (copiando Octava) y se reconstruyó comparando contra el código literal de
  `modulo_14.docx`, que sí especifica HC-SR04 + control de RGB bidireccional para la
  tercera app.
- El cableado (TRIG=15, ECHO=16, RGB=11/12/13) se confirmó con el estudiante antes de
  escribir el firmware, ya que en Octava hubo que reasignar pines por conflicto físico
  con el RGB de Séptima.
- Se recuperó `distanciometro_android.png` (fondo de pantalla de esta app), que ya
  existía sin usarse en el drawable de Octava.
- La lógica de color se corrigió tras una prueba real con el sensor despejado (ver tabla
  arriba: caso `valor==0`).

---

## Resumen comparativo

| | Séptima | Octava | Novena |
|---|---|---|---|
| Sensor/actuador ESP32 | LED RGB | BH1750 (luz) | HC-SR04 (distancia) + LED RGB |
| Dirección BLE | Android→ESP32 (WRITE) | ESP32→Android (NOTIFY) | Ambas (NOTIFY + WRITE) |
| Pines sensor/actuador | RGB: 13/12/11 | I2C: SDA 8 / SCL 9 | TRIG 15 / ECHO 16, RGB: 13/12/11 |
| Visualización Android | `TableroColor` (vista previa RGB) | Gauge + tabla + gráfica | Gauge + tabla + gráfica |
| Blink de verificación (GPIO 1) | 1000 ms | 500 ms | 250 ms |
| Nombre BLE del ESP32 | `PhysicsESP32_BLE` | `PhysicsESP32_BLE` | `PhysicsESP32_Dist` |

# CLAUDE.md

> Reglas generales del curso y seguimiento de teoría (dominada/pendiente): ver
> `../CLAUDE.md` en la raíz de `Simulaciones/`. Este archivo solo cubre el resumen técnico
> de lo que se tuvo en cuenta al construir las 3 apps de este módulo (módulo 14, BLE
> Android↔ESP32-S3), a grandes rasgos — no línea por línea.

## Arquitectura común a las 3 apps

Patrón cliente-servidor por **BLE**: el Android es cliente (`BluetoothGatt`/
`BluetoothGattCallback`), la ESP32-S3 es servidor (`BLEDevice`/`BLEServer`/`BLECharacteristic`,
Arduino IDE). Un servicio GATT (mismo `SERVICE_UUID` en las 3 apps) expone una o dos
características identificadas por UUID propios. Decisiones que se repiten en las 3:

- **Escaneo BLE con lista visual, no `ScannerBluetooth` de la guía**: la librería
  `comunicaciones.aar` del curso escanea con Bluetooth clásico (`BluetoothAdapter.
  startDiscovery`), pero un ESP32 que solo hace *advertising* BLE es invisible para ese
  escaneo. Se reemplazó `ActividadEscaneoDispositivos` por un escaneo BLE real
  (`BluetoothLeScanner`/`ScanCallback`) que muestra en un `ListView` todos los dispositivos
  BLE cercanos (sin filtrar por UUID) para que el estudiante elija el suyo por nombre/MAC —
  útil en salón de clase con varios ESP32 corriendo el mismo firmware/UUID a la vez.
- **`android:usesPermissionFlags="neverForLocation"`** en `BLUETOOTH_SCAN` (manifest): sin
  este flag, Android exige además `ACCESS_FINE_LOCATION` y Ubicación activada para entregar
  resultados de escaneo, y los descarta en silencio si falta — causaba que el escaneo
  "corriera" pero nunca devolviera ningún dispositivo.
- **Convención de sketch de Arduino IDE**: la carpeta del `.ino` debe llamarse exactamente
  igual que el archivo (sin doble extensión), o el IDE no lo reconoce como sketch válido.
- **Blink de verificación no bloqueante en GPIO 1**: un LED que parpadea desde el arranque,
  independiente de si el celular ya conectó, para confirmar a simple vista que el firmware
  está corriendo. Se implementó con `millis()` en vez de `delay()` porque un `delay()` extra
  dentro de `loop()` desincroniza el contador `tiempo`/`periodo` que viaja en el JSON (el eje
  de tiempo de la gráfica en el celular). Con las 3 placas del salón corriendo a la vez, cada
  app usa un período distinto para identificar cuál firmware es cuál con solo mirar el LED:
  **Séptima = 1000 ms, Octava = 500 ms, Novena = 250 ms** (adición nuestra, no está en la
  guía).

## `MiTrigesimaSeptimaApp/` — LED RGB, Android controla al ESP32 (escritura)

La app original de la guía: el celular escribe un JSON de color a una característica
`WRITE`, el ESP32 lo aplica al LED RGB (GPIO 11/12/13 vía `ledcAttach`/`ledcWrite`). Se
mantiene el código tal cual la guía (incluido un bug conocido y dejado a propósito: la
condición `||` en `onRequestPermissionsResult` que en la práctica siempre "aprueba" los
permisos, sin importar si el usuario los negó — el estudiante pidió explícitamente no
corregirlo para que coincida con el material del curso). Se agregó `BLEServerCallbacks`
(`onConnect`/`onDisconnect`) para reiniciar el *advertising* tras una desconexión — sin esto,
el ESP32 deja de ser descubrible hasta reiniciar la placa manualmente (bug real de
`BLEServer` en `arduino-esp32`, no cubierto por la guía).

## `MiTrigesimaOctavaApp/` — luxómetro BH1750, ESP32 notifica al Android (lectura)

Sensor de iluminancia GY-30/BH1750 por I2C; el ESP32 notifica el valor (`NOTIFY` +
`BLE2902`), el celular solo lee y grafica (gauge, tabla, `MPAndroidChart`). Decisiones:

- **I2C reasignado a GPIO 8 (SDA) / GPIO 9 (SCL)**: la guía usa `Wire.begin(11, 12)`, pero
  esos pines ya están ocupados por el LED RGB de Séptima en la misma placa física. El
  ESP32/ESP32-S3 no ata el periférico I2C a pines fijos (GPIO matrix), así que se reasignó
  sin necesidad de recablear nada — 8/9 son además los pines por defecto de la placa
  `esp32-s3-devkitc-1` según su `pins_arduino.h`.
- **MPAndroidChart vía JitPack**: la librería de gráficas no está en Maven Central/Google,
  hubo que agregar el repositorio JitPack en `settings.gradle.kts`.
- **`GaugeSimple` reemplazado por la versión del módulo VIII**: el código literal de la guía
  para esta clase no coincidía visualmente con la referencia (números rotados, sin marco,
  sin el texto "IoT.PhysicsSensor"); se encontró que los `GaugeSimple.java` de las 3 apps del
  módulo VIII son idénticos entre sí y sí igualan la referencia — se usó esa versión
  (API-compatible, mismos métodos públicos).
- Se recuperaron `configuracion.png`/`luxometro_android.png` extrayéndolos de una captura
  compuesta dentro de `modulo_14.docx` (son de baja resolución; reemplazar por los
  originales del profesor si están disponibles).

## `MiTrigesimaNovenaApp/` — distanciómetro HC-SR04 + LED RGB, bidireccional

La tercera app de la guía **no es otro luxómetro** (se armó así por error en un primer
intento y se reconstruyó): es un sensor ultrasónico HC-SR04 (`trigPin=15`/`echoPin=16`,
medición por `pulseIn`) que además controla el mismo LED RGB de Séptima (GPIO 11/12/13) según
rangos de distancia — la única de las 3 con comunicación **bidireccional**: una
característica TX (`NOTIFY`, UUID propio) para la distancia y una RX (`WRITE`, mismo UUID que
usaba Séptima) para el color. Decisiones:

- Clase nueva `Distanciometro extends GaugeSimple` con `cambiarEscala()` que reajusta el
  rango del gauge según la magnitud medida (más legible que un rango fijo 0–20cm cuando el
  objeto está a 300cm).
- `ClienteBluetooth` reescrito para manejar dos características (lectura + escritura) en vez
  de una sola.
- **Lógica de color corregida tras probar con hardware real**: el HC-SR04 manda `valor=0`
  cuando no detecta ningún eco dentro del timeout (nada en rango, o un fallo momentáneo de
  eco por ángulo/superficie) — indistinguible de "objeto a 0cm" si solo se mira el rango de
  distancia. Esto causaba rojos falsos con el sensor despejado. Se agregó un caso explícito:
  `valor==0` → azul (sin eco), antes de evaluar los rangos de distancia real (≤20cm rojo,
  >20cm verde).
- Se recuperó `distanciometro_android.png`, que ya existía (sin usarse) en el drawable de
  Octava — es la imagen de fondo pensada para esta app, no una imagen "sobrante".

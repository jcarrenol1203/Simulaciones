//para comunicación WiFi
#include <WiFi.h>
//para el protocolo MQTT de IoT
#include <PubSubClient.h>
//para manejar JSON
#include <ArduinoJson.h>
//para el sensor de humedad/temperatura DHT11
#include <DHT.h>

// --- Sensor DHT11 (humedad relativa) ---
#define DHTPIN 42
#define DHTTYPE DHT11
DHT dht(DHTPIN, DHTTYPE);

// --- Sensor LM35 (temperatura, analógico: 10 mV/°C) ---
const int lm35Pin = 4; // ADC1 (seguro con WiFi activo, a diferencia de ADC2 en GPIO 11-20)

// último valor válido conocido (por si una lectura puntual del DHT11 falla)
float temperatura = 20.0;
float humedad = 50.0;

// --- Sensor ultrasónico HC-SR04 (distancia) ---
const int trigPin = 15;
const int echoPin = 16;
float distancia = 0;

// --- LED RGB indicador de distancia (mismos pines de MiTrigesimaSeptimaApp
// y de led_rgb_mqtt_esp32.ino: GPIO 11/12/13) ---
const int pinAzul = 11;
const int pinVerde = 12;
const int pinRojo = 13;

int tiempo = 0; //en ms
int periodo = 1000; //en ms - el DHT11 solo admite ~1 lectura por segundo
unsigned long tiempoAnteriorMuestreo = 0;

// LED de verificación (adicional, no está en la guía): parpadea siempre,
// esté o no conectado el broker, para confirmar a simple vista que el
// firmware sigue corriendo. 250 ms para distinguirla de MiCuadragesimaTerceraApp
// (1000 ms) y MiCuadragesimaCuartaApp (500 ms).
int pinVerificacion = 1;
unsigned long periodoParpadeoMs = 250;
unsigned long tiempoAnteriorParpadeo = 0;
bool estadoParpadeo = false;

// --- Variables para conexiones WiFi ---
const char* ssid = "Comunidad_UNMED";      //reemplazar SSID
const char* password = "wifi_med_213"; //reemplazar password

// --- Datos del Broker MQTT ---
const char* mqtt_server = "45.56.74.248";
const int mqttPort = 1883;
const char* mqtt_usuario = "fisica";
const char* mqtt_clave = "iotfisica";
const char* topico = "s1027";

WiFiClient espCliente;
PubSubClient mqttCliente(espCliente);

void setup() {
  Serial.begin(115200);

  // LED de verificación
  pinMode(pinVerificacion, OUTPUT);
  digitalWrite(pinVerificacion, LOW);

  // Sensor ultrasónico
  pinMode(trigPin, OUTPUT);
  pinMode(echoPin, INPUT);

  // LED RGB indicador de distancia
  pinMode(pinRojo, OUTPUT);
  pinMode(pinVerde, OUTPUT);
  pinMode(pinAzul, OUTPUT);
  establecerColor(0, 0, 0);

  // Sensor DHT11
  dht.begin();

  // Conectar a WiFi
  conectarToWiFi();

  // Configurar MQTT
  setupMQTT();
}

// Administrar conexión wiFi
void conectarToWiFi() {
  delay(10);
  Serial.println();
  Serial.print("Conectando a...");
  Serial.println(ssid);

  WiFi.begin(ssid, password);

  while (WiFi.status() != WL_CONNECTED) {
    delay(500);
    Serial.print(".");
  }

  Serial.println("");
  Serial.println("WiFi conectado");
  Serial.print("IP address: ");
  Serial.println(WiFi.localIP());
}

// Administrar configuración de conexión al Broker MQTT
void setupMQTT() {
  mqttCliente.setServer(mqtt_server, mqttPort);
  mqttCliente.setCallback(callback);
}

// Conectar el cliente ESP32 MQTT al Broker
void reconnect() {
  Serial.println("Conectando a Broker MQTT...");

  while (!mqttCliente.connected()) {
    Serial.println("Reconectando al Broker MQTT...");

    String clientId = "ESP32Client-";
    clientId += String(random(0xffff), HEX);

    if (mqttCliente.connect(clientId.c_str(), mqtt_usuario, mqtt_clave)) {
      Serial.println("¡Conectado!");
      mqttCliente.subscribe(topico);
    } else {
      Serial.print("Falló, rc=");
      Serial.print(mqttCliente.state());
      Serial.println(" Intentando de nuevo en 5 segundos...");
      delay(5000);
    }
  }
}

void callback(char* topic, byte* payload, unsigned int length) {
  Serial.print("Callback - ");
  Serial.print("Message:");
  for (int i = 0; i < length; i++) {
    Serial.print((char)payload[i]);
  }
  Serial.println();
}

// Leer humedad del DHT11: si falla la lectura puntual (isnan), se conserva
// el último valor válido conocido en vez de publicar NaN.
void leerHumedad() {
  float h = dht.readHumidity();

  if (!isnan(h)) {
    humedad = h;
  } else {
    Serial.println("Error leyendo humedad del DHT11, se reutiliza el último valor válido");
  }
}

// Leer temperatura del LM35: sensor analógico de 10 mV/°C. El ADC del
// ESP32-S3 es de 12 bits (0-4095) y con la atenuación por defecto (11 dB)
// cubre hasta ~3.3V, más que suficiente para el rango de temperatura de
// laboratorio (0-100°C -> 0-1V).
void leerLM35() {
  int lectura = analogRead(lm35Pin);
  float voltaje = lectura * 3.3 / 4095.0;
  temperatura = voltaje * 100.0;
}

// Distancia con el HC-SR04, corrigiendo la velocidad del sonido según la
// temperatura y la humedad relativa medidas por el DHT11.
void medirDistancia() {
  // Corrección de la velocidad del sonido en el aire (fórmula empírica):
  // v = 331.3 + 0.606*T + 0.0124*HR (m/s), T en °C, HR en %.
  float velocidad_sonido = 331.3 + 0.606 * temperatura + 0.0124 * humedad;

  // Un timeout aislado (pulseIn -> 0) no siempre significa "sin obstáculo":
  // puede ser ruido eléctrico puntual en el pin Echo. Se reintenta hasta 2
  // veces más antes de aceptar el 0, dejando ~60 ms entre disparos para que
  // el eco del pulso anterior se disipe (recomendado por el datasheet del
  // HC-SR04).
  float duracion = 0;
  for (int intento = 0; intento < 3 && duracion == 0; intento++) {
    if (intento > 0) {
      delay(60);
    }

    digitalWrite(trigPin, LOW);
    delayMicroseconds(2);
    digitalWrite(trigPin, HIGH);
    delayMicroseconds(10);
    digitalWrite(trigPin, LOW);

    // Timeout de 30000 microsegundos para evitar que el ESP32 se congele
    // si no hay obstáculo (pulseIn nunca ve el flanco de bajada).
    duracion = pulseIn(echoPin, HIGH, 30000);
  }

  if (duracion == 0) {
    distancia = 0;
  } else {
    distancia = ((duracion * velocidad_sonido) / 2) * 0.0001;
  }
}

// Enciende el LED RGB con el color dado (0-255 por canal).
void establecerColor(int rojo, int verde, int azul) {
  analogWrite(pinRojo, rojo);
  analogWrite(pinVerde, verde);
  analogWrite(pinAzul, azul);
}

// Colorea el LED según el rango de la última distancia medida:
// rojo = sin eco (0) o distancia grande (>=50cm), azul = mediana
// (20-50cm), verde = corta y distinta de 0 (<20cm).
void actualizarColorRGB() {
  if (distancia == 0 || distancia >= 50) {
    establecerColor(255, 0, 0);
  } else if (distancia >= 20) {
    establecerColor(0, 0, 255);
  } else {
    establecerColor(0, 255, 0);
  }
}

void loop() {
  // Blink no bloqueante del LED de verificación (pin 1)
  unsigned long ahora = millis();
  if (ahora - tiempoAnteriorParpadeo >= periodoParpadeoMs) {
    tiempoAnteriorParpadeo = ahora;
    estadoParpadeo = !estadoParpadeo;
    digitalWrite(pinVerificacion, estadoParpadeo ? HIGH : LOW);
  }

  if (!mqttCliente.connected()) {
    reconnect();
  }
  mqttCliente.loop();

  // Muestreo no bloqueante cada "periodo" ms (con millis(), sin delay()),
  // para que mqttCliente.loop() se siga llamando en cada vuelta del loop().
  if (ahora - tiempoAnteriorMuestreo >= (unsigned long) periodo) {
    tiempoAnteriorMuestreo = ahora;

    leerHumedad();
    leerLM35();
    medirDistancia();
    actualizarColorRGB();
    SerializeObject();

    tiempo = tiempo + periodo;
  }
}

void SerializeObject() {
  StaticJsonDocument<300> doc;

  doc["periodo"] = periodo;
  doc["tiempo"] = tiempo;
  doc["distancia"] = distancia;
  doc["temperatura"] = temperatura;
  doc["humedad"] = humedad;

  char buffer[200];
  serializeJson(doc, buffer);

  Serial.println("Enviando mensaje a MQTT tópico...");
  Serial.println(buffer);

  if (mqttCliente.publish(topico, buffer) == true) {
    Serial.println("Envío de mensaje exitoso");
  } else {
    Serial.println("Error en el envío del mensaje");
  }

  Serial.println("-------------");
}

//para comunicación WiFi
#include <WiFi.h>
//para el protocolo MQTT de IoT
#include <PubSubClient.h>
//para comunicación del sensor
#include <Wire.h>
#include <BH1750.h>
//para manejar JSON
#include <ArduinoJson.h>

BH1750 lightMeter(0x23);

StaticJsonDocument<300> doc; //300 bytes

uint16_t valor;
int tiempo = 0; //en ms
int periodo = 500; //en ms
int minimo = 0;
int maximo = 100;
unsigned long tiempoAnteriorMuestreo = 0;

// LED de verificación (adicional, no está en la guía): parpadea siempre,
// esté o no conectado el broker, para confirmar a simple vista que el
// firmware sigue corriendo. 500 ms para distinguirla de MiCuadragesimaTerceraApp (1000 ms).
int pinVerificacion = 1;
unsigned long periodoParpadeoMs = 500;
unsigned long tiempoAnteriorParpadeo = 0;
bool estadoParpadeo = false;

// --- Variables para conexiones WiFi ---
const char* ssid = "Londonos_2.4";      //reemplazar SSID
const char* password = "Juansimon2"; //reemplazar password

// --- Datos del Broker MQTT ---
const char* mqtt_server = "45.56.74.248";
const int mqttPort = 1883;
const char* mqtt_usuario = "fisica";
const char* mqtt_clave = "iotfisica";
const char* topico = "s2";

WiFiClient espCliente;
PubSubClient mqttCliente(espCliente);

void setup() {
  Serial.begin(115200);

  // LED de verificación
  pinMode(pinVerificacion, OUTPUT);
  digitalWrite(pinVerificacion, LOW);

  // SDA = 8, SCL = 9 (reasignado desde 11/12 de la guía, para dejar 11/12/13 libres)
  Wire.begin(8, 9);

  if (lightMeter.begin()) {
    Serial.println(F("BH1750 inicializado"));
  } else {
    Serial.println(F("Error inicializando BH1750"));
  }
  Serial.println(F("BH1750 Test begin"));

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

    // ¡LA SOLUCIÓN AL ERROR 5! Pasamos usuario y contraseña
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

    // Leer luxometro
    valor = lightMeter.readLightLevel();

    // Serializar datos JSON para enviarlos
    SerializeObject();

    tiempo = tiempo + periodo;
  }
}

void SerializeObject() {
  doc.clear(); // ¡VITAL! Limpia la memoria antes de armar el nuevo JSON

  doc["maximo"] = maximo;
  doc["minimo"] = minimo;
  doc["unidad"] = "lx";
  doc["periodo"] = periodo;
  doc["tiempo"] = tiempo;
  doc["valor"] = valor;

  char buffer[200];
  serializeJson(doc, buffer); // Cambiado a serializeJson normal para ahorrar ancho de banda

  Serial.println("Enviando mensaje a MQTT tópico...");
  Serial.println(buffer);

  if (mqttCliente.publish(topico, buffer) == true) {
    Serial.println("Envío de mensaje exitoso");
  } else {
    Serial.println("Error en el envío del mensaje");
  }

  Serial.println("-------------");
}

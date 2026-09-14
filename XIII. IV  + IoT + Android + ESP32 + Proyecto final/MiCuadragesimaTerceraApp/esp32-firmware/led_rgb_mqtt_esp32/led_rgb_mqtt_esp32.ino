#include <WiFi.h>
#include <PubSubClient.h>
#include <ArduinoJson.h>

// --- 1. Definición de pines (Basado en tu hardware) ---
const int pinAzul = 11;
const int pinVerde = 12;
const int pinRojo = 13;

int pinVerificacion = 1;
unsigned long periodoParpadeoMs = 1000;
unsigned long tiempoAnteriorParpadeo = 0;
bool estadoParpadeo = false;

// --- 2. Credenciales de Red y Servidor ---
const char* ssid = "Londonos_2.4";
const char* password = "Juansimon2";

const char* mqtt_server = "45.56.74.248";
const int mqtt_port = 1883; 
const char* mqtt_usuario = "fisica";
const char* mqtt_clave = "iotfisica";
const char* topico_suscripcion = "s1";

WiFiClient espClient;
PubSubClient mqttCliente(espClient);

// --- 3. Función de Color ---
void establecerColor(int rojo, int verde, int azul) {
  // Ajusta según cómo configuraste el PWM (ledcWrite o analogWrite)
  // Aquí uso analogWrite que es el estándar actual del ESP32
  analogWrite(pinRojo, rojo);
  analogWrite(pinVerde, verde);
  analogWrite(pinAzul, azul);
}

// --- 4. Conexión WiFi ---
void setup_wifi() {
  delay(10);
  Serial.println();
  Serial.print("Conectando a ");
  Serial.println(ssid);

  WiFi.begin(ssid, password);

  while (WiFi.status() != WL_CONNECTED) {
    delay(500);
    Serial.print(".");
  }

  Serial.println("\nWiFi conectado. IP: ");
  Serial.println(WiFi.localIP());
}

// --- 5. Recepción de Mensajes MQTT (El Cerebro) ---
void callback(char* topic, byte* payload, unsigned int length) {
  Serial.print("Mensaje recibido en [");
  Serial.print(topic);
  Serial.print("]: ");
  
  String mensaje = "";
  for (int i = 0; i < length; i++) {
    mensaje += (char)payload[i];
  }
  Serial.println(mensaje);

  // Deserializamos el JSON que nos envía la App 43
  StaticJsonDocument<200> doc;
  DeserializationError error = deserializeJson(doc, mensaje);

  if (error) {
    Serial.print("Error leyendo JSON: ");
    Serial.println(error.c_str());
    return;
  }

  // Extraemos los valores. (Asegúrate de que estas claves coincidan con tu código Java)
  int r = doc["r"]; 
  int g = doc["g"]; 
  int b = doc["b"]; 

  // Cambiamos el color sin interrupciones
  establecerColor(r, g, b);
}

// --- 6. Conexión MQTT (¡Aquí está la magia del Error 5!) ---
void reconnect() {
  while (!mqttCliente.connected()) {
    Serial.print("Intentando conexión MQTT...");
    String clientId = "ESP32Client-";
    clientId += String(random(0, 0xffff), HEX);

    // Conectamos PASANDO EL USUARIO Y CONTRASEÑA
    if (mqttCliente.connect(clientId.c_str(), mqtt_usuario, mqtt_clave)) {
      Serial.println("¡Conectado exitosamente!");
      
      // Nos suscribimos al tópico
      mqttCliente.subscribe(topico_suscripcion);
    } else {
      Serial.print("Falló, rc=");
      Serial.print(mqttCliente.state());
      Serial.println(" Intentando de nuevo en 5 segundos...");
      delay(5000);
    }
  }
}

// --- 7. Inicialización ---
void setup() {
  Serial.begin(115200);

    // LED de verificación
  pinMode(pinVerificacion, OUTPUT);
  digitalWrite(pinVerificacion, LOW);

  // Configuración de pines (o usa ledcSetup/ledcAttachPin si usas el core viejo)
  pinMode(pinRojo, OUTPUT);
  pinMode(pinVerde, OUTPUT);
  pinMode(pinAzul, OUTPUT);

  // Iniciar con el LED apagado
  establecerColor(0, 0, 0);

  setup_wifi();
  mqttCliente.setServer(mqtt_server, mqtt_port);
  mqttCliente.setCallback(callback);
}

// --- 8. Bucle Principal ---
void loop() {

  // Blink no bloqueante: alterna el pin de verificación sin usar delay(),
  // así mqttCliente.loop() se sigue llamando en cada vuelta del loop().
  unsigned long ahora = millis();
  if (ahora - tiempoAnteriorParpadeo >= periodoParpadeoMs) {
    tiempoAnteriorParpadeo = ahora;
    estadoParpadeo = !estadoParpadeo;
    digitalWrite(pinVerificacion, estadoParpadeo ? HIGH : LOW);
  }

  if (!mqttCliente.connected()) {
    reconnect();
  }

  // Mantiene vivo el protocolo MQTT
  mqttCliente.loop();

  /* 
   * ¡IMPORTANTE! 
   * Nota que aquí ya no hay ningún establecerColor(0, 255, 255) 
   * ni lógica extra. Solo el callback() tiene permiso de cambiar la luz.
   * Esto erradica el problema del titileo Cian.
   */
}

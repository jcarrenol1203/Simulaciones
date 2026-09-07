#include <ArduinoJson.h>
#include <BLEDevice.h>
#include <BLEServer.h>
#include <BLEUtils.h>
#include <BLE2902.h>

// UUIDs para el BLE
#define SERVICE_UUID           "4fafc201-1fb5-459e-8fcc-c5c9c331914b"
// Característica para RECIBIR el color desde el celular (WRITE)
#define CHARACTERISTIC_RX_UUID "beb5483e-36e1-4688-b7f5-ea07361b26a8"
// Característica para ENVIAR la distancia al celular (NOTIFY)
#define CHARACTERISTIC_TX_UUID "71850116-2917-48f8-a145-c38d8f0f0312"

BLEServer* pServer = NULL;
BLECharacteristic* pCharacteristicTX = NULL;
bool deviceConnected = false;
bool oldDeviceConnected = false;

// Variables RGB
int pinRed = 13;
int pinGreen = 12;
int pinBlue = 11;
int frecuencia = 5000;
int resolucion = 8;

// Variables HC-SR04
const int trigPin = 15;
const int echoPin = 16;
int valor = 0;
int tiempo = 0;
int periodo = 100; // ms

// LED de verificación (mismo patrón que Séptima/Octava)
int pinVerificacion = 1;
unsigned long periodoParpadeoMs = 250; // 250 ms: identifica esta placa (MiTrigesimaNovenaApp) frente a la de Séptima (1000 ms) y Octava (500 ms)
unsigned long ultimoCambioParpadeo = 0;
bool estadoParpadeo = false;

// Callback para manejar conexión/desconexión
class MyServerCallbacks: public BLEServerCallbacks {
    void onConnect(BLEServer* pServer) {
      deviceConnected = true;
      Serial.println("¡Celular conectado!");
    };
    void onDisconnect(BLEServer* pServer) {
      deviceConnected = false;
      Serial.println("Celular desconectado.");
    }
};

// Callback para recibir el JSON de los colores
class MyCallbacks: public BLECharacteristicCallbacks {
  void onWrite(BLECharacteristic *pCharacteristic) {
    String rxValue = pCharacteristic->getValue().c_str();

    if (rxValue.length() > 0) {
      StaticJsonDocument<300> docRx;
      DeserializationError error = deserializeJson(docRx, rxValue);
      if (!error) {
        int r = docRx["r"];
        int g = docRx["g"];
        int b = docRx["b"];

        ledcWrite(pinRed, r);
        ledcWrite(pinGreen, g);
        ledcWrite(pinBlue, b);
      }
    }
  }
};

void setup() {
  Serial.begin(115200);

  // LED de verificación
  pinMode(pinVerificacion, OUTPUT);
  digitalWrite(pinVerificacion, LOW);

  // Configuración de Pines del Sensor
  pinMode(trigPin, OUTPUT);
  pinMode(echoPin, INPUT);

  // Configuración de Pines del LED (Core v3.0+)
  ledcAttach(pinRed, frecuencia, resolucion);
  ledcAttach(pinGreen, frecuencia, resolucion);
  ledcAttach(pinBlue, frecuencia, resolucion);
  ledcWrite(pinRed, 0);
  ledcWrite(pinGreen, 0);
  ledcWrite(pinBlue, 0);

  // 1. Inicialización BLE
  BLEDevice::init("PhysicsESP32_Dist");
  pServer = BLEDevice::createServer();
  pServer->setCallbacks(new MyServerCallbacks());

  BLEService *pService = pServer->createService(SERVICE_UUID);

  // 2. Crear Característica TX (enviamos datos al celular)
  pCharacteristicTX = pService->createCharacteristic(
                        CHARACTERISTIC_TX_UUID,
                        BLECharacteristic::PROPERTY_NOTIFY
                      );
  pCharacteristicTX->addDescriptor(new BLE2902());

  // 3. Crear Característica RX (recibimos datos del celular)
  BLECharacteristic *pCharacteristicRX = pService->createCharacteristic(
                                            CHARACTERISTIC_RX_UUID,
                                            BLECharacteristic::PROPERTY_WRITE
                                          );
  pCharacteristicRX->setCallbacks(new MyCallbacks());

  // 4. Iniciar Servicio y Publicidad
  pService->start();
  BLEAdvertising *pAdvertising = BLEDevice::getAdvertising();
  pAdvertising->addServiceUUID(SERVICE_UUID);
  pAdvertising->setScanResponse(true);
  pAdvertising->setMinPreferred(0x0);
  BLEDevice::startAdvertising();

  Serial.println("¡Dispositivo listo! Esperando conexión...");
}

void loop() {

  // No bloqueante: con delay() aquí, cada vuelta del loop tardaría más de lo
  // que "periodo" registra, desincronizando el eje de tiempo que se grafica
  // en el celular respecto al tiempo real transcurrido (mismo caso de Octava).
  if (millis() - ultimoCambioParpadeo >= periodoParpadeoMs) {
    estadoParpadeo = !estadoParpadeo;
    digitalWrite(pinVerificacion, estadoParpadeo ? HIGH : LOW);
    ultimoCambioParpadeo = millis();
  }

  // Solo leemos el sensor y enviamos datos si el celular está conectado
  if (deviceConnected) {
    medirDistancia();
    SerializarObject();
  }

  // --- Lógica de desconexión segura ---
  if (!deviceConnected && oldDeviceConnected) {
      delay(500);
      pServer->startAdvertising();
      Serial.println("Reiniciando publicidad Bluetooth");
      oldDeviceConnected = deviceConnected;
  }
  if (deviceConnected && !oldDeviceConnected) {
      oldDeviceConnected = deviceConnected;
  }

  delay(periodo);
  if (deviceConnected) {
    tiempo = tiempo + periodo;
  }
}

void medirDistancia() {
    digitalWrite(trigPin, LOW);
    delayMicroseconds(2);
    digitalWrite(trigPin, HIGH);
    delayMicroseconds(10);
    digitalWrite(trigPin, LOW);

    // Timeout de 30000 microsegundos para evitar que el ESP32 se congele
    // si no hay obstáculo (pulseIn nunca ve el flanco de bajada).
    float duracion = pulseIn(echoPin, HIGH, 30000);
    Serial.print("Tiempo de eco detectado: ");
    Serial.println(duracion);

    float velocidad_sonido = 340;
    if (duracion == 0) {
      valor = 0;
    } else {
      valor = ((duracion * velocidad_sonido) / 2) * 0.0001;
    }
}

void SerializarObject() {
    StaticJsonDocument<300> docTx;
    docTx["unidad"] = "cm";
    docTx["periodo"] = periodo;
    docTx["tiempo"] = tiempo;
    docTx["valor"] = valor;

    char buffer[200];
    serializeJson(docTx, buffer);

    pCharacteristicTX->setValue(buffer);
    pCharacteristicTX->notify();
}

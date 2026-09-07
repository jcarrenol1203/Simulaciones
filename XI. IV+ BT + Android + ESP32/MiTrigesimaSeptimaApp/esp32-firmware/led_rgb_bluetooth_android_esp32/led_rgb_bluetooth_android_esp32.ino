#include <BLEDevice.h>
#include <BLEServer.h>
#include <BLEUtils.h>
#include <ArduinoJson.h>

// UUIDs: Los identificadores únicos que tu app buscará para conectarse.
#define SERVICE_UUID        "4fafc201-1fb5-459e-8fcc-c5c9c331914b"
#define CHARACTERISTIC_UUID "beb5483e-36e1-4688-b7f5-ea07361b26a8"

// Recuerda usar los pines que sí existen en el ESP32-S3 (ej. 4, 5, 6)
int pinRed = 13;
int pinGreen = 12;
int pinBlue = 11;
int frecuencia = 5000;
int resolucion = 8;

// LED de verificación (adicional a la guía): parpadea siempre, esté o no
// conectado el celular, para confirmar a simple vista que el firmware sigue
// corriendo.
int pinVerificacion = 1;
unsigned long periodoParpadeoMs = 500;

// Esta clase es un "Callback". Se dispara automáticamente en segundo plano
// cada vez que tu celular escribe un dato en la Característica BLE.
class MyCallbacks: public BLECharacteristicCallbacks {
    void onWrite(BLECharacteristic *pCharacteristic) {
      // Obtenemos el valor que llegó desde el celular
      String rxValue = pCharacteristic->getValue().c_str();

      if (rxValue.length() > 0) {
        Serial.print("JSON Recibido: ");
        Serial.println(rxValue);

        // Reservamos memoria para el JSON
        StaticJsonDocument<300> doc;

        // Intentamos procesar el texto recibido
        DeserializationError error = deserializeJson(doc, rxValue);

        if (!error) {
          // Extraemos las variables
          int r = doc["r"];
          int g = doc["g"];
          int b = doc["b"];

          // Actualizamos los LEDs usando la nueva sintaxis (Directo al pin)
          ledcWrite(pinRed, r);
          ledcWrite(pinGreen, g);
          ledcWrite(pinBlue, b);
        } else {
          Serial.println("Error: El texto recibido no es un JSON válido");
        }
      }
    }
};

void setup() {
  Serial.begin(115200);

  // LED de verificación
  pinMode(pinVerificacion, OUTPUT);
  digitalWrite(pinVerificacion, LOW);

  // 1. Configuración moderna de PWM (Core ESP32 v3.0+)
  // Ya no se usan "canales", se asocia la frecuencia y resolución directo al pin.
  ledcAttach(pinRed, frecuencia, resolucion);
  ledcAttach(pinGreen, frecuencia, resolucion);
  ledcAttach(pinBlue, frecuencia, resolucion);

  // Apagamos los LEDs al iniciar
  ledcWrite(pinRed, 0);
  ledcWrite(pinGreen, 0);
  ledcWrite(pinBlue, 0);

  // 2. Inicialización del Servidor BLE
  BLEDevice::init("PhysicsESP32_BLE");
  BLEServer *pServer = BLEDevice::createServer();

  // Creamos el Servicio
  BLEService *pService = pServer->createService(SERVICE_UUID);

  // Creamos la Característica (Con permiso de ESCRITURA para recibir datos)
  BLECharacteristic *pCharacteristic = pService->createCharacteristic(
                                         CHARACTERISTIC_UUID,
                                         BLECharacteristic::PROPERTY_WRITE
                                       );

  // Asignamos nuestra clase Callback para que "escuche" las escrituras
  pCharacteristic->setCallbacks(new MyCallbacks());

  // Iniciamos el Servicio
  pService->start();

  // Configuramos el anuncio para que el celular lo pueda descubrir
  BLEAdvertising *pAdvertising = BLEDevice::getAdvertising();
  pAdvertising->addServiceUUID(SERVICE_UUID);
  pAdvertising->setScanResponse(true);
  BLEDevice::startAdvertising();

  Serial.println("¡BLE listo! Esperando conexión...");
}

void loop() {
  // El callback BLE corre en su propia tarea de FreeRTOS, así que este
  // delay() no bloquea ni afecta la conexión BLE.
  digitalWrite(pinVerificacion, HIGH);
  delay(periodoParpadeoMs);
  digitalWrite(pinVerificacion, LOW);
  delay(periodoParpadeoMs);
}

#include <Wire.h>
#include <BH1750.h>
#include <ArduinoJson.h>
#include <BLEDevice.h>
#include <BLEServer.h>
#include <BLEUtils.h>
#include <BLE2902.h>

// UUIDs para el servicio y característica
#define SERVICE_UUID        "4fafc201-1fb5-459e-8fcc-c5c9c331914b"
#define CHARACTERISTIC_UUID "beb5483e-36e1-4688-b7f5-ea07361b26a8"

BLEServer* pServer = NULL;
BLECharacteristic* pCharacteristic = NULL;
bool deviceConnected = false;
bool oldDeviceConnected = false;

BH1750 lightMeter(0x23);
StaticJsonDocument<300> doc;
uint16_t valor;
int tiempo = 0; // en ms
int periodo = 500; // en ms

int pinVerificacion = 1;
unsigned long periodoParpadeoMs = 500;
unsigned long ultimoCambioParpadeo = 0;
bool estadoParpadeo = false;

// Callback para saber si el celular se conectó o desconectó
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

void setup(){
  Serial.begin(115200);

    // LED de verificación
  pinMode(pinVerificacion, OUTPUT);
  digitalWrite(pinVerificacion, LOW);

  // Inicializa el bus I2C
  Wire.begin(8, 9); 
  
  if (lightMeter.begin()) {
    Serial.println(F("BH1750 inicializado"));
  } else {
    Serial.println(F("Error inicializando BH1750"));
  }

  // 1. Configuración de BLE
  BLEDevice::init("PhysicsESP32_BLE");
  pServer = BLEDevice::createServer();
  pServer->setCallbacks(new MyServerCallbacks());

  BLEService *pService = pServer->createService(SERVICE_UUID);

  // 2. Creamos la característica como NOTIFY (empuja datos al cliente)
  pCharacteristic = pService->createCharacteristic(
                      CHARACTERISTIC_UUID,
                      BLECharacteristic::PROPERTY_NOTIFY
                    );

  // 3. Este Descriptor es vital: le permite al celular "suscribirse" a las notificaciones
  pCharacteristic->addDescriptor(new BLE2902());

  pService->start();

  // 4. Iniciar la publicidad para que el celular lo encuentre
  BLEAdvertising *pAdvertising = BLEDevice::getAdvertising();
  pAdvertising->addServiceUUID(SERVICE_UUID);
  pAdvertising->setScanResponse(true);
  pAdvertising->setMinPreferred(0x0);  
  BLEDevice::startAdvertising();
  
  Serial.println("Esperando conexión para enviar datos de luz...");
}

void loop() {

  // No bloqueante: si usáramos delay() aquí (como antes), cada vuelta del
  // loop tardaría 1000 ms extra por el parpadeo, pero "tiempo" solo se
  // incrementaría en "periodo" (500 ms) -- desincronizando el eje de tiempo
  // que se grafica en el celular respecto al tiempo real transcurrido.
  if (millis() - ultimoCambioParpadeo >= periodoParpadeoMs) {
    estadoParpadeo = !estadoParpadeo;
    digitalWrite(pinVerificacion, estadoParpadeo ? HIGH : LOW);
    ultimoCambioParpadeo = millis();
  }

  // Solo leemos el sensor y enviamos JSON si hay un celular conectado
  if (deviceConnected) {
    valor = lightMeter.readLightLevel();
    SerializeObject();
  }

  // --- Lógica para manejar desconexiones ---
  if (!deviceConnected && oldDeviceConnected) {
      delay(500); 
      pServer->startAdvertising(); // Volver a ser visible
      Serial.println("Reiniciando publicidad Bluetooth");
      oldDeviceConnected = deviceConnected;
  }
  // --- Lógica para manejar nuevas conexiones ---
  if (deviceConnected && !oldDeviceConnected) {
      oldDeviceConnected = deviceConnected;
  }

  delay(periodo);
  tiempo = tiempo + periodo;
}
 
void SerializeObject() {
   doc.clear(); // Limpiar el JSON anterior para no acumular basura
   doc["unidad"] = "lux";
   doc["periodo"] = periodo;
   doc["tiempo"] = tiempo;
   doc["valor"] = valor;   
   
   char buffer[200];
   // Usamos serializeJson normal (no Pretty) para ahorrar bytes, 
   // ya que el BLE tiene un límite de envío por paquete (MTU).
   serializeJson(doc, buffer); 

   // Empujamos el dato por BLE
   pCharacteristic->setValue(buffer);
   pCharacteristic->notify();
   
   // También lo imprimimos en el Serial Monitor para depurar
   Serial.println(buffer);      
}

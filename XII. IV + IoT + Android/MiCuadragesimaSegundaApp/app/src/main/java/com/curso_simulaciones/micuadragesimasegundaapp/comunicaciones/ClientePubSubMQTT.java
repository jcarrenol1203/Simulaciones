package com.curso_simulaciones.micuadragesimasegundaapp.comunicaciones;

import static android.content.ContentValues.TAG;

import android.app.Activity;
import android.util.Log;

import com.curso_simulaciones.micuadragesimasegundaapp.datos.AlmacenDatosRAM;

import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

/*
 * ==========================================================
 *   ADVERTENCIA IMPORTANTE PARA LOS ESTUDIANTES
 * ==========================================================
 *
 * En versiones antiguas de Android se usaba con frecuencia
 * la clase MqttAndroidClient junto con el archivo
 * org.eclipse.paho.android.service.jar.
 *
 * A partir de Android 12 este enfoque dejó de ser seguro
 * porque esa librería utiliza un componente interno
 * llamado AlarmReceiver que ya no es aceptado por el
 * sistema y produce errores de seguridad.
 *
 * La forma recomendada y "a prueba de futuro" es usar
 * directamente la clase MqttAsyncClient que se encuentra
 * en el archivo org.eclipse.paho.client.mqttv3.jar.
 *
 * Ventajas:
 *   - Funciona en Android 12, 13 y posteriores.
 *   - No necesita BroadcastReceiver ni configuraciones
 *     especiales en AndroidManifest.xml.
 *   - Es más simple y confiable para proyectos actuales.
 *
 * Por esta razón, en este curso usaremos MqttAsyncClient.
 * ==========================================================
 */
public class ClientePubSubMQTT implements MqttCallback, IMqttActionListener {
    private final Activity actividad;
    private static String MQTTHOST;
    private static String USERNAME;
    private static String PASSWORD;
    private String topicStr;
    private MqttAsyncClient client;
    private MqttConnectOptions options;
    private String datoString;

    public ClientePubSubMQTT(Activity actividad) {
        this.actividad = actividad;
        MQTTHOST = AlmacenDatosRAM.MQTTHOST;
        USERNAME = AlmacenDatosRAM.USERNAME;
        PASSWORD = AlmacenDatosRAM.PASSWORD;
        topicStr = AlmacenDatosRAM.topicStr;
    }

    // 1) Conectar
    public void conectar() {
        try {
            String clientId = MqttAsyncClient.generateClientId();
            client = new MqttAsyncClient(
                    MQTTHOST,
                    clientId,
                    new MemoryPersistence()
            );
            client.setCallback(this);
            options = new MqttConnectOptions();
            options.setMqttVersion(MqttConnectOptions.MQTT_VERSION_3_1_1);
            options.setUserName(USERNAME);
            options.setPassword(PASSWORD.toCharArray());
            options.setCleanSession(true);
            options.setAutomaticReconnect(true);
            options.setKeepAliveInterval(60);
            client.connect(options, null, this);
            AlmacenDatosRAM.conectado_PubSub = "Conectando con el broker...";
        } catch (MqttException e) {
            AlmacenDatosRAM.conectado_PubSub = "Falla conexión con el broker...";
            AlmacenDatosRAM.conectado = false;
            e.printStackTrace();
        }
    }

    // Éxito de connect()
    @Override
    public void onSuccess(IMqttToken asyncActionToken) {
        try {
            client.subscribe(topicStr, 0);
            AlmacenDatosRAM.conectado_PubSub = "Suscripción al tópico exitosa";
            AlmacenDatosRAM.conectado = true;
        } catch (MqttException e) {
            AlmacenDatosRAM.conectado_PubSub = "Falla la suscripción al tópico...";
            AlmacenDatosRAM.conectado = false;
            e.printStackTrace();
        }
    }

    // Falla de connect()
    @Override
    public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
        Log.d(TAG, "Falla conexión", exception);
        AlmacenDatosRAM.conectado_PubSub = "Falla conexión con el broker...";
        AlmacenDatosRAM.conectado = false;
    }

    // Se perdió la conexión
    @Override
    public void connectionLost(Throwable cause) {
        Log.d(TAG, "Conexión perdida", cause);
        AlmacenDatosRAM.conectado = false;
    }

    // Llegó un mensaje
    @Override
    public void messageArrived(String topic, MqttMessage mqttMessage) {
        if (AlmacenDatosRAM.conectado) {
            datoString = new String(mqttMessage.getPayload());
            AlmacenDatosRAM.conectado_PubSub = "Recibiendo datos...";
        }
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
        Log.d(TAG, "Mensaje entregado");
    }

    // 2) Leer mensaje
    public String leerString() {
        return datoString;
    }

    // 3) Enviar mensajes
    public void setEnviarMensajes(byte[] datoBytesEnviar) {
        if (client == null || !client.isConnected())
            return;
        try {
            AlmacenDatosRAM.conectado_PubSub = "Enviando datos...";
            MqttMessage message = new MqttMessage(datoBytesEnviar);
            message.setQos(1);
            message.setRetained(false);
            client.publish(topicStr, message);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    // 4) Desconectar
    public void desconectar() {
        try {
            if (client != null && client.isConnected()) {
                client.unsubscribe(topicStr);
                client.disconnect(null, null);
            }
            AlmacenDatosRAM.conectado = false;
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }
}

package client;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

public class Publisher implements PropertyChangeListener {
    public MqttClient client;
    public String clientId;
    public String answerTopic;

    public Publisher(String broker) {
        this.clientId = MqttClient.generateClientId();
        try {
            this.client = new MqttClient(broker, clientId);
            client.connect();
            System.out.println("Connected to BROKER: " + broker);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }
    public void publish(String topic, String message){
        if(client.isConnected()) {
            try {
                MqttMessage delivery = new MqttMessage(message.getBytes());
                delivery.setQos(2);
                client.publish(topic,delivery);
                System.out.println("Published: " + message);
            } catch (MqttException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if(evt.getNewValue() instanceof String answer) {
            publish(this.answerTopic,answer);
        }

    }
}
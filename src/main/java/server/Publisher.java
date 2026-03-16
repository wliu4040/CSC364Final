package server;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

public class Publisher{
    MqttClient client;

    public Publisher(String broker) {
        String clientId = MqttClient.generateClientId();
        try {
            this.client = new MqttClient(broker, clientId);
            client.connect();
            System.out.println("Connected to BROKER: " + broker);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }
    public void publishJob(String topic, String message){
        if(client.isConnected()) {
            try {
                MqttMessage delivery = new MqttMessage(message.getBytes());
                delivery.setQos(2);
                client.publish(topic,delivery);
                System.out.println("Published job: " + message + " @ " + topic);
            } catch (MqttException e) {
                throw new RuntimeException(e);
            }
        }

    }

}
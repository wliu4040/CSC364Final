package client;

import org.eclipse.paho.client.mqttv3.*;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

public class Subscriber implements MqttCallback {
    PropertyChangeSupport pcs = new PropertyChangeSupport(this);
    MqttClient client;

    public Subscriber(String broker) {
        try {
            String clientId = MqttClient.generateClientId();
            client = new MqttClient(broker, clientId);
            client.setCallback(this);
            client.connect();
            System.out.println("Connected to BROKER: " + broker);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    public void addTopic(String topic) throws MqttException {
        client.subscribe(topic);
        System.out.println("Listening on: " + topic);
    }
    @Override
    public void connectionLost(Throwable throwable) {
        System.out.println("Connection lost: " + throwable.getMessage());
    }

    @Override
    public void messageArrived(String topic, MqttMessage mqttMessage) {
        String message = new String(mqttMessage.getPayload());
        if(!message.contains("[")) {
            System.out.println("Received message: " + message);
        }
        if(topic.contains("/assign") && !message.contains("[")) {
            try {
                String answer = Solver.solve(message);
                pcs.firePropertyChange("answer",null,answer);
            }
            catch (Exception e) {
                System.out.println("system boom");
            }
        }
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        pcs.addPropertyChangeListener(listener);
    }

}
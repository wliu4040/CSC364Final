package client;

import org.eclipse.paho.client.mqttv3.MqttException;

public class Main {
    private static final String broker = "tcp://test.mosquitto.org:1883";
    private static final String requestTopic = "/csc364/wliu40/request";
    private static final String answersTopic = "/csc364/wliu40/assign/";

    public static void main(String[] args) {
        int numThreads = 10;
        for(int i = 0; i < numThreads; i++) {
            Publisher publisher = new Publisher(broker);
            String clientId = publisher.clientId;
            Subscriber subscriber = new Subscriber(broker);
            subscriber.addPropertyChangeListener(publisher);
            try {
                subscriber.addTopic(answersTopic + clientId);
            } catch (MqttException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
            publisher.publish(requestTopic,clientId);
            publisher.answerTopic = answersTopic + clientId;

        }

    }
}

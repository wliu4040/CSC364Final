package server;

import org.eclipse.paho.client.mqttv3.MqttException;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.*;
import java.util.stream.Collectors;

public class SubscriberManager implements PropertyChangeListener {
    private final String topicBase;
    private final Subscriber subscriber;
    private final WorkerTracker workerTracker;
    private final String workerFinderTopic;
    double bestDistance = Double.MAX_VALUE;
    List<Integer> bestTour = new ArrayList<>();
    private final Timer timer;

    public SubscriberManager(String broker, String topicBase, WorkerTracker workerTracker, MapPanel mapPanel) {
        this.topicBase = topicBase;
        workerFinderTopic = topicBase + "/request";
        subscriber = new Subscriber(broker);
        try {
            subscriber.addTopic(workerFinderTopic);
        } catch (MqttException e) {
            throw new RuntimeException(e);
        }
        subscriber.addPropertyChangeListener(this);
        this.workerTracker = workerTracker;

        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                mapPanel.setTour(bestTour);
            }
        },0,1000);

    }

    public void shutdown() {
        timer.cancel();
    }

    public void addWorkerSubscription(String workerClientID) throws MqttException {
        String topic = String.format("%s/assign/%s",topicBase,workerClientID);
        subscriber.addTopic(topic);
    }

    public void removeClient(String clientID) throws MqttException {
        String workerTopic = String.format("%s/assign/%s",topicBase,clientID);
        subscriber.removeTopic(workerTopic);
    }


    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if(evt.getOldValue().equals(workerFinderTopic) && evt.getNewValue() instanceof String message) {
            workerTracker.addWorker(message);
        }
        else if(evt.getOldValue() instanceof String topic && topic.contains("/assign") && evt.getNewValue() instanceof String message && message.contains("[")){
            String[] parts = message.split(" ");
            double distance = Double.parseDouble(parts[1]);
            if(distance < bestDistance) {
                bestDistance = distance;
                String tourStr = message.substring(message.indexOf("[") + 1, message.indexOf("]"));
                bestTour = Arrays.stream(tourStr.split(", "))
                        .map(Integer::parseInt)
                        .collect(Collectors.toList());
            }
            System.out.println("Externally processed " + message);
            String workerClientId = topic.substring(topic.lastIndexOf("/") + 1);
            String jobId = parts[0];
            workerTracker.jobFinished(workerClientId,jobId);
        }
    }




}

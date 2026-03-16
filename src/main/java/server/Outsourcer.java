package server;

import org.eclipse.paho.client.mqttv3.MqttException;

import javax.swing.*;
import java.util.*;
import java.util.Timer;

public class Outsourcer implements Runnable {
    private final Queue<Job> jobsQueue;
    private static final String broker = "tcp://test.mosquitto.org:1883";
    private static final String topicBase = "/csc364/wliu40";
    private SubscriberManager subscriberManager ;
    private PublisherManager publisherManager;
    private WorkerTracker workerTracker;
    public Outsourcer(String fileUrl, List<City> cities, MapPanel mapPanel, JTextArea log) {
        this.jobsQueue = initializeJobsQueue(fileUrl,cities);
        WorkerTracker workerTracker = new WorkerTracker();
        this.workerTracker = workerTracker;
        subscriberManager = new SubscriberManager(broker, topicBase,workerTracker,mapPanel,log);
        publisherManager = new PublisherManager(broker,topicBase,workerTracker);
    }

    @Override
    public void run() {
        while(true) {
            try {
                Job job = jobsQueue.poll();
                if(job == null) {
                    subscriberManager.shutdown();
                    break;
                }
                String clientID = workerTracker.getNextAvailableWorker();
                outsource(job,clientID);
            } catch (InterruptedException | MqttException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void outsource(Job job,String clientID) throws InterruptedException, MqttException {
        subscriberManager.addWorkerSubscription(clientID);
        publisherManager.publishTask(job,clientID);
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                Job currTask = workerTracker.getWorkerTask(clientID);
                if(currTask != null && currTask.getId() == job.getId()) {
                    try {
                        subscriberManager.removeClient(clientID);
                        String new_clientID = workerTracker.getNextAvailableWorker();
                        outsource(job,new_clientID);
                    } catch (MqttException e) {
                        throw new RuntimeException(e);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }, 5000);
    }

    public Queue<Job> initializeJobsQueue(String fileUrl, List<City> cities) {
        int currIndex = 0;
        Queue<Job> queue = new LinkedList<>();
        int numCities = cities.size();
        while(currIndex + 100 < numCities) {
            queue.add(new Job(fileUrl,currIndex, currIndex + 100));
            currIndex += 100;
        }
        if(currIndex < numCities) {
            queue.add(new Job(fileUrl,currIndex, numCities));
        }
        return queue;
    }
}

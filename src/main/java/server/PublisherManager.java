package server;

public class PublisherManager {
    private final String topicBase;
    private final WorkerTracker workerTracker;
    private final Publisher publisher;
    public PublisherManager(String broker, String topicBase, WorkerTracker workerTracker) {
        this.topicBase = topicBase;
        this.workerTracker = workerTracker;
        this.publisher = new Publisher(broker);
    }

    public void publishTask(Job job, String workerClientId) {
        String jobDestination = topicBase + "/assign/" + workerClientId;
        publisher.publishJob(jobDestination,job.toString());
        workerTracker.addJob(workerClientId,job);

    }
}

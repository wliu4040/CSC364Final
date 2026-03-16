package server;

public class Job {
    private static int idCounter = 0;
    int id;
    int startIndex;
    int endIndex;
    String fileUrl;

    public Job(String fileUrl, int startIndex, int endIndex) {
        this.fileUrl = fileUrl;
        this.id = idCounter++;
        this.startIndex = startIndex;
        this.endIndex = endIndex;
    }

    public int getId() {
        return id;
    }
    @Override
    public String toString() {
        return String.format("%d %d %d %s", id, startIndex, endIndex, fileUrl);
    }
}

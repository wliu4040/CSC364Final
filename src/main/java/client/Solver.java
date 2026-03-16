package client;

import server.City;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class Solver {
    public static int processTime = 600;
    public static String solve(String job) throws IOException {
        String[] messageParts = job.split(" ");
        int jobId = Integer.parseInt(messageParts[0]);
        int startIndex = Integer.parseInt(messageParts[1]);
        int endIndex = Integer.parseInt(messageParts[2]);
        String url = messageParts[3];
        String fileName = url.split("/")[ url.split("/").length - 1];
        List<City> cities;
        if(!Files.exists(Path.of(fileName))){
            try (InputStream in = new URL(url).openStream()) {
                Files.copy(in, Path.of(fileName));
            }
        }
        cities = TspParser.load(new File(fileName));
        double bestDistance = Double.MAX_VALUE;
        List<Integer> bestTour = null;
        for(int i = startIndex; i < endIndex; i++) {
            List<Integer> currentTour = NearestNeighborSolver.solve(cities, i);
            double distance = NearestNeighborSolver.length(cities, currentTour);
            if(distance < bestDistance) {
                bestDistance = distance;
                bestTour = currentTour;
            }
        }

        return String.format("%d %f %s",jobId,bestDistance,bestTour);
    }
}


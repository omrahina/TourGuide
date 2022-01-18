package tourGuide.service;

import gpsUtil.GpsUtil;
import gpsUtil.location.VisitedLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import tourGuide.user.User;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

@Service
public class TrackerService {

    private final Logger LOGGER = LoggerFactory.getLogger(TrackerService.class);

    private final GpsUtil gpsUtil;
    private final ExecutorService trackerExecutorService;

    public TrackerService(GpsUtil gpsUtil, @Qualifier("fixedTrackerThreadPool") ExecutorService trackerExecutorService) {
        this.gpsUtil = gpsUtil;
        this.trackerExecutorService = trackerExecutorService;

    }

    /**
     * Assures that no new task is submitted and awaits for executing tasks to terminate
     */
    public void stopTracking() {
        trackerExecutorService.shutdown();
        try {
            trackerExecutorService.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            LOGGER.error(e.getMessage());
        }
    }

    public Future<VisitedLocation> trackUserLocation(User user){
        return trackerExecutorService.submit(() -> {
            VisitedLocation visitedLocation = gpsUtil.getUserLocation(user.getUserId());
            user.addToVisitedLocations(visitedLocation);
            return visitedLocation;
        });
    }

    public VisitedLocation getUserLocation(User user) throws ExecutionException, InterruptedException {
        if (user.getVisitedLocations().isEmpty()) {
            return trackUserLocation(user).get();
        }

        return user.getLastVisitedLocation();
    }

}

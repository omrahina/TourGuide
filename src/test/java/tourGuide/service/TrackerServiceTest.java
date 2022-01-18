package tourGuide.service;

import gpsUtil.GpsUtil;
import gpsUtil.location.Location;
import gpsUtil.location.VisitedLocation;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringRunner;
import tourGuide.user.User;

import java.util.Date;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
public class TrackerServiceTest {


    @Mock
    private GpsUtil gpsUtil;

    private ExecutorService executorService;

    @InjectMocks
    private TrackerService trackerService;

    @BeforeClass
    public static void setUp() {

        Locale.setDefault(new Locale("en", "US"));
        System.setProperty("tracker.nbThreads", "9");
    }

    @Before
    public void setUpEachTest(){
        executorService = Executors.newFixedThreadPool(9);
        trackerService = new TrackerService(gpsUtil, executorService);
    }

    @Test
    public void should_trackUserLocation_ok() {
        UUID userId = UUID.randomUUID();
        //when(executorService.submit((Runnable) Mockito.any(ThreadPoolExecutor.class))).thenCallRealMethod();
        when(gpsUtil.getUserLocation(any())).thenReturn(new VisitedLocation(userId, new Location(0.12, 0.3), new Date()));
        User user = new User(userId, "jon", "000", "jon@tourGuide.com");
        trackerService.trackUserLocation(user);
        trackerService.stopTracking();

        assertEquals(user.getUserId(), user.getLastVisitedLocation().userId);
    }

    @Test
    public void getUserLocation_empty_visitedLocation() throws ExecutionException, InterruptedException {
        UUID userId = UUID.randomUUID();
        when(gpsUtil.getUserLocation(any())).thenReturn(new VisitedLocation(userId, new Location(0.12, 0.3), new Date()));
        User user = new User(userId, "jon", "000", "jon@tourGuide.com");
        VisitedLocation visitedLocation = trackerService.getUserLocation(user);
        trackerService.stopTracking();

        assertEquals(visitedLocation.userId, user.getUserId());
    }

}

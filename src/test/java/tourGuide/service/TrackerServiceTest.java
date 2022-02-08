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
import tourGuide.data.InternalDataHelper;
import tourGuide.dto.LocationHistoryDto;
import tourGuide.user.User;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
public class TrackerServiceTest {

    @Mock
    private GpsUtil gpsUtil;

    @InjectMocks
    private TrackerService trackerService;

    private UUID userId;

    @BeforeClass
    public static void setUp() {
        Locale.setDefault(new Locale("en", "US"));
    }

    @Before
    public void setUpEachTest(){
        ExecutorService executorService = Executors.newFixedThreadPool(9);
        trackerService = new TrackerService(gpsUtil, executorService);
        userId = UUID.randomUUID();
    }

    @Test
    public void should_trackUserLocation_ok() {
        when(gpsUtil.getUserLocation(any())).thenReturn(new VisitedLocation(userId, new Location(0.12, 0.3), new Date()));
        User user = new User(userId, "jon", "000", "jon@tourGuide.com");
        trackerService.trackUserLocation(user);
        trackerService.stopTracking();

        assertEquals(user.getUserId(), user.getLastVisitedLocation().userId);
    }

    @Test
    public void getUserLocation_empty_visitedLocation_case() {
        when(gpsUtil.getUserLocation(any())).thenReturn(new VisitedLocation(userId, new Location(0.12, 0.3), new Date()));
        User user = new User(userId, "jon", "000", "jon@tourGuide.com");
        VisitedLocation visitedLocation = trackerService.getUserLocation(user);
        trackerService.stopTracking();

        assertEquals(visitedLocation.userId, user.getUserId());
    }

    @Test
    public void getUserLocation_not_empty_visitedLocations_case() {
        User user = new User(userId, "jon", "000", "jon@tourGuide.com");
        VisitedLocation visitedLocation = new VisitedLocation(userId, new Location(0.12, 0.3), new Date());
        user.addToVisitedLocations(visitedLocation);
        VisitedLocation userLocation = trackerService.getUserLocation(user);

        assertEquals(visitedLocation, userLocation);
    }

    @Test
    public void should_return_all_known_locations() {
        InternalDataHelper.setInternalUserNumber(2);
        InternalDataHelper.initializeInternalUsers();
        InternalDataHelper.getInternalUserMap().get("internalUser0").clearVisitedLocations();
        List<User> users = new ArrayList<>(InternalDataHelper.getInternalUserMap().values());

        List<LocationHistoryDto> locationHistory = trackerService.getAllKnownLocations(users);

        assertNotNull(locationHistory);
        assertEquals(1, locationHistory.size());
    }

    @Test
    public void should_not_find_any_location_history() {
        InternalDataHelper.setInternalUserNumber(2);
        InternalDataHelper.initializeInternalUsers();
        InternalDataHelper.getInternalUserMap().get("internalUser0").clearVisitedLocations();
        InternalDataHelper.getInternalUserMap().get("internalUser1").clearVisitedLocations();
        List<User> users = new ArrayList<>(InternalDataHelper.getInternalUserMap().values());

        List<LocationHistoryDto> locationHistory = trackerService.getAllKnownLocations(users);

        assertNull(locationHistory);
    }

}

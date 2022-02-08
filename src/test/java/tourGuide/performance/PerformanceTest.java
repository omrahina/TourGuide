package tourGuide.performance;

import gpsUtil.GpsUtil;
import gpsUtil.location.Attraction;
import gpsUtil.location.VisitedLocation;
import org.apache.commons.lang3.time.StopWatch;
import org.junit.Before;
import org.junit.Test;
import rewardCentral.RewardCentral;
import tourGuide.data.InternalDataHelper;
import tourGuide.service.InternalUserService;
import tourGuide.service.RewardsService;
import tourGuide.service.TrackerService;
import tourGuide.user.User;

import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertTrue;

public class PerformanceTest {

	private GpsUtil gpsUtil;
	private RewardsService rewardsService;
	private TrackerService trackerService;
	private InternalUserService internalUserService;
	private final ExecutorService trackerExecutorService = Executors.newFixedThreadPool(9);
	private final ExecutorService rewardsExecutorService = Executors.newFixedThreadPool(45);

	@Before
	public void setUp(){
		gpsUtil = new GpsUtil();
		trackerService = new TrackerService(gpsUtil, trackerExecutorService);
		rewardsService = new RewardsService(gpsUtil, new RewardCentral(), rewardsExecutorService);
		internalUserService = new InternalUserService();
		Locale.setDefault(new Locale("en", "US"));
	}

	@Test
	public void highVolumeTrackLocation() {

		// Users should be incremented up to 100,000, and test finishes within 15 minutes
		InternalDataHelper.setInternalUserNumber(100);
		InternalDataHelper.initializeInternalUsers();

		List<User> allUsers = internalUserService.getAllUsers();
	    StopWatch stopWatch = new StopWatch();
		stopWatch.start();
		for(User user : allUsers) {
			trackerService.trackUserLocation(user);
		}
		trackerService.stopTracking();
		stopWatch.stop();

		System.out.println("highVolumeTrackLocation: Time Elapsed: " + TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()) + " seconds."); 
		assertTrue(TimeUnit.MINUTES.toSeconds(15) >= TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()));
	}

	@Test
	public void highVolumeGetRewards() {

		// Users should be incremented up to 100,000, and test finishes within 20 minutes
		InternalDataHelper.setInternalUserNumber(10);
		InternalDataHelper.initializeInternalUsers();
		
	    Attraction attraction = gpsUtil.getAttractions().get(0);
		List<User> allUsers = internalUserService.getAllUsers();
		allUsers.forEach(u -> u.addToVisitedLocations(new VisitedLocation(u.getUserId(), attraction, new Date())));
		StopWatch stopWatch = new StopWatch();
		stopWatch.start();
	    allUsers.forEach(u -> rewardsService.calculateRewards(u));
	    rewardsService.stopRewarding();
		stopWatch.stop();

		for(User user : allUsers) {
			assertTrue(user.getUserRewards().size() > 0);
		}
		System.out.println("highVolumeGetRewards: Time Elapsed: " + TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()) + " seconds."); 
		assertTrue(TimeUnit.MINUTES.toSeconds(20) >= TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()));
	}
	
}

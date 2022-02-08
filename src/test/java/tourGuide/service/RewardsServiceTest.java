package tourGuide.service;

import gpsUtil.GpsUtil;
import gpsUtil.location.Attraction;
import gpsUtil.location.VisitedLocation;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringRunner;
import rewardCentral.RewardCentral;
import tourGuide.dto.NearbyAttractionDto;
import tourGuide.exceptions.NoDealOrRewardException;
import tourGuide.user.User;
import tourGuide.user.UserReward;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
public class RewardsServiceTest {

	@Mock
	private GpsUtil gpsUtil;

	@Mock
	private RewardCentral rewardsCentral;

	@InjectMocks
	private RewardsService rewardsService;

	private Attraction attraction;

	@BeforeClass
	public static void setUp() {
		Locale.setDefault(new Locale("en", "US"));
	}

	@Before
	public void setUpPerTest(){
		ExecutorService rewardsExecutorService = Executors.newFixedThreadPool(45);
		rewardsService = new RewardsService(gpsUtil, rewardsCentral, rewardsExecutorService);
		attraction = new Attraction("Disneyland", "Anaheim", "CA", 33.817595D, -117.922008D);
	}

	@Test
	public void should_return_userRewards() throws ExecutionException, InterruptedException {
		when(gpsUtil.getAttractions()).thenReturn(Collections.singletonList(attraction));
		when(rewardsCentral.getAttractionRewardPoints(any(), any())).thenReturn(5);
		User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
		user.addToVisitedLocations(new VisitedLocation(user.getUserId(), attraction, new Date()));
		List<UserReward> userRewards = rewardsService.getUserRewards(user);
		rewardsService.stopRewarding();

		assertTrue(user.getUserRewards().size() > 0);
		assertEquals(1, userRewards.size());
	}

	@Test
	public void should_throw_NoDealOrRewardException() {
		when(gpsUtil.getAttractions()).thenReturn(Collections.singletonList(attraction));
		User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");

		assertThatExceptionOfType(NoDealOrRewardException.class).isThrownBy(()
				-> rewardsService.getUserRewards(user))
				.withMessage("Rewards record empty");

	}

	@Test
	public void should_return_nearByAttractions() {
		when(gpsUtil.getAttractions()).thenReturn(Collections.singletonList(attraction));
		when(rewardsCentral.getAttractionRewardPoints(any(), any())).thenReturn(5);
		VisitedLocation visitedLocation = new VisitedLocation(UUID.randomUUID(), attraction, new Date());
		NearbyAttractionDto nearbyAttractions = rewardsService.getNearByAttractions(visitedLocation);

		assertEquals(visitedLocation.location, nearbyAttractions.getUserLocation());
		assertThat(nearbyAttractions.getNearbyAttractions()).isNotEmpty().hasSize(1);
	}

	@Test
	public void getNearbyAttractions_nothing_found() {
		when(gpsUtil.getAttractions()).thenReturn(new ArrayList<>());
		VisitedLocation visitedLocation = new VisitedLocation(UUID.randomUUID(), attraction, new Date());
		NearbyAttractionDto nearbyAttractions = rewardsService.getNearByAttractions(visitedLocation);

		assertNull(nearbyAttractions.getNearbyAttractions());
	}

	@Test
	public void isWithinAttractionProximity() {
		assertTrue(rewardsService.isWithinAttractionProximity(attraction, attraction));
	}

	@Test
	public void should_calculate_distance_between_two_locations() {
		VisitedLocation visitedLocation = new VisitedLocation(UUID.randomUUID(), attraction, new Date());
		assertTrue(rewardsService.getDistance(visitedLocation.location, visitedLocation.location) < 1);
	}
	
}

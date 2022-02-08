package tourGuide.service;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;
import tourGuide.exceptions.NoDealOrRewardException;
import tourGuide.user.User;
import tripPricer.Provider;
import tripPricer.TripPricer;

import java.util.List;
import java.util.UUID;

import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
public class TourGuideServiceTest {

	private TripPricer tripPricer;
	private TourGuideService tourGuideService;

	@BeforeClass
	public static void setUp() {
		System.setProperty("tripPricer.tripPricerApiKey", "test-server-api-key");
	}

	@Before
	public void setUpEachTest(){
		tripPricer = new TripPricer();
		tourGuideService = new TourGuideService(tripPricer);
	}

	@Test
	public void should_return_tripDeals() throws NoDealOrRewardException {
		UUID userId = UUID.randomUUID();
		User user = new User(userId, "jon", "000", "jon@tourGuide.com");

		List<Provider> providers = tourGuideService.getTripDeals(user);

		assertEquals(5, providers.size());
	}
	
	
}

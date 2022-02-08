package tourGuide.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import tourGuide.exceptions.NoDealOrRewardException;
import tourGuide.user.User;
import tourGuide.user.UserReward;
import tripPricer.Provider;
import tripPricer.TripPricer;

import java.util.List;

@Service
public class TourGuideService {

	@Value("${tripPricer.tripPricerApiKey}")
	private String tripPricerApiKey;

	private final Logger LOGGER = LoggerFactory.getLogger(TourGuideService.class);
	private final TripPricer tripPricer;
	
	public TourGuideService(TripPricer tripPricer) {
		this.tripPricer = tripPricer;
	}
	
	public List<Provider> getTripDeals(User user) throws NoDealOrRewardException{
		int cumulativeRewardPoints = user.getUserRewards().stream().mapToInt(UserReward::getRewardPoints).sum();
		List<Provider> providers = tripPricer.getPrice(tripPricerApiKey, user.getUserId(), user.getUserPreferences().getNumberOfAdults(),
				user.getUserPreferences().getNumberOfChildren(), user.getUserPreferences().getTripDuration(), cumulativeRewardPoints);
		if (!providers.isEmpty()) {
			LOGGER.info("TripDeals retrieved");
			user.setTripDeals(providers);
			return providers;
		}
		LOGGER.debug("Trip deals empty");
		throw new NoDealOrRewardException("No Trip Deal available");
	}
	
}

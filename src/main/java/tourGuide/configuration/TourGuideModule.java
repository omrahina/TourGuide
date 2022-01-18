package tourGuide.configuration;

import gpsUtil.GpsUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import rewardCentral.RewardCentral;
import tourGuide.service.RewardsService;
import tripPricer.TripPricer;

import java.util.concurrent.ExecutorService;

@Configuration
public class TourGuideModule {

	@Autowired
	@Qualifier("fixedRewardsThreadPool")
	private ExecutorService rewardsExecutorService;

	@Bean
	public GpsUtil getGpsUtil() {
		return new GpsUtil();
	}
	
	@Bean
	public RewardsService getRewardsService() {
		return new RewardsService(getGpsUtil(), getRewardCentral(), rewardsExecutorService);
	}
	
	@Bean
	public RewardCentral getRewardCentral() {
		return new RewardCentral();
	}

	@Bean
	public TripPricer getTripPricer() {
		return new TripPricer();
	}
	
}

package tourGuide.service;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import gpsUtil.GpsUtil;
import gpsUtil.location.Attraction;
import gpsUtil.location.Location;
import gpsUtil.location.VisitedLocation;
import rewardCentral.RewardCentral;
import tourGuide.user.User;
import tourGuide.user.UserReward;

@Service
public class RewardsService {

	private final Logger LOGGER = LoggerFactory.getLogger(RewardsService.class);

	private final GpsUtil gpsUtil;
	private final RewardCentral rewardsCentral;
	private final ExecutorService rewardsExecutorService;

    private static final double STATUTE_MILES_PER_NAUTICAL_MILE = 1.15077945;

	// proximity in miles
	private int proximityBuffer;
	private final int attractionProximityRange = 200;
	
	public RewardsService(GpsUtil gpsUtil, RewardCentral rewardCentral, @Qualifier("fixedRewardsThreadPool") ExecutorService rewardsExecutorService) {
		this.gpsUtil = gpsUtil;
		this.rewardsCentral = rewardCentral;
		this.rewardsExecutorService = rewardsExecutorService;
		this.proximityBuffer = 10;
	}
	
	public void setProximityBuffer(int proximityBuffer) {
		this.proximityBuffer = proximityBuffer;
	}

	public List<UserReward> getUserRewards(User user) {
		return user.getUserRewards();
	}

	public void calculateRewards(User user) {
		rewardsExecutorService.submit(() -> {
			List<VisitedLocation> userLocations = user.getVisitedLocations();
			List<Attraction> attractions = gpsUtil.getAttractions();

			for(VisitedLocation visitedLocation : userLocations) {
				for(Attraction attraction : attractions) {
					if(user.getUserRewards().stream().filter(r -> r.attraction.attractionName.equals(attraction.attractionName)).count() == 0) {
						if(nearAttraction(visitedLocation, attraction)) {
							user.addUserReward(new UserReward(visitedLocation, attraction, getRewardPoints(attraction, user)));
						}
					}
				}
			}
		});
	}

	/**
	 * Assures that no new task is submitted and awaits for executing tasks to terminate
	 */
	public void stopRewarding() {
		rewardsExecutorService.shutdown();
		try {
			rewardsExecutorService.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
		} catch (InterruptedException e) {
			LOGGER.error(e.getMessage());
		}
	}
	
	public boolean isWithinAttractionProximity(Attraction attraction, Location location) {
		return !(getDistance(attraction, location) > attractionProximityRange);
	}
	
	private boolean nearAttraction(VisitedLocation visitedLocation, Attraction attraction) {
		return getDistance(attraction, visitedLocation.location) > proximityBuffer ? false : true;
	}
	
	private int getRewardPoints(Attraction attraction, User user) {
		return rewardsCentral.getAttractionRewardPoints(attraction.attractionId, user.getUserId());
	}
	
	public double getDistance(Location loc1, Location loc2) {
        double lat1 = Math.toRadians(loc1.latitude);
        double lon1 = Math.toRadians(loc1.longitude);
        double lat2 = Math.toRadians(loc2.latitude);
        double lon2 = Math.toRadians(loc2.longitude);

        double angle = Math.acos(Math.sin(lat1) * Math.sin(lat2)
                               + Math.cos(lat1) * Math.cos(lat2) * Math.cos(lon1 - lon2));

        double nauticalMiles = 60 * Math.toDegrees(angle);
        double statuteMiles = STATUTE_MILES_PER_NAUTICAL_MILE * nauticalMiles;
        return statuteMiles;
	}

}

package tourGuide.service;

import gpsUtil.GpsUtil;
import gpsUtil.location.Attraction;
import gpsUtil.location.Location;
import gpsUtil.location.VisitedLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import rewardCentral.RewardCentral;
import tourGuide.dto.AttractionDto;
import tourGuide.dto.NearbyAttractionDto;
import tourGuide.exceptions.NoDealOrRewardException;
import tourGuide.user.User;
import tourGuide.user.UserReward;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class RewardsService {

	private final Logger LOGGER = LoggerFactory.getLogger(RewardsService.class);

	private final GpsUtil gpsUtil;
	private final RewardCentral rewardsCentral;
	private final ExecutorService rewardsExecutorService;

    private static final double STATUTE_MILES_PER_NAUTICAL_MILE = 1.15077945;
	// proximity in miles
    private final int ATTRACTION_PROXIMITY_RANGE = 200;
	private int proximityBuffer;
	
	public RewardsService(GpsUtil gpsUtil, RewardCentral rewardCentral, @Qualifier("fixedRewardsThreadPool") ExecutorService rewardsExecutorService) {
		this.gpsUtil = gpsUtil;
		this.rewardsCentral = rewardCentral;
		this.rewardsExecutorService = rewardsExecutorService;
		this.proximityBuffer = 10;
	}
	
	public void setProximityBuffer(int proximityBuffer) {
		this.proximityBuffer = proximityBuffer;
	}

	public List<UserReward> getUserRewards(User user) throws ExecutionException, InterruptedException, NoDealOrRewardException {
		List<UserReward> userRewards = calculateRewards(user).get();
		if (!userRewards.isEmpty()){
			LOGGER.info("user rewards retrieved");
			return userRewards;
		}
		LOGGER.debug("No rewards found");
		throw new NoDealOrRewardException("Rewards record empty");
	}

	public Future<List<UserReward>> calculateRewards(User user) {
		return rewardsExecutorService.submit(() -> {
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
			return user.getUserRewards();
		});
	}

	public NearbyAttractionDto getNearByAttractions(VisitedLocation visitedLocation) {
		Location userLocation = visitedLocation.location;
		List<Attraction> fiveNearestAttractions = gpsUtil.getAttractions().stream()
				.sorted(Comparator.comparingDouble(attraction -> getDistance(attraction, userLocation)))
				.limit(5)
				.collect(Collectors.toList());
		if (!fiveNearestAttractions.isEmpty()) {
			List<AttractionDto> attractions = fiveNearestAttractions.stream()
					.map(attraction -> new AttractionDto(attraction, getDistance(attraction, userLocation),
							rewardsCentral.getAttractionRewardPoints(attraction.attractionId, visitedLocation.userId)))
					.collect(Collectors.toList());
			LOGGER.info(fiveNearestAttractions.size() +" nearest attraction(s) retrieved");
			return new NearbyAttractionDto(userLocation, attractions);
		}
		LOGGER.error("Cannot return the nearest attractions");
		return new NearbyAttractionDto(visitedLocation.location);
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
		return getDistance(attraction, location) > ATTRACTION_PROXIMITY_RANGE ? false : true;
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
		return STATUTE_MILES_PER_NAUTICAL_MILE * nauticalMiles;
	}

}

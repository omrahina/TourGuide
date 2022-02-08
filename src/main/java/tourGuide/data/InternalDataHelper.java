package tourGuide.data;

import gpsUtil.location.Location;
import gpsUtil.location.VisitedLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tourGuide.user.User;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.stream.IntStream;

public class InternalDataHelper {

	private static final Logger LOGGER = LoggerFactory.getLogger(InternalDataHelper.class);
	private static final Map<String, User> INTERNAL_USER_MAP = new HashMap<>();
	private static int internalUserNumber = 100;

	static {
		initializeInternalUsers();
	}
	public static Map<String, User> getInternalUserMap() {
		return INTERNAL_USER_MAP;
	}

	public static void addInternalUser(User user) {
		INTERNAL_USER_MAP.put(user.getUserName(), user);
	}

	public static void setInternalUserNumber(int internalUserNumber) {
		InternalDataHelper.internalUserNumber = internalUserNumber;
	}
	
	public static int getInternalUserNumber() {
		return internalUserNumber;
	}

	public static void initializeInternalUsers() {
		LOGGER.info("Initializing users");
		INTERNAL_USER_MAP.clear();
		IntStream.range(0, InternalDataHelper.getInternalUserNumber()).forEach(i -> {
			String userName = "internalUser" + i;
			String phone = "000";
			String email = userName + "@tourGuide.com";
			User user = new User(UUID.randomUUID(), userName, phone, email);
			generateUserLocationHistory(user);

			INTERNAL_USER_MAP.put(userName, user);
		});
		LOGGER.debug("Created {} internal test users.", getInternalUserNumber());
		LOGGER.debug("Finished initializing users");
	}


	private static void generateUserLocationHistory(User user) {
		IntStream.range(0, 3).forEach(i-> {
			user.addToVisitedLocations(new VisitedLocation(user.getUserId(), new Location(generateRandomLatitude(), generateRandomLongitude()), getRandomTime()));
		});
	}

	private static double generateRandomLongitude() {
		double leftLimit = -180;
		double rightLimit = 180;
		return leftLimit + new Random().nextDouble() * (rightLimit - leftLimit);
	}

	private static double generateRandomLatitude() {
		double leftLimit = -85.05112878;
		double rightLimit = 85.05112878;
		return leftLimit + new Random().nextDouble() * (rightLimit - leftLimit);
	}

	private static Date getRandomTime() {
		LocalDateTime localDateTime = LocalDateTime.now().minusDays(new Random().nextInt(30));
		return Date.from(localDateTime.toInstant(ZoneOffset.UTC));
	}
}

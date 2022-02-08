package tourGuide.controller;

import java.util.List;
import java.util.concurrent.ExecutionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import gpsUtil.location.VisitedLocation;
import org.springframework.web.server.ResponseStatusException;
import tourGuide.dto.LocationHistoryDto;
import tourGuide.dto.NearbyAttractionDto;
import tourGuide.exceptions.NoDealOrRewardException;
import tourGuide.exceptions.UserLocationException;
import tourGuide.exceptions.UserNotFoundException;
import tourGuide.service.InternalUserService;
import tourGuide.service.RewardsService;
import tourGuide.service.TourGuideService;
import tourGuide.service.TrackerService;
import tourGuide.user.User;
import tourGuide.user.UserReward;
import tripPricer.Provider;

@RestController
public class TourGuideController {

	@Autowired
	private TourGuideService tourGuideService;

	@Autowired
    private TrackerService trackerService;

	@Autowired
    private RewardsService rewardsService;

	@Autowired
    private InternalUserService internalUserService;

    private final Logger LOGGER = LoggerFactory.getLogger(TourGuideController.class);


    @RequestMapping("/")
    public String index() {
        return "Greetings from TourGuide!";
    }
    
    @RequestMapping("/getLocation") 
    public ResponseEntity<VisitedLocation> getLocation(@RequestParam String userName) {
        VisitedLocation visitedLocation = null;
        try {
            visitedLocation = trackerService.getUserLocation(getUser(userName));
        } catch (UserNotFoundException e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Please verify the username", e);
        } catch (UserLocationException e) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR, "Please retry", e);
        }
        LOGGER.info("User location detected");
        return new ResponseEntity<>(visitedLocation, HttpStatus.OK);
    }

    @RequestMapping("/getNearbyAttractions") 
    public ResponseEntity<NearbyAttractionDto> getNearbyAttractions(@RequestParam String userName) {
        try {
            VisitedLocation visitedLocation = trackerService.getUserLocation(getUser(userName));
            NearbyAttractionDto nearbyAttractionDto = rewardsService.getNearByAttractions(visitedLocation);
            if (!nearbyAttractionDto.getNearbyAttractions().isEmpty()) {
                return new ResponseEntity<>(nearbyAttractionDto, HttpStatus.OK);
            }
            return new ResponseEntity<>(nearbyAttractionDto, HttpStatus.NOT_FOUND);
        } catch (UserNotFoundException e){
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Please verify the username", e);
        }
    }
    
    @RequestMapping("/getRewards") 
    public ResponseEntity<List<UserReward>> getRewards(@RequestParam String userName) {
        try {
            User user = getUser(userName);
            List<UserReward> userRewards = rewardsService.getUserRewards(user);
            return new ResponseEntity<>(userRewards, HttpStatus.OK);
        } catch (UserNotFoundException e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Please verify the username", e);
        } catch (InterruptedException | ExecutionException e) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR, "An error occurred! Please retry", e);
        } catch (NoDealOrRewardException e) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, e.getMessage(), e);
        }
    }
    
    @RequestMapping("/getAllCurrentLocations")
    public ResponseEntity<List<LocationHistoryDto>> getAllCurrentLocations() {
        List<User> users = internalUserService.getAllUsers();
        List<LocationHistoryDto> locationHistory = trackerService.getAllKnownLocations(users);
        if (locationHistory != null) {
            return new ResponseEntity<>(locationHistory, HttpStatus.OK);
        }
    	
    	throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Location history empty");
    }
    
    @RequestMapping("/getTripDeals")
    public ResponseEntity<List<Provider>> getTripDeals(@RequestParam String userName) {
        try {
            List<Provider> providers = tourGuideService.getTripDeals(getUser(userName));
            return new ResponseEntity<>(providers, HttpStatus.OK);
        } catch (UserNotFoundException e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Please verify the username", e);
        } catch (NoDealOrRewardException e) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, e.getMessage(), e);
        }
    }
    
    private User getUser(String userName) {
    	return internalUserService.findUserByUserName(userName);
    }
   

}
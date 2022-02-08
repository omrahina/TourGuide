package tourGuide.dto;

import gpsUtil.location.Location;
import gpsUtil.location.VisitedLocation;

import java.util.List;

public class NearbyAttractionDto {

    Location userLocation;
    List<AttractionDto> nearbyAttractions;

    public NearbyAttractionDto(Location userLocation) {
        this.userLocation = userLocation;
    }

    public NearbyAttractionDto(Location userLocation, List<AttractionDto> nearbyAttractions) {
        this.userLocation = userLocation;
        this.nearbyAttractions = nearbyAttractions;
    }

    public Location getUserLocation() {
        return userLocation;
    }

    public void setUserLocation(Location userLocation) {
        this.userLocation = userLocation;
    }

    public List<AttractionDto> getNearbyAttractions() {
        return nearbyAttractions;
    }

    public void setNearbyAttractions(List<AttractionDto> nearbyAttractions) {
        this.nearbyAttractions = nearbyAttractions;
    }
}

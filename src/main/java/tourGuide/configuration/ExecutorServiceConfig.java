package tourGuide.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Configuration
public class ExecutorServiceConfig {

    @Value("${tracker.nbThreads}")
    private int numberOfTrackerThreads;

    @Value("${rewards.nbThreads}")
    private int numberOfRewardsThreads;

    @Bean("fixedTrackerThreadPool")
    public ExecutorService fixedTrackerThreadPool() {
        return Executors.newFixedThreadPool(numberOfTrackerThreads);
    }

    @Bean("fixedRewardsThreadPool")
    public ExecutorService fixedRewardsThreadPool() {
        return Executors.newFixedThreadPool(numberOfRewardsThreads);
    }

}

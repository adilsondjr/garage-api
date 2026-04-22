package br.com.teste.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import br.com.teste.service.GarageConfigService;

@Component
public class DataInitializer {
    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

    private final GarageConfigService garageConfigService;
    private final Environment environment;

    public DataInitializer(GarageConfigService garageConfigService, Environment environment) {
        this.garageConfigService = garageConfigService;
        this.environment = environment;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void initializeGarageConfig() {
        String[] activeProfiles = environment.getActiveProfiles();
        if (isTestEnvironment(activeProfiles)) {
            logger.info("Test environment detected. Skipping garage configuration initialization.");
            return;
        }

        logger.info("Starting garage configuration initialization...");
        int maxRetries = 30;
        int retryCount = 0;
        Exception lastException = null;

        while (retryCount < maxRetries) {
            retryCount++;
            try {
                logger.info("Attempt {}/{}: Fetching garage configuration from simulator...", retryCount, maxRetries);
                garageConfigService.initializeFromSimulator();
                logger.info("Garage configuration initialization completed successfully");
                return;
            } catch (Exception ex) {
                lastException = ex;
                if (retryCount < maxRetries) {
                    logger.warn("Attempt {}/{} failed. Error: {}. Will retry in 3 seconds...",
                        retryCount, maxRetries, ex.getMessage());
                    try {
                        Thread.sleep(3000);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        logger.warn("Initialization interrupted", ie);
                        break;
                    }
                } else {
                    logger.error("Final exception after {} attempts", maxRetries, ex);
                }
            }
        }

        logger.warn("Failed to initialize garage configuration from simulator after {} attempts. " +
            "The simulator may not be responding correctly. Last error: {}. Application will continue without initial config.",
            maxRetries, lastException != null ? lastException.getMessage() : "Unknown error");
    }

    private boolean isTestEnvironment(String[] activeProfiles) {
        for (String profile : activeProfiles) {
            if ("test".equalsIgnoreCase(profile)) {
                return true;
            }
        }
        return false;
    }
}

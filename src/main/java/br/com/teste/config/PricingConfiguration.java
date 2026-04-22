package br.com.teste.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "pricing")
public class PricingConfiguration {
    private double lowOccupancyThreshold = 0.25;
    private double mediumOccupancyThreshold = 0.5;
    private double highOccupancyThreshold = 0.75;
    private double lowOccupancyMultiplier = 0.9;
    private double mediumOccupancyMultiplier = 1.0;
    private double highOccupancyMultiplier = 1.1;
    private double fullOccupancyMultiplier = 1.25;
    private long freeParsingMinutes = 30;

    public double getLowOccupancyThreshold() {
        return lowOccupancyThreshold;
    }

    public void setLowOccupancyThreshold(double lowOccupancyThreshold) {
        this.lowOccupancyThreshold = lowOccupancyThreshold;
    }

    public double getMediumOccupancyThreshold() {
        return mediumOccupancyThreshold;
    }

    public void setMediumOccupancyThreshold(double mediumOccupancyThreshold) {
        this.mediumOccupancyThreshold = mediumOccupancyThreshold;
    }

    public double getHighOccupancyThreshold() {
        return highOccupancyThreshold;
    }

    public void setHighOccupancyThreshold(double highOccupancyThreshold) {
        this.highOccupancyThreshold = highOccupancyThreshold;
    }

    public double getLowOccupancyMultiplier() {
        return lowOccupancyMultiplier;
    }

    public void setLowOccupancyMultiplier(double lowOccupancyMultiplier) {
        this.lowOccupancyMultiplier = lowOccupancyMultiplier;
    }

    public double getMediumOccupancyMultiplier() {
        return mediumOccupancyMultiplier;
    }

    public void setMediumOccupancyMultiplier(double mediumOccupancyMultiplier) {
        this.mediumOccupancyMultiplier = mediumOccupancyMultiplier;
    }

    public double getHighOccupancyMultiplier() {
        return highOccupancyMultiplier;
    }

    public void setHighOccupancyMultiplier(double highOccupancyMultiplier) {
        this.highOccupancyMultiplier = highOccupancyMultiplier;
    }

    public double getFullOccupancyMultiplier() {
        return fullOccupancyMultiplier;
    }

    public void setFullOccupancyMultiplier(double fullOccupancyMultiplier) {
        this.fullOccupancyMultiplier = fullOccupancyMultiplier;
    }

    public long getFreeParsingMinutes() {
        return freeParsingMinutes;
    }

    public void setFreeParsingMinutes(long freeParsingMinutes) {
        this.freeParsingMinutes = freeParsingMinutes;
    }
}

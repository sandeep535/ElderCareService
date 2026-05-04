package com.eldercare.service.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import lombok.Getter;
import lombok.Setter;

@Configuration
@ConfigurationProperties(prefix = "vitals.threshold")
@Getter
@Setter
public class VitalThresholdConfig {

    private int systolicMin;
    private int systolicMax;
    private int diastolicMin;
    private int diastolicMax;
    private int hrMin;
    private int hrMax;
    private double tempMin;
    private double tempMax;
    private int spo2Min;
}

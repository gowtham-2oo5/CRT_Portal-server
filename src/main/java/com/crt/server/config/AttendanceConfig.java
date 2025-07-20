package com.crt.server.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "attendance")
@Data
public class AttendanceConfig {
    /**
     * Flag to control whether attendance can be submitted after the end time of a time slot
     */
    private boolean enforceEndTimeRestriction = true;
}

package com.crt.server.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "attendance")
@Data
public class AttendanceConfig {
    private boolean enforceEndTimeRestriction = true;
}

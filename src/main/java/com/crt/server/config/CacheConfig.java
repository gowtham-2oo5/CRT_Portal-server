package com.crt.server.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.github.benmanes.caffeine.cache.Caffeine;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();
        
        // Configure default cache settings
        cacheManager.setCaffeine(Caffeine.newBuilder()
            .expireAfterWrite(30, TimeUnit.MINUTES)
            .maximumSize(1000)
            .recordStats());
        
        // Set up cache names
        cacheManager.setCacheNames(Arrays.asList(
            "facultyTimeSlots", 
            "sectionDetails", 
            "facultyTimetable",
            "userDetails",
            "studentsBySection",
            "attendanceByTimeSlot",
            "attendanceByStudent",
            "sectionAttendanceRecords"
        ));
        
        return cacheManager;
    }
}

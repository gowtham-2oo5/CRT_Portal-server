package com.crt.server.security;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RateLimitService {
    
    private final ConcurrentHashMap<String, Bucket> buckets = new ConcurrentHashMap<>();
    
    public boolean isAllowed(String key, int requests, Duration duration) {
        Bucket bucket = buckets.computeIfAbsent(key, k -> createBucket(requests, duration));
        return bucket.tryConsume(1);
    }
    
    private Bucket createBucket(int requests, Duration duration) {
        Bandwidth limit = Bandwidth.classic(requests, Refill.intervally(requests, duration));
        return Bucket.builder().addLimit(limit).build();
    }
}

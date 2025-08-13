package com.crt.server.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/cache")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasRole('ADMIN')")
public class CacheController {

    private final CacheManager cacheManager;

    @GetMapping("/info")
    public ResponseEntity<Map<String, Object>> getCacheInfo() {
        Map<String, Object> cacheInfo = new HashMap<>();
        
        cacheInfo.put("cacheManagerType", cacheManager.getClass().getSimpleName());
        cacheInfo.put("cacheNames", cacheManager.getCacheNames());
        
        Map<String, Object> cacheStats = new HashMap<>();
        for (String cacheName : cacheManager.getCacheNames()) {
            var cache = cacheManager.getCache(cacheName);
            if (cache != null) {
                Map<String, Object> stats = new HashMap<>();
                stats.put("exists", true);
                
                // If it's a Caffeine cache, get detailed stats
                if (cache instanceof CaffeineCache) {
                    CaffeineCache caffeineCache = (CaffeineCache) cache;
                    var nativeCache = caffeineCache.getNativeCache();
                    var cacheStats2 = nativeCache.stats();
                    
                    stats.put("hitCount", cacheStats2.hitCount());
                    stats.put("missCount", cacheStats2.missCount());
                    stats.put("hitRate", cacheStats2.hitRate());
                    stats.put("evictionCount", cacheStats2.evictionCount());
                    stats.put("estimatedSize", nativeCache.estimatedSize());
                }
                
                cacheStats.put(cacheName, stats);
            } else {
                cacheStats.put(cacheName, Map.of("exists", false));
            }
        }
        
        cacheInfo.put("cacheStats", cacheStats);
        
        return ResponseEntity.ok(cacheInfo);
    }

    @PostMapping("/clear/{cacheName}")
    public ResponseEntity<String> clearCache(@PathVariable String cacheName) {
        var cache = cacheManager.getCache(cacheName);
        if (cache != null) {
            cache.clear();
            log.info("Cache '{}' cleared successfully", cacheName);
            return ResponseEntity.ok("Cache '" + cacheName + "' cleared successfully");
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/clear-all")
    public ResponseEntity<String> clearAllCaches() {
        for (String cacheName : cacheManager.getCacheNames()) {
            var cache = cacheManager.getCache(cacheName);
            if (cache != null) {
                cache.clear();
            }
        }
        log.info("All caches cleared successfully");
        return ResponseEntity.ok("All caches cleared successfully");
    }
}

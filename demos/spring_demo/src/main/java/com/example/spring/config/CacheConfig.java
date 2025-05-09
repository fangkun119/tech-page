package com.example.spring.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.github.benmanes.caffeine.cache.Caffeine;
import java.util.concurrent.TimeUnit;

@Configuration
@EnableCaching
public class CacheConfig {
    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager(CacheNames.GREETINGS_CACHE);
        cacheManager.setCaffeine(
                Caffeine.newBuilder()
                        .initialCapacity(100)
                        .maximumSize(1000)
                        .expireAfterWrite(10, TimeUnit.MINUTES)
                        .expireAfterAccess(5, TimeUnit.MINUTES)
                        .recordStats()
                        .removalListener((key, value, cause) -> System.out.println("Key removed: " + key))
        );
        return cacheManager;
    }
}



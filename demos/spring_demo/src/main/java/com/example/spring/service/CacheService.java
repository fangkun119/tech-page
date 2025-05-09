package com.example.spring.service;
import com.github.benmanes.caffeine.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Service
public class CacheService {

    private final CacheManager cacheManager;

    public CacheService(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    @SuppressWarnings("unchecked")
    public Map<String, String> getAllCacheStat() {
        Map<String, String> cacheStats = new HashMap<>();
        // 遍历所有缓存名称
        for (String cacheName : cacheManager.getCacheNames()) {
            // 获取本地缓存
            Cache<Object, Object> caffeineCache
                    = (Cache<Object, Object>) Objects.requireNonNull(cacheManager.getCache(cacheName)).getNativeCache();
            // 获取缓存统计信息
            long size = caffeineCache.estimatedSize();
            cacheStats.put(cacheName, String.format("size:%d; detail:%s", size, caffeineCache.stats()));
        }
        return cacheStats;
    }
}



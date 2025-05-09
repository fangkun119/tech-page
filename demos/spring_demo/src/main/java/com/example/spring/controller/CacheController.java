package com.example.spring.controller;

import com.example.spring.service.CacheService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;

@RestController
@RequestMapping("/cache")
public class CacheController {

    private final CacheService cacheService;

    public CacheController(@Autowired CacheService cacheService) {
        this.cacheService = cacheService;
    }

    @GetMapping("/stat")
    public Map<String, String> getCacheStat() {
        return cacheService.getAllCacheStat();
    }
}


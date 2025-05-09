package com.example.spring.service;

import com.example.spring.config.CacheNames;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
public class GreetingService {
    // 下面两个方法的调用，会被Cache相互干扰
    @Cacheable(value = CacheNames.GREETINGS_CACHE /*配在CacheConfig中*/, key = "#id")
    public String getGreetingByID(int id) {
        simulateSlowOperation();
        return "Hello, User " + id + "(ID) !";
    }

    @Cacheable(value = CacheNames.GREETINGS_CACHE, key = "#code")
    public String getGreetingBySecretCode(int code) {
        simulateSlowOperation();
        return "Hello, User " + code + "(Secret Code) !";
    }

    // 下面两个方法不会，不会被Cache相互干扰
    @Cacheable(value = CacheNames.GREETINGS_CACHE, keyGenerator = "firstNameKeyGenerator")
    public String getGreetingByFirstName(String firstName) {
        simulateSlowOperation();
        return "Hello, " + firstName + "(First Name)!";
    }

    @Cacheable(value = CacheNames.GREETINGS_CACHE, keyGenerator = "lastNameKeyGenerator")
    public String getGreetingByLastName(String lastName) {
        simulateSlowOperation();
        return "Hello, " + lastName + "(Last Name)!";
    }

    // 模拟慢速操作
    private void simulateSlowOperation() {
        try {
            Thread.sleep(2000L); // Sleep for 2 seconds
        } catch (InterruptedException e) {
            System.out.println("interrupted");
        }
    }
}


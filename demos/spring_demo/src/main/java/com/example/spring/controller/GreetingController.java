package com.example.spring.controller;

import com.example.spring.service.GreetingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/greeting")
public class GreetingController {
    private final GreetingService greetingService;

    @Autowired
    public GreetingController(GreetingService greetingService) {
        this.greetingService = greetingService;
    }

    @GetMapping("/id/{id}")
    public String getGreetingById(@PathVariable int id) {
        return greetingService.getGreetingByID(id);
    }

    @GetMapping("/code/{code}")
    public String getGreetingBySecretCode(@PathVariable int code) {
        return greetingService.getGreetingBySecretCode(code);
    }

    @GetMapping("/first_name/{firstName}")
    public String getGreetingByFirstName(@PathVariable String firstName) {
        return greetingService.getGreetingByFirstName(firstName);
    }

    @GetMapping("/last_name/{lastName}")
    public String getGreetingByLastName(@PathVariable String lastName) {
        return greetingService.getGreetingByLastName(lastName);
    }
}


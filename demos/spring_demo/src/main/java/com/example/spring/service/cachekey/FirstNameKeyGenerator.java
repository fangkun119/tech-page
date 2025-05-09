package com.example.spring.service.cachekey;

import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;

@Component("firstNameKeyGenerator")
public class FirstNameKeyGenerator implements KeyGenerator {
    @SuppressWarnings("NullableProblems")
    @Override
    public Object generate(Object target, Method method, Object... params) {
        // target: com.example.spring.service.GreetingService
        // method: getGreetingByFirstName
        // params: [${param}]
        return "FirstName:" + params[0];
    }
}

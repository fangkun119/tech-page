package com.example.spring.service.cachekey;

import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

@Component("lastNameKeyGenerator")
public class LastNameKeyGenerator implements KeyGenerator {
    @SuppressWarnings("NullableProblems")
    @Override
    public Object generate(Object target, Method method, Object... params) {
        return "LastName:" + params[0];
    }
}

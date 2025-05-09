package com.demo.internal.spring.user;

import com.demo.internal.spring.framework.DemoApplicationContext;
import com.demo.internal.spring.user.service.UserService;


// 搭建应用代码，模拟从Context获取Bean并调用它的方法
public class MyApplication {

    public static void main(String[] args) {
        DemoApplicationContext applicationContext = new DemoApplicationContext(AppConfig.class);
        UserService userService = (UserService) applicationContext.getBean("userService");
        userService.test();
    }
}

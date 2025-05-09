package com.demo.internal.spring.user.service;

import com.demo.internal.spring.framework.*;
import com.demo.springframework.*;

@Component
@Transactional
public class UserService implements BeanNameAware, ApplicationContextAware {

    @Autowired
    private OrderService orderService;

    private DemoApplicationContext applicationContext;
    private String beanName;

    public void test(){
        System.out.println(orderService);
        System.out.println(applicationContext);
        System.out.println(beanName);
    }

    @Override
    public void setApplicationContext(DemoApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public void setBeanName(String name) {
        this.beanName = name;
    }
}

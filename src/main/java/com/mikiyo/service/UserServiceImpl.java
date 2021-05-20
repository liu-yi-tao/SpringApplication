package com.mikiyo.service;

import com.spring.Autowired;
import com.spring.Component;

/**
 * @author Mikiyo
 * @create 2021-05-19 10:09
 */
@Component("userService")
//@Scope("prototype")
public class UserServiceImpl implements UserService {

    @Autowired
    private OrderService orderService;

    private String name;

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public void test() {
        System.out.println(orderService);
        System.out.println(name);
    }

}

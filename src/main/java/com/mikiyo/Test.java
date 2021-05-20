package com.mikiyo;

import com.spring.SpringApplicationContext;

/**
 * @author Mikiyo
 * @create 2021-05-19 10:31
 */
public class Test {

    public static void main(String[] args) {
        SpringApplicationContext applicationContext = new SpringApplicationContext(AppConfig.class);

        System.out.println(applicationContext.getBean("userService"));
        System.out.println(applicationContext.getBean("userService"));
        System.out.println(applicationContext.getBean("userService"));

    }


}

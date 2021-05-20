package com.spring;

/**
 * @author Mikiyo
 * @create 2021-05-20 10:58
 */
public interface InitializingBean {

    void afterPropertiesSet() throws Exception;

}

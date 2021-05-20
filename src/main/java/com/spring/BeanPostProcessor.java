package com.spring;

/**
 * @author Mikiyo
 * @create 2021-05-20 11:12
 */
public interface BeanPostProcessor {

    Object postProcessBeforeInitialization(Object bean, String beanName);

    Object postProcessAfterInitialization(Object bean, String beanName);

}

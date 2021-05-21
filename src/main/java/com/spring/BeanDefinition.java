package com.spring;

/**
 * @author Mikiyo
 * @create 2021-05-19 15:00
 */
public class BeanDefinition {

    // 类
    private Class clazz;

    // 类标识
    private String scope;

    public void setClazz(Class clazz) {
        this.clazz = clazz;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public Class getClazz() {
        return clazz;
    }

    public String getScope() {
        return scope;
    }

    @Override
    public String toString() {
        return "BeanDefinition{" +
                "clazz=" + clazz +
                ", scope='" + scope + '\'' +
                '}';
    }
}

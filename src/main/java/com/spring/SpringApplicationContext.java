package com.spring;

import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.net.FileNameMap;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Mikiyo
 * @create 2021-05-19 9:43
 */
public class SpringApplicationContext {

    private Class configClass;

    // 单例池
    private ConcurrentHashMap<String, Object> singletonObjects = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, BeanDefinition> beanDefinitionMap = new ConcurrentHashMap<>();

    public SpringApplicationContext(Class configClass) {

        this.configClass = configClass;

        // 解析配置类
        // ComponentScan注解  ---> 扫描路径  ---> 扫描 ---> BeanDefinition ---> BeanDefinitionMap
        scan(configClass);

        for (Map.Entry<String, BeanDefinition> entry : beanDefinitionMap.entrySet()) {
            String beanName = entry.getKey();
            BeanDefinition beanDefinition = entry.getValue();
            if ("singleton".equals(beanDefinition.getScope())) {
                // 单例 bean
                Object bean = createBean(beanDefinition);
                singletonObjects.put(beanName, bean);
            }
        }

    }

    private Object createBean(BeanDefinition beanDefinition) {
        Class clazz = beanDefinition.getClazz();
        try {
            Object instance = clazz.getDeclaredConstructor().newInstance();

            return instance;
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void scan(Class configClass) {
        ComponentScan componentScanAnnotation = (ComponentScan) configClass.getDeclaredAnnotation(ComponentScan.class);
        //        // 扫描路径
        String path = componentScanAnnotation.value();
        path = path.replace(".", "/");
        // 扫描
        // Bootstrap ---> jre/lib
        // Ext ---------> jre/ext/lib
        // App ---------> classpath
        ClassLoader classLoader = this.getClass().getClassLoader();

        URL resource = classLoader.getResource(path);

        File file = new File(resource.getFile());
        if (file.isDirectory()) {

            File[] files = file.listFiles();
            for (File f : files) {
                String fileName = f.getAbsolutePath();
                if (fileName.endsWith(".class")) {
                    String className = fileName.substring(fileName.indexOf("com"), fileName.indexOf(".class"));
                    className = className.replace("\\", ".");

                    try {
                        Class<?> clazz = classLoader.loadClass(className);
                        if (clazz.isAnnotationPresent(Component.class)) {
                            // 表示当前这个类是一个bean
                            // 解析类，判断当前bean是单例bean，还是prototype的bean 解析类 ---> beanDefinition

                            Component componentAnnotation = clazz.getDeclaredAnnotation(Component.class);
                            String beanName = componentAnnotation.value();

                            BeanDefinition beanDefinition = new BeanDefinition();
                            beanDefinition.setClazz(clazz);
                            if (clazz.isAnnotationPresent(Scope.class)) {
                                Scope scopeAnnotation = clazz.getDeclaredAnnotation(Scope.class);
                                beanDefinition.setScope(scopeAnnotation.value());
                            } else {
                                beanDefinition.setScope("singleton");
                            }

                            beanDefinitionMap.put(beanName, beanDefinition);
                        }
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                }
            }

        }
    }

    public Object getBean(String beanName) {
        if (beanDefinitionMap.containsKey(beanName)) {
            BeanDefinition beanDefinition = beanDefinitionMap.get(beanName);
            if ("singleton".equals(beanDefinition.getScope())) {
                Object o = singletonObjects.get(beanName);
                return o;
            } else {
                // 创建 bean 对象
                Object bean = createBean(beanDefinition);
                return bean;
            }
        } else {
            // 不存在对应的bean
            throw new NullPointerException();
        }
    }

}

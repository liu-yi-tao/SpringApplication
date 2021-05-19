package com.spring;

import java.io.File;
import java.lang.annotation.Annotation;
import java.net.FileNameMap;
import java.net.URL;

/**
 * @author Mikiyo
 * @create 2021-05-19 9:43
 */
public class SpringApplicationContext {

    private Class configClass;

    public SpringApplicationContext(Class configClass) {

        this.configClass = configClass;

        // 解析配置类
        // ComponentScan注解  ---> 扫描路径  ---> 扫描
        ComponentScan componentScanAnnotation = (ComponentScan) configClass.getDeclaredAnnotation(ComponentScan.class);
        //        // 扫描路径
        String path = componentScanAnnotation.value();
        path.replace(".", "/");
        // 扫描
        // Bootstrap ---> jre/lib
        // Ext ---------> jre/ext/lib
        // App ---------> classpath
        ClassLoader classLoader = SpringApplicationContext.class.getClassLoader();
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

                        }
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                }
            }

        }

    }

    public Object getBean(String beanName) {
        return null;
    }

}

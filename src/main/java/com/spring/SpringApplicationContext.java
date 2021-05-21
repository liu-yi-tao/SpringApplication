package com.spring;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
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

    // 所有扫描类信息
    private ConcurrentHashMap<String, BeanDefinition> beanDefinitionMap = new ConcurrentHashMap<>();

    // 存放初始化前后执行方式对象
    private List<BeanPostProcessor> beanPostProcessorList = new ArrayList<>();

    public SpringApplicationContext(Class configClass) {

        this.configClass = configClass;

        // 解析配置类
        // ComponentScan注解  ---> 扫描路径  ---> 扫描 ---> BeanDefinition ---> BeanDefinitionMap
        scan(configClass);


        for (Map.Entry<String, BeanDefinition> entry : beanDefinitionMap.entrySet()) {
            //
            String beanName = entry.getKey();
            BeanDefinition beanDefinition = entry.getValue();
            if ("singleton".equals(beanDefinition.getScope())) {
                // 单例 bean
                Object bean = createBean(beanName, beanDefinition);
                singletonObjects.put(beanName, bean);
            }
        }

    }

    // 创建bean对象
    private Object createBean(String beanName, BeanDefinition beanDefinition) {
        // 获取对象类
        Class clazz = beanDefinition.getClazz();

        try {
            // 实例化类
            Object instance = clazz.getDeclaredConstructor().newInstance();

            // 依赖注入 判断对象是否有属性
            for (Field declaredField : clazz.getDeclaredFields()) {
                // 判断属性是否标有 Autowired 自动注入注解
                if (declaredField.isAnnotationPresent(Autowired.class)) {
                    // 根据成员变量名获取 bean 对象
                    Object bean = getBean(declaredField.getName());
                    // 反射获取设置私有变量的值，在访问时忽略访问修饰符检查
                    declaredField.setAccessible(true);
                    // 将属性赋值
                    declaredField.set(instance, bean);
                }
            }

            // Aware 回调
            if (instance instanceof BeanNameAware) {
                // 执行 Awared 回调方法
                ((BeanNameAware) instance).setBeanName(beanName);
            }

            // 初始化前
            for (BeanPostProcessor beanPostProcessor : beanPostProcessorList) {
                // 执行初始化前方法
                instance = beanPostProcessor.postProcessBeforeInitialization(instance, beanName);
            }

            // 初始化
            // 判断扫描的对象是否实现 初始化对象
            if (instance instanceof InitializingBean) {
                try {
                    // 执行初始化方法
                    ((InitializingBean) instance).afterPropertiesSet();
                } catch (Exception e) {

                }
            }

            // 初始化后
            for (BeanPostProcessor beanPostProcessor : beanPostProcessorList) {
                // 执行初始化后方法
                instance = beanPostProcessor.postProcessAfterInitialization(instance, beanName);
            }

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

    // 扫描函数
    private void scan(Class configClass) {
        ComponentScan componentScanAnnotation = (ComponentScan) configClass.getDeclaredAnnotation(ComponentScan.class);
        // 扫描路径
        String path = componentScanAnnotation.value();
        path = path.replace(".", "/");
        //        // 扫描
        // Bootstrap ---> jre/lib
        // Ext ---------> jre/ext/lib
        // App ---------> classpath
        // 类加载器
        ClassLoader classLoader = this.getClass().getClassLoader();

        // 获取扫描路径
        URL resource = classLoader.getResource(path);

        // 获取扫描的所有文件
        File file = new File(resource.getFile());
        // 判断是否未文件夹
        if (file.isDirectory()) {
            // 获取文件夹下所有文件
            File[] files = file.listFiles();
            for (File f : files) {
                // 获取文件名
                String fileName = f.getAbsolutePath();
                // 判断是否为 class 文件
                if (fileName.endsWith(".class")) {
                    // 获取类的路径包名
                    String className = fileName.substring(fileName.indexOf("com"), fileName.indexOf(".class"));
                    // 将 com\exe\service\service\xxx
                    className = className.replace("\\", ".");

                    try {
                        // 获取类
                        Class<?> clazz = classLoader.loadClass(className);
                        // 判断是否被注入
                        if (clazz.isAnnotationPresent(Component.class)) {
                            // 表示当前这个类是一个bean
                            // 解析类，判断当前bean是单例bean，还是prototype的bean 解析类 ---> beanDefinition

                            //  判断是否 clazz 是否实现 BeanPostProcessor
                            if (BeanPostProcessor.class.isAssignableFrom(clazz)) {
                                // 实例化切面对象
                                BeanPostProcessor instance = (BeanPostProcessor) clazz.getDeclaredConstructor().newInstance();
                                // 将创建出来的切面对象插入到集合
                                beanPostProcessorList.add(instance);
                            }

                            // 获取扫描的注解
                            Component componentAnnotation = clazz.getDeclaredAnnotation(Component.class);
                            // 获取 beanName
                            String beanName = componentAnnotation.value();

                            // 创建 bean 信息对象
                            BeanDefinition beanDefinition = new BeanDefinition();
                            // 将 类 存放值 bean信息对象
                            beanDefinition.setClazz(clazz);
                            // 判断是否加入Scope 标识单例
                            if (clazz.isAnnotationPresent(Scope.class)) {
                                // 获取Scope对象
                                Scope scopeAnnotation = clazz.getDeclaredAnnotation(Scope.class);
                                // 将信息存入 bean 信息对象中
                                beanDefinition.setScope(scopeAnnotation.value());
                            } else {
                                // 否则为单例对象
                                beanDefinition.setScope("singleton");
                            }
                            // bean 信息存放置容器
                            beanDefinitionMap.put(beanName, beanDefinition);
                        }
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    } catch (InstantiationException e) {
                        e.printStackTrace();
                    } catch (NoSuchMethodException e) {
                        e.printStackTrace();
                    } catch (InvocationTargetException e) {
                        e.printStackTrace();
                    }
                }
            }

        }
    }

    // 获取 bean
    public Object getBean(String beanName) {
        // 判断容器中是否存在 bean对象
        if (beanDefinitionMap.containsKey(beanName)) {
            // 获取 bean 信息
            BeanDefinition beanDefinition = beanDefinitionMap.get(beanName);
            // 判断 bean 是否为单例对象
            if ("singleton".equals(beanDefinition.getScope())) {
                Object o = singletonObjects.get(beanName);
                return o;
            } else {
                // 创建 bean 对象
                Object bean = createBean(beanName, beanDefinition);
                return bean;
            }
        } else {
            // 不存在对应的bean
            throw new NullPointerException();
        }
    }

}

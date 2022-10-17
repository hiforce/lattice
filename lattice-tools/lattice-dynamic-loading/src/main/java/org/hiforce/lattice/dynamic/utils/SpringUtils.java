package org.hiforce.lattice.dynamic.utils;

import org.hiforce.lattice.exception.LatticeRuntimeException;
import org.hiforce.lattice.runtime.utils.SpringApplicationContextHolder;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * @author Rocky Yu
 * @since 2022/10/17
 */
public class SpringUtils {

    public static void removeBean(String beanName) {
        ConfigurableApplicationContext applicationContext =
                (ConfigurableApplicationContext) SpringApplicationContextHolder.getContext();
        if (!applicationContext.containsBean(beanName)) {
            return;
        }
        BeanDefinitionRegistry beanFactory = (BeanDefinitionRegistry) applicationContext.getBeanFactory();
        beanFactory.removeBeanDefinition(beanName);
    }


    @SuppressWarnings("ALL")
    public static <T> T registerBean(String beanName, Class<T> clazz, Object... args) {
        ConfigurableApplicationContext applicationContext =
                (ConfigurableApplicationContext) SpringApplicationContextHolder.getContext();
        if (applicationContext.containsBean(beanName)) {
            T bean = (T) applicationContext.getBean(beanName);
            if (bean.getClass().isAssignableFrom(clazz)) {
                return bean;
            } else {
                throw new LatticeRuntimeException("LATTICE-DYNAMIC-0002" + beanName);
            }
        }

        BeanDefinitionBuilder beanDefinitionBuilder = BeanDefinitionBuilder.genericBeanDefinition(clazz);
        for (Object arg : args) {
            beanDefinitionBuilder.addConstructorArgValue(arg);
        }
        BeanDefinition beanDefinition = beanDefinitionBuilder.getRawBeanDefinition();

        BeanDefinitionRegistry beanFactory = (BeanDefinitionRegistry) applicationContext.getBeanFactory();
        beanFactory.registerBeanDefinition(beanName, beanDefinition);

        return applicationContext.getBean(beanName, clazz);
    }
}

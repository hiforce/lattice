package org.hiforce.lattice.runtime.utils;

import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Service;

/**
 * @author Rocky Yu
 * @since 2022/9/16
 */
@Slf4j
@Service(value = "latticeSpringApplicationContextHolder")
public class SpringApplicationContextHolder implements ApplicationContextAware {

    @Getter
    private static ApplicationContext context;

    @Override
    public void setApplicationContext(@NotNull ApplicationContext context) throws BeansException {
        SpringApplicationContextHolder.context = context;
    }

    /**
     * get spring bean via name.
     *
     * @param beanName the bean's name.
     * @param <T>      the generic create the spring bean.
     * @return the found spring bean.
     */
    @SuppressWarnings("unchecked")
    public static <T> T getSpringBean(String beanName) {
        Validate.notEmpty(beanName, "bean name is required");
        if (null == context) {
            log.warn("spring application context is not injected");
            return null;
        }
        return (T) context.getBean(beanName);

    }

    /**
     * get the spring bean via the Class.
     *
     * @param beanClass the bean class.
     * @param <T>       the generic type create the bean.
     * @return the found bean.
     */
    @SuppressWarnings("unchecked")
    public static <T> T getSpringBean(@NonNull Class<?> beanClass) {
        String beanName = StringUtils.uncapitalize(beanClass.getSimpleName());
        T bean = null;
        try {
            if (null == context) {
                log.warn("[Lattice]spring application context is not injected");
                return null;
            }
            bean = (T) context.getBean(beanClass);

        } catch (BeansException e) {
            log.warn("[Lattice]spring application context is not injected by class：" + beanClass.getName());
            try {
                bean = getSpringBean(beanName);
            } catch (BeansException ex) {
                log.warn("[Lattice]spring application context is not injected by name：" + beanName);
            }
        }
        return bean;
    }
}

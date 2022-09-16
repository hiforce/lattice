package org.hiforce.lattice.runtime.utils;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.hiforce.lattice.runtime.spi.LatticeSpiFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;

import static org.springframework.core.annotation.AnnotationUtils.getAnnotation;

/**
 * @author Rocky Yu
 * @since 2022/9/16
 */
@Slf4j
public class LatticeBeanUtils {


    public static void autowireBean(Object object) {
        ApplicationContext context = findApplicationContext(object.getClass().getClassLoader());
        if (context != null) {
            try {
                context.getAutowireCapableBeanFactory().autowireBean(object);
            } catch (Throwable e) {
                log.warn("[Lattice]Failed to autowireBean " + object.getClass().getName(), e);
            }
        }
    }

    public static ApplicationContext findApplicationContext(ClassLoader classLoader) {
        return SpringApplicationContextHolder.getContext();
    }


    public static <T> T getAndCreateSpringBeanViaClass(Class<?> beanClass) {
        if (null == beanClass) {
            return null;
        }
        //尝试从Spring注册的Bean中获取
        try {
            SpringAnnotationResult result = getSpringAnnotationResult(beanClass);
            if (result.isHasAnnotation()) {
                T t = StringUtils.isEmpty(result.getValue())
                        ? SpringApplicationContextHolder.getSpringBean(beanClass)
                        : SpringApplicationContextHolder.getSpringBean(result.getValue());
                if (null != t) {
                    return t;
                }
            }
            T t = SpringApplicationContextHolder
                    .getSpringBean(StringUtils.uncapitalize(beanClass.getSimpleName()));
            if (null != t) {
                return t;
            }
            return createSpringBeanInstance(beanClass);
        } catch (Throwable th) {
            log.warn("[Lattice]Failed to Find SpringBean via name: {}", beanClass.getName());
            return createSpringBeanInstance(beanClass);
        }
    }

    private static <T> T createSpringBeanInstance(@Nonnull Class<?> beanClass) {
        try {
            ApplicationContext context = SpringApplicationContextHolder.getContext();
            if (context != null) {
                return (T) context.getAutowireCapableBeanFactory().createBean(beanClass);
            }
            return (T) beanClass.newInstance();
        } catch (Throwable e) {
            log.error("[Lattice]Failed to create spring bean instance", e);
            return createBeanInstance(beanClass);
        }
    }

    private static <T> T createBeanInstance(@Nonnull Class<?> beanClass) {
        try {
            return (T) beanClass.newInstance();
        } catch (Throwable e) {
            log.error("[Lattice]Failed to create spring bean instance", e);
            return null;
        }
    }

    private static SpringAnnotationResult getSpringAnnotationResult(Class<?> beanClass) {
        Service service = getAnnotation(beanClass, Service.class);
        if (null != service) {
            return new SpringAnnotationResult(true, service.value());
        }
        Repository repository = getAnnotation(beanClass, Repository.class);
        if (null != repository) {
            return new SpringAnnotationResult(true, repository.value());
        }
        Component component = getAnnotation(beanClass, Component.class);
        if (null != component) {
            return new SpringAnnotationResult(true, component.value());
        }
        return new SpringAnnotationResult(false, null);
    }

}

class SpringAnnotationResult {

    @Getter
    private final boolean hasAnnotation;

    @Getter
    private String value;

    public SpringAnnotationResult(boolean hasAnnotation, String value) {
        this.hasAnnotation = hasAnnotation;
        this.value = value;
    }
}


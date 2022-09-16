package org.hiforce.lattice.utils;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;

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


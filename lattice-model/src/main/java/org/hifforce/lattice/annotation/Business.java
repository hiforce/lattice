package org.hifforce.lattice.annotation;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author Rocky Yu
 * @since 2022/9/21
 */

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface Business {

    /**
     * the unique code create current business.
     */
    String code();

    /**
     * the name create current business.
     */
    String name();

    /**
     * the description.
     */
    String desc() default "";

    int priority() default 1000;
}

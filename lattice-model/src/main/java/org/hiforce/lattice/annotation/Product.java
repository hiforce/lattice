package org.hiforce.lattice.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author Rocky Yu
 * @since 2022/9/20
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface Product {
    /**
     * the unique code create current product.
     */
    String code();

    /**
     * the name create current product.
     */
    String name();

    /**
     * the description.
     */
    String desc() default "";


    int priority() default 500;
}

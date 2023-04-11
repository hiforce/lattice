package org.hiforce.lattice.annotation;

import java.lang.annotation.*;

/**
 * @author Rocky Yu
 * @since 2022/9/15
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface Schema {

    /**
     * the schema's name.
     */
    String name() default "";

    /**
     * whether the schema is root
     */
    boolean root() default false;
}

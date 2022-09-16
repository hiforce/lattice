package org.hifforce.lattice.annotation;


import org.hifforce.lattice.annotation.model.ReduceType;

import java.lang.annotation.*;

/**
 * @author Rocky Yu
 * @since 2022/9/15
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Inherited
public @interface Extension {

    String name() default "";

    String code();

    String desc() default "";

    /**
     * @return 扩展点的ReduceType
     */
    ReduceType reduceType() default ReduceType.UNKNOWN;
}

package org.hifforce.lattice.annotation;

import java.lang.annotation.*;

/**
 * @author Rocky Yu
 * @since 2022/9/18
 */
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface Realization {

    /**
     * @return The bizCode realization supported.
     */
    String[] codes();

    /**
     * @return The specific scenario realization server.
     */
    String scenario() default "";
}

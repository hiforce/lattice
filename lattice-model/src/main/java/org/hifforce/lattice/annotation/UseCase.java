package org.hifforce.lattice.annotation;

import org.hifforce.lattice.model.ability.IBusinessExt;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author Rocky Yu
 * @since 2022/9/28
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface UseCase {

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

    /**
     *
     * @return The Open SDK
     */
    Class<? extends IBusinessExt> sdk();


    int priority() default 100;
}

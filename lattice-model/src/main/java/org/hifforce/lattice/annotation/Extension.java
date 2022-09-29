package org.hifforce.lattice.annotation;


import org.hifforce.lattice.annotation.model.ProtocolType;
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

    ReduceType reduceType() default ReduceType.UNKNOWN;

    ProtocolType protocolType() default ProtocolType.LOCAL;
}

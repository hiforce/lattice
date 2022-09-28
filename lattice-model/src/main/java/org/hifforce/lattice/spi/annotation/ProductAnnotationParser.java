package org.hifforce.lattice.spi.annotation;

import java.lang.annotation.Annotation;

/**
 * @author Rocky Yu
 * @since 2022/9/20
 */
public abstract class ProductAnnotationParser<T extends Annotation>
        extends LatticeAnnotationParser<T> {

    public abstract String getCode(T annotation);

    public abstract String getName(T annotation);

    public abstract String getDesc(T annotation);

    public abstract int getPriority(T annotation);
}
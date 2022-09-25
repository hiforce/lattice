package org.hifforce.lattice.spi.annotation;

import java.lang.annotation.Annotation;

/**
 * @author Rocky Yu
 * @since 2022/9/15
 */
public abstract class LatticeAnnotationParser<T extends Annotation> {

    public abstract Class<T> getAnnotationClass();
}

package org.hifforce.lattice.spi.annotation;

import org.hifforce.lattice.annotation.model.ExtensionAnnotation;
import org.hifforce.lattice.annotation.model.ReduceType;

import java.lang.annotation.Annotation;

/**
 * @author Rocky Yu
 * @since 2022/9/16
 */
public abstract class ExtensionAnnotationParser<T extends Annotation> extends LatticeAnnotationParser<T> {

    public abstract String getName(T annotation);

    public abstract String getCode(T annotation);

    public abstract String getDesc(T annotation);

    public abstract ReduceType getReduceType(T annotation);

    public ExtensionAnnotation buildAnnotationInfo(T annotation) {
        if (null == annotation) {
            return null;
        }
        ExtensionAnnotation info = new ExtensionAnnotation();
        info.setName(getName(annotation));
        info.setCode(getCode(annotation));
        info.setDesc(getDesc(annotation));
        info.setReduceType(getReduceType(annotation));
        return info;
    }
}

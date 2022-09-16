package org.hifforce.lattice.annotation.parser;

import org.hifforce.lattice.annotation.model.AbilityAnnotation;

import java.lang.annotation.Annotation;

/**
 * @author Rocky Yu
 * @since 2022/9/15
 */
public abstract class AbilityAnnotationParser<T extends Annotation> extends LatticeAnnotationParser<T, AbilityAnnotation> {

    public abstract String getCode(T annotation);

    public abstract String getName(T annotation);

    public abstract String getDesc(T annotation);

    public abstract String getParent(T annotation);

    public AbilityAnnotation buildAnnotationInfo(T annotation) {
        AbilityAnnotation info = new AbilityAnnotation();
        info.setCode(getCode(annotation));
        info.setName(getName(annotation));
        info.setDesc(getDesc(annotation));
        info.setParent(getParent(annotation));
        return info;
    }
}

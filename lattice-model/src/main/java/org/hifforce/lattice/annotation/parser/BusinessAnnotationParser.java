package org.hifforce.lattice.annotation.parser;

import org.hifforce.lattice.annotation.model.BusinessAnnotation;
import org.hifforce.lattice.model.business.TemplateType;

import java.lang.annotation.Annotation;

/**
 * @author Rocky Yu
 * @since 2022/9/21
 */
public abstract class BusinessAnnotationParser<T extends Annotation>
        extends LatticeAnnotationParser<T> {

    public abstract String getCode(T annotation);

    public abstract String getName(T annotation);

    public abstract String getDesc(T annotation);

    public abstract int getPriority(T annotation);

    public BusinessAnnotation buildAnnotationInfo(T annotation) {
        BusinessAnnotation info = new BusinessAnnotation();
        info.setCode(getCode(annotation));
        info.setName(getName(annotation));
        info.setDesc(getDesc(annotation));
        info.setPriority(getPriority(annotation));
        return info;
    }
}
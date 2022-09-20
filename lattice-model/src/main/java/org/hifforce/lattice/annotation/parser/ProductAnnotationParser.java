package org.hifforce.lattice.annotation.parser;

import org.hifforce.lattice.annotation.model.ProductAnnotation;
import org.hifforce.lattice.model.ability.IBusinessExt;
import org.hifforce.lattice.model.business.TemplateType;

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

    public abstract TemplateType getType(T annotation);

    public abstract int getPriority(T annotation);

    public ProductAnnotation buildAnnotationInfo(T annotation) {
        ProductAnnotation info = new ProductAnnotation();
        info.setCode(getCode(annotation));
        info.setName(getName(annotation));
        info.setDesc(getDesc(annotation));
        info.setType(getType(annotation));
        info.setPriority(getPriority(annotation));
        return info;
    }
}
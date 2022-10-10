package org.hiforce.lattice.spi.annotation;

import org.hiforce.lattice.annotation.model.RealizationAnnotation;
import org.hiforce.lattice.model.ability.IBusinessExt;

import java.lang.annotation.Annotation;

/**
 * @author Rocky Yu
 * @since 2022/9/18
 */
public abstract class RealizationAnnotationParser<T extends Annotation> extends LatticeAnnotationParser<T> {

    public abstract String[] getCodes(T annotation);

    public abstract String getScenario(T annotation);

    public RealizationAnnotation buildAnnotationInfo(T annotation, Class<IBusinessExt> businessExtClass) {
        RealizationAnnotation info = new RealizationAnnotation();
        info.setCodes(getCodes(annotation));
        info.setScenario(getScenario(annotation));
        info.setBusinessExtClass(businessExtClass);
        return info;
    }
}

package org.hiforce.lattice.spi.annotation;

import org.hiforce.lattice.annotation.model.AbilityAnnotation;
import org.hiforce.lattice.model.ability.IAbility;

import java.lang.annotation.Annotation;

/**
 * @author Rocky Yu
 * @since 2022/9/15
 */
public abstract class AbilityAnnotationParser<T extends Annotation> extends LatticeAnnotationParser<T> {

    public abstract String getCode(T annotation);

    public abstract String getName(T annotation);

    public abstract String getDesc(T annotation);

    public abstract String getParent(T annotation);

    @SuppressWarnings("rawtypes")
    public AbilityAnnotation buildAnnotationInfo(T annotation, Class<IAbility> abilityClass) {
        AbilityAnnotation info = new AbilityAnnotation();
        info.setCode(getCode(annotation));
        info.setName(getName(annotation));
        info.setDesc(getDesc(annotation));
        info.setParent(getParent(annotation));
        info.setAbilityClass(abilityClass);
        return info;
    }
}

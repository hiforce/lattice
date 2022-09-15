package org.hifforce.lattice.annotation.parser;

import com.google.auto.service.AutoService;
import org.hifforce.lattice.annotation.Ability;
import org.hifforce.lattice.annotation.model.AbilityAnnotation;

/**
 * @author Rocky Yu
 * @since 2022/9/15
 */
@AutoService(AbilityAnnotationParser.class)
public class AbilityAnnotationParser extends LatticeAnnotationParser<Ability, AbilityAnnotation> {

    @Override
    public Class<Ability> getAnnotationClass() {
        return Ability.class;
    }

    public String getCode(Ability annotation) {
        return annotation.code();
    }

    public String getName(Ability annotation) {
        return annotation.name();
    }

    public String getDesc(Ability annotation) {
        return annotation.desc();
    }

    public String getParent(Ability annotation) {
        return annotation.parent();
    }

    public AbilityAnnotation buildAnnotationInfo(Ability annotation) {
        AbilityAnnotation info = new AbilityAnnotation();
        info.setCode(getCode(annotation));
        info.setName(getName(annotation));
        info.setDesc(getDesc(annotation));
        info.setParent(getParent(annotation));
        return info;
    }

}

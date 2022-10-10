package org.hiforce.lattice.annotation.parser;

import com.google.auto.service.AutoService;
import org.hiforce.lattice.annotation.Ability;
import org.hiforce.lattice.spi.annotation.AbilityAnnotationParser;

/**
 * @author Rocky Yu
 * @since 2022/9/16
 */
@AutoService(AbilityAnnotationParser.class)
public class DefaultAbilityAnnotationParser extends AbilityAnnotationParser<Ability> {
    @Override
    public String getCode(Ability annotation) {
        return annotation.code();
    }

    @Override
    public String getName(Ability annotation) {
        return annotation.name();
    }

    @Override
    public String getDesc(Ability annotation) {
        return annotation.desc();
    }

    @Override
    public String getParent(Ability annotation) {
        return annotation.parent();
    }

    @Override
    public Class<Ability> getAnnotationClass() {
        return Ability.class;
    }
}

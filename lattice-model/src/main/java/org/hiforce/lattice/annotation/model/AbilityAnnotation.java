package org.hiforce.lattice.annotation.model;

import lombok.Getter;
import lombok.Setter;
import org.hiforce.lattice.model.ability.IAbility;

/**
 * @author Rocky Yu
 * @since 2022/9/15
 */
@SuppressWarnings("rawtypes")
public class AbilityAnnotation {

    @Getter
    @Setter
    private String code;

    @Getter
    @Setter
    private String name;

    @Getter
    @Setter
    private String desc;

    @Getter
    @Setter
    private String parent;

    @Getter
    @Setter
    private Class<IAbility> abilityClass;
}

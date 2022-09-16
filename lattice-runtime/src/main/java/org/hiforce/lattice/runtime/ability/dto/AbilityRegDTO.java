package org.hiforce.lattice.runtime.ability.dto;

import lombok.Getter;
import org.hifforce.lattice.model.register.BaseSpec;

import java.io.Serializable;
import java.util.Collection;

/**
 * @author Rocky Yu
 * @since 2022/9/16
 */
public class AbilityRegDTO implements Serializable {

    private static final long serialVersionUID = -492534703389638611L;

    @Getter
    private final Collection<Class> classSet;

    @Getter
    private final BaseSpec parent;

    public AbilityRegDTO(BaseSpec parent, Collection<Class> classSet) {
        this.parent = parent;
        this.classSet = classSet;
    }
}

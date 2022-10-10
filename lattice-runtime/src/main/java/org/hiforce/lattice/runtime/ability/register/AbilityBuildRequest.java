package org.hiforce.lattice.runtime.ability.register;

import lombok.Getter;
import org.hiforce.lattice.model.register.BaseSpec;

import java.io.Serializable;
import java.util.Collection;

/**
 * @author Rocky Yu
 * @since 2022/9/16
 */
public class AbilityBuildRequest implements Serializable {

    private static final long serialVersionUID = -492534703389638611L;

    @Getter
    private final Collection<Class> classSet;

    @Getter
    private final BaseSpec parent;

    public AbilityBuildRequest(BaseSpec parent, Collection<Class> classSet) {
        this.parent = parent;
        this.classSet = classSet;
    }
}

package org.hiforce.lattice.maven.model;

import lombok.Getter;
import lombok.Setter;
import org.hifforce.lattice.utils.JacksonUtils;

/**
 * @author Rocky Yu
 * @since 2022/10/7
 */
public class AbilityInfo extends BaseInfo {

    private static final long serialVersionUID = 4484440751145231453L;

    @Getter
    @Setter
    private DependencyInfo dependency;

    @Override
    public String toString() {

        return "Ability = [" + JacksonUtils.serializeWithoutException(this) + ']';
    }
}

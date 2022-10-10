package org.hiforce.lattice.maven.model;

import com.google.common.collect.Lists;
import lombok.Getter;
import lombok.Setter;
import org.hiforce.lattice.utils.JacksonUtils;

import java.util.List;

/**
 * @author Rocky Yu
 * @since 2022/10/7
 */
public class AbilityInfo extends BaseInfo {

    private static final long serialVersionUID = 4484440751145231453L;

    @Getter
    @Setter
    private DependencyInfo dependency;

    @Getter
    private final List<AbilityInstInfo> instances = Lists.newArrayList();

    @Override
    public String toString() {

        return "Ability = [" + JacksonUtils.serializeWithoutException(this) + ']';
    }
}

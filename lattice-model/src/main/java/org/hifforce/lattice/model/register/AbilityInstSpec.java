package org.hifforce.lattice.model.register;

import com.google.common.collect.Sets;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

import java.util.Set;

/**
 * @author Rocky Yu
 * @since 2022/9/15
 */
public class AbilityInstSpec extends BaseSpec {

    @Getter
    @Setter
    private String abilityCode;

    @Getter
    @Setter
    private String instanceClass;

    @Getter
    @Setter
    private int priority = 2000;

    @Getter
    private final Set<ExtensionPointSpec> extensions = Sets.newHashSet();

    @Override
    public boolean equals(Object obj) {
        if (null == obj) {
            return false;
        }
        if (!(obj instanceof AbilityInstSpec)) {
            return false;
        }
        AbilityInstSpec target = (AbilityInstSpec) obj;
        return StringUtils.equals(target.getCode(), this.getCode());
    }

    @Override
    public int hashCode() {
        if (StringUtils.isNotEmpty(getCode())) {
            return getCode().hashCode();
        }
        return super.hashCode();
    }

    public String toString() {
        return String.format("AbilityInst code: [%s], name: [%s]", getCode(), getName());
    }
}

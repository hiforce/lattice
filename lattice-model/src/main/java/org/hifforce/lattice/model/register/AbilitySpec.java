package org.hifforce.lattice.model.register;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Rocky Yu
 * @since 2022/9/15
 */
@Slf4j
public class AbilitySpec extends BaseSpec {

    /**
     * The parent model's code
     */
    @Getter
    @Setter
    private String parentCode;

    /**
     * The parent model type, e.g. Domain, Capability.
     * For visualization breakdown the ability, we need the parent type and code.
     */
    @Getter
    @Setter
    private String parentType;


    @Getter
    @Setter
    private Class<?> abilityClass;

    /**
     * An ability contains many instances.
     * For e.g. EyeAbility may have HumanEyeAbilityInst, ElectronicEyeAbilityInst..
     */
    @Getter
    private Map<String, AbilityInstSpec> abilityInstSpecMap = Maps.newConcurrentMap();

    /**
     * the ability instances create current ability.
     */
    @Getter
    private final Set<AbilityInstSpec> abilityInstances = Sets.newConcurrentHashSet();

    public static AbilitySpec of(String code, String name, String desc) {
        AbilitySpec abilitySpec = new AbilitySpec();
        abilitySpec.setCode(code);
        abilitySpec.setName(name);
        abilitySpec.setDescription(desc);
        return abilitySpec;
    }

    public void addAbilityInstance(AbilityInstSpec abilityInstSpec) {
        if (null == abilityInstSpec) {
            return;
        }
        if (abilityInstSpecMap.containsKey(abilityInstSpec.getCode())) {
            log.debug("Duplicated ability instance registered，code: [{}], name: [{}]",
                    abilityInstSpec.getCode(), abilityInstSpec.getName());
            return;
        }
        abilityInstSpec.setAbilityCode(this.getCode());
        abilityInstSpecMap.put(abilityInstSpec.getCode(), abilityInstSpec);
    }

    public void addAbilityInstance(List<AbilityInstSpec> instanceSpecs) {
        if (CollectionUtils.isEmpty(instanceSpecs)) {
            return;
        }
        for (AbilityInstSpec abilityInstanceSpec : instanceSpecs) {
            if (abilityInstanceSpec.inList(abilityInstances)) {
                log.debug("[Lattice]AbilityInstance重复注册，[{}-{}]", abilityInstanceSpec.getName(), abilityInstanceSpec.getCode());
                continue;
            }
            abilityInstanceSpec.setAbilityCode(this.getCode());
            abilityInstances.add(abilityInstanceSpec);
        }
        rebuildAbilityInstanceSpecMap();
    }

    private Map<String, AbilityInstSpec> rebuildAbilityInstanceSpecMap() {
        this.abilityInstSpecMap = Maps.newConcurrentMap();
        if (CollectionUtils.isEmpty(abilityInstances)) {
            return abilityInstSpecMap;
        }
        for (AbilityInstSpec abilityInstance : abilityInstances) {
            if (StringUtils.isBlank(abilityInstance.getInstanceClass())) {
                continue;
            }
            abilityInstSpecMap.put(abilityInstance.getInstanceClass(), abilityInstance);
        }
        return abilityInstSpecMap;
    }

    @Override
    public boolean equals(Object obj) {
        if (null == obj) {
            return false;
        }
        if (!(obj instanceof AbilitySpec)) {
            return false;
        }
        AbilitySpec target = (AbilitySpec) obj;
        return StringUtils.equals(target.getCode(), this.getCode());
    }

    @Override
    public int hashCode() {
        if (StringUtils.isEmpty(this.getCode())) {
            return super.hashCode();
        }
        return this.getCode().hashCode();
    }

    @Override
    public String toString() {
        return "Ability{" +
                "code='" + getCode() + '\'' +
                "name='" + getName() + '\'' +
                '}';
    }
}

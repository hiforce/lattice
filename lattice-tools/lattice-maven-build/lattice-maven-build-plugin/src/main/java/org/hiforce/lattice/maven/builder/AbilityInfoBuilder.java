package org.hiforce.lattice.maven.builder;

import org.hiforce.lattice.model.ability.IAbility;
import org.hiforce.lattice.model.register.AbilityInstSpec;
import org.hiforce.lattice.model.register.AbilitySpec;
import org.hiforce.lattice.model.register.ExtensionSpec;
import org.hiforce.lattice.maven.LatticeBuildPlugin;
import org.hiforce.lattice.maven.model.AbilityInfo;
import org.hiforce.lattice.maven.model.AbilityInstInfo;
import org.hiforce.lattice.maven.model.ExtParam;
import org.hiforce.lattice.maven.model.ExtensionInfo;
import org.hiforce.lattice.runtime.ability.register.AbilityBuildRequest;
import org.hiforce.lattice.runtime.ability.register.AbilityRegister;

import java.lang.reflect.Parameter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Rocky Yu
 * @since 2022/10/6
 */
public class AbilityInfoBuilder extends LatticeInfoBuilder {

    @Override
    public String getSpiClassName() {
        return IAbility.class.getName();
    }

    public AbilityInfoBuilder(
            LatticeBuildPlugin plugin) {
        super(plugin);
    }

    public void build() {
        List<String> definedAbilityNames = getProvidedInfoClassNames();
        List<AbilityInfo> providedAbilities = getLoadAbilityClass(definedAbilityNames);
        getPlugin().getLatticeInfo().getAbility().getProviding().addAll(providedAbilities);

        List<String> importAbilityNames = getImportInfoClassNames();
        List<AbilityInfo> importedAbilityInfos = getLoadAbilityClass(importAbilityNames);
        getPlugin().getLatticeInfo().getAbility().getUsing().addAll(importedAbilityInfos);
    }

    @SuppressWarnings("all")
    private List<AbilityInfo> getLoadAbilityClass(List<String> classNames) {
        AbilityRegister register = AbilityRegister.getInstance();
        List<AbilitySpec> abilitySpecs = register.register(new AbilityBuildRequest(null, loadTargetClassList(classNames)));
        return abilitySpecs.stream()
                .map(p -> buildAbilityInfo(p))
                .collect(Collectors.toList());
    }

    @SuppressWarnings("all")
    private AbilityInfo buildAbilityInfo(AbilitySpec abilitySpec) {
        AbilityInfo info = new AbilityInfo();

        Class<?> abilityClass = abilitySpec.getAbilityClass();
        info.setCode(abilitySpec.getCode());
        info.setName(abilitySpec.getName());
        info.setClassName(abilityClass.getName());

        info.getInstances().addAll(abilitySpec.getAbilityInstances().stream()
                .map(p -> buildAbilityInstInfo(p)).collect(Collectors.toList()));
        info.setDependency(getDependencyInfo(abilityClass));
        return info;
    }

    private AbilityInstInfo buildAbilityInstInfo(AbilityInstSpec instSpec) {
        AbilityInstInfo instInfo = new AbilityInstInfo();
        instInfo.setCode(instSpec.getCode());
        instInfo.setAbilityCode(instSpec.getAbilityCode());
        instInfo.setName(instSpec.getName());
        instInfo.setClassName(instSpec.getInstanceClass());
        instInfo.setPriority(instSpec.getPriority());

        instInfo.getExtensions().addAll(instSpec.getExtensions().stream()
                .map(AbilityInfoBuilder::buildExtensionInfo)
                .collect(Collectors.toList()));
        return instInfo;
    }

    public static ExtensionInfo buildExtensionInfo(ExtensionSpec spec) {
        ExtensionInfo info = new ExtensionInfo();
        info.setAbilityCode(spec.getAbilityCode());
        info.setGroupCode(spec.getGroupCode());
        info.setGroupName(spec.getGroupName());
        info.setCode(spec.getCode());
        info.setName(spec.getName());
        info.setReduceType(spec.getReduceType());
        info.setProtocolType(spec.getProtocolType());
        if (null != spec.getItfClass()) {
            info.setClassName(spec.getItfClass().getName());
        }
        if (null != spec.getInvokeMethod()) {
            info.setReturnTypeName(spec.getInvokeMethod().getReturnType().getName());
            info.setMethodName(spec.getInvokeMethod().getName());
            info.setParameterCount(spec.getInvokeMethod().getParameterCount());
            for (int i = 0; i < spec.getInvokeMethod().getParameterCount(); i++) {
                Parameter parameter = spec.getInvokeMethod().getParameters()[i];
                ExtParam param = new ExtParam();
                param.setName(parameter.getName());
                param.setTypeName(parameter.getType().getTypeName());
                info.getParams().add(param);
            }
        }
        return info;
    }
}

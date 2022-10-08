package org.hiforce.lattice.maven.builder;

import org.apache.commons.lang3.StringUtils;
import org.apache.maven.model.Dependency;
import org.hifforce.lattice.model.ability.IAbility;
import org.hifforce.lattice.model.register.AbilityInstSpec;
import org.hifforce.lattice.model.register.AbilitySpec;
import org.hifforce.lattice.model.register.ExtensionPointSpec;
import org.hiforce.lattice.maven.LatticeBuildPlugin;
import org.hiforce.lattice.maven.model.*;
import org.hiforce.lattice.runtime.Lattice;
import org.hiforce.lattice.runtime.ability.register.AbilityBuildRequest;
import org.hiforce.lattice.runtime.ability.register.AbilityRegister;

import java.io.File;
import java.lang.reflect.Parameter;
import java.net.URI;
import java.security.CodeSource;
import java.security.ProtectionDomain;
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
        getLog().info(">> Lattice AbilityInfoBuilder build~~~");
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
        List<Dependency> dependencies = getPlugin().getMavenProject().getRuntimeDependencies();
        Class<?> abilityClass = abilitySpec.getAbilityClass();
        info.setCode(abilitySpec.getCode());
        info.setName(abilitySpec.getName());
        info.setClassName(abilityClass.getName());

        info.getInstances().addAll(abilitySpec.getAbilityInstances().stream()
                .map(p -> buildAbilityInstInfo(p)).collect(Collectors.toList()));

        try {
            ProtectionDomain protectionDomain = abilityClass.getProtectionDomain();
            CodeSource codeSource = protectionDomain.getCodeSource();
            URI location = (codeSource != null) ? codeSource.getLocation().toURI() : null;
            String path = (location != null) ? location.getSchemeSpecificPart() : null;

            File file = new File(path);
            DependencyInfo dependency = dependencies.stream()
                    .filter(p -> StringUtils.equals(file.getName(),
                            String.format("%s-%s.jar", p.getArtifactId(), p.getVersion())))
                    .findFirst()
                    .map(p -> DependencyInfo.of(p.getGroupId(), p.getArtifactId(), p.getVersion()))
                    .orElse(null);
            if (null != dependency) {
                info.setDependency(dependency);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
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

    public static ExtensionInfo buildExtensionInfo(ExtensionPointSpec spec) {
        ExtensionInfo info = new ExtensionInfo();
        info.setCode(spec.getCode());
        info.setName(spec.getName());
        info.setGroupCode(spec.getGroupCode());
        info.setGroupName(spec.getGroupName());
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

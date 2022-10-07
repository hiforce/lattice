package org.hiforce.lattice.maven.builder;

import org.apache.commons.lang3.StringUtils;
import org.apache.maven.model.Dependency;
import org.apache.maven.plugin.logging.Log;
import org.hifforce.lattice.model.ability.IAbility;
import org.hifforce.lattice.model.register.AbilitySpec;
import org.hiforce.lattice.maven.LatticeBuildPlugin;
import org.hiforce.lattice.maven.model.AbilityInfo;
import org.hiforce.lattice.maven.model.DependencyInfo;
import org.hiforce.lattice.runtime.Lattice;
import org.hiforce.lattice.runtime.ability.register.AbilityBuildRequest;
import org.hiforce.lattice.runtime.ability.register.AbilityRegister;

import java.io.File;
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

    public AbilityInfoBuilder(
            LatticeBuildPlugin plugin) {
        super(plugin);
    }

    public void build() {
        getLog().info(">>> AbilityInfoBuilder build...");
        List<String> definedAbilityNames = Lattice.getServiceProviderValues(
                IAbility.class.getName(), getProjectClassLoader());

        getLog().info(">>> " + getPlugin().getMavenProject().getName() + " defined abilities: " + String.join(",", definedAbilityNames));

        List<String> importAbilityNames = Lattice.getServiceProviderValues(
                IAbility.class.getName(), getImportClassLoader());

        List<AbilityInfo> importedAbilityInfos = getLoadAbilityClass(importAbilityNames);
        getPlugin().getLatticeInfo().getUsingAbilities().addAll(importedAbilityInfos);

        getLog().info(">>> Imported abilities = [ " +
                importedAbilityInfos.stream().map(AbilityInfo::toString).collect(Collectors.joining(",")) + " ] ");
    }

    @SuppressWarnings("all")
    private List<AbilityInfo> getLoadAbilityClass(List<String> classNames) {

        AbilityRegister register = AbilityRegister.getInstance();
        List<AbilitySpec> abilitySpecs = register.register(new AbilityBuildRequest(null, loadTargetClassList(classNames)));
        abilitySpecs.forEach(p -> getLog().info(abilitySpecs.toString()));
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


    private Log getLog() {
        return getPlugin().getLog();
    }
}

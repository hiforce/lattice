package org.hiforce.lattice.maven.builder;

import com.google.common.collect.Lists;
import org.apache.maven.model.Dependency;
import org.hifforce.lattice.model.ability.IBusinessExt;
import org.hifforce.lattice.model.business.IUseCase;
import org.hifforce.lattice.model.register.UseCaseSpec;
import org.hiforce.lattice.maven.LatticeBuildPlugin;
import org.hiforce.lattice.maven.model.RealizationInfo;
import org.hiforce.lattice.maven.model.UseCaseInfo;
import org.hiforce.lattice.runtime.ability.register.AbilityRegister;
import org.hiforce.lattice.runtime.ability.register.TemplateRegister;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author Rocky Yu
 * @since 2022/10/7
 */
public class UseCaseInfoBuilder extends LatticeInfoBuilder {
    @Override
    public String getSpiClassName() {
        return IUseCase.class.getName();
    }

    public UseCaseInfoBuilder(LatticeBuildPlugin plugin) {
        super(plugin);
    }

    public void build() {
        getLog().info(">> Lattice UseCaseInfoBuilder build~~~");
        List<String> providedClassNames = getProvidedInfoClassNames();
        List<UseCaseInfo> provided = getUseCaseInfo(providedClassNames);
        getPlugin().getLatticeInfo().getUseCase().getProviding().addAll(provided);

        TemplateRegister.getInstance().getUseCases().clear();

        List<String> importClassNames = getImportInfoClassNames();
        List<UseCaseInfo> imported = getUseCaseInfo(importClassNames);
        getPlugin().getLatticeInfo().getUseCase().getUsing().addAll(imported);
    }

    @SuppressWarnings("all")
    private List<UseCaseInfo> getUseCaseInfo(List<String> classNames) {
        try {
            AbilityRegister register = AbilityRegister.getInstance();
            List<UseCaseSpec> specs = TemplateRegister.getInstance().registerUseCases(loadTargetClassList(classNames));
            return specs.stream()
                    .map(p -> buildUseCaseInfo(p))
                    .collect(Collectors.toList());
        } catch (Throwable th) {
            th.printStackTrace();
            return Lists.newArrayList();
        }
    }

    @SuppressWarnings("all")
    private UseCaseInfo buildUseCaseInfo(UseCaseSpec useCaseSpec) {
        UseCaseInfo info = new UseCaseInfo();
        List<Dependency> dependencies = getPlugin().getMavenProject().getRuntimeDependencies();
        info.setCode(useCaseSpec.getCode());
        info.setName(useCaseSpec.getName());
        info.setClassName(useCaseSpec.getUseCaseClass().getName());
        info.setSdk(useCaseSpec.getSdk().getName());
        info.getExtensions().addAll(
                useCaseSpec.getExtensions().stream()
                        .map(AbilityInfoBuilder::buildExtensionInfo).collect(Collectors.toSet()));


        List<RealizationInfo> realizationInfos =
                useCaseSpec.getRealizations().stream()
                        .map(LatticeInfoBuilder::buildRealizationInfo)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList());
        try {
            for (RealizationInfo realizationInfo : realizationInfos) {
                IBusinessExt businessExt = (IBusinessExt) getTotalClassLoader()
                        .loadClass(realizationInfo.getBusinessExtClass())
                        .newInstance();
                info.getCustomized().addAll(buildCustomizedExtensionInfos(businessExt));
            }
        } catch (Exception ex) {
            getLog().error(ex.getMessage(), ex);
        }

        info.setDependency(getDependencyInfo(useCaseSpec.getUseCaseClass()));
        return info;
    }


}

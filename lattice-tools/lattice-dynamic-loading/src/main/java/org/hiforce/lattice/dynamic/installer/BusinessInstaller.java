package org.hiforce.lattice.dynamic.installer;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.hiforce.lattice.dynamic.classloader.LatticeClassLoader;
import org.hiforce.lattice.dynamic.model.PluginFileInfo;
import org.hiforce.lattice.model.ability.IBusinessExt;
import org.hiforce.lattice.model.business.IBusiness;
import org.hiforce.lattice.model.register.BusinessSpec;
import org.hiforce.lattice.model.register.RealizationSpec;
import org.hiforce.lattice.runtime.Lattice;
import org.hiforce.lattice.runtime.ability.register.TemplateRegister;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.hiforce.lattice.dynamic.utils.DynamicUtils.isPluginDefined;
import static org.hiforce.lattice.runtime.Lattice.getServiceProviderClasses;

/**
 * @author Rocky Yu
 * @since 2022/10/13
 */
@Slf4j
@SuppressWarnings("all")
public class BusinessInstaller implements LatticeInstaller {

    @Override
    public InstallResult install(LatticeClassLoader classLoader, PluginFileInfo fileInfo) {
        if (null == fileInfo) {
            return InstallResult.success();
        }
        log.info("Lattice dynamic install business file: " + fileInfo.getFile().getPath());

        Set<Class> classSet = getServiceProviderClasses(IBusinessExt.class.getName(), classLoader);
        classSet = classSet.stream().filter(p -> isPluginDefined(p, fileInfo))
                .collect(Collectors.toSet());
        if (CollectionUtils.isNotEmpty(classSet)) {
            List<RealizationSpec> installed = TemplateRegister.getInstance().registerRealizations(classSet);
            log.info("---> realization installed: " + installed.stream()
                    .filter(p -> null != p.getBusinessExt())
                    .map(p -> p.getBusinessExt().getClass().getName())
                    .collect(Collectors.joining(",")));
        }

        Set<Class> businessExtClassSet = getServiceProviderClasses(IBusiness.class.getName(), classLoader);
        if (CollectionUtils.isNotEmpty(businessExtClassSet)) {
            businessExtClassSet = businessExtClassSet.stream().filter(p -> isPluginDefined(p, fileInfo))
                    .collect(Collectors.toSet());
            List<BusinessSpec> businessSpecs = TemplateRegister.getInstance().registerBusinesses(businessExtClassSet);
            log.info("---> business installed: " + businessSpecs.stream()
                    .map(p -> String.format("[%s]-%s", p.getCode(), p.getName()))
                    .collect(Collectors.joining(",")));

            businessSpecs.forEach(p -> Lattice.getInstance().autoAddAndBuildBusinessConfig(p));
        }
        return InstallResult.success();
    }
}

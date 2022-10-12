package org.hiforce.lattice.dynamic.destroy;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.hiforce.lattice.annotation.model.BusinessAnnotation;
import org.hiforce.lattice.dynamic.classloader.LatticeClassLoader;
import org.hiforce.lattice.dynamic.model.PluginFileInfo;
import org.hiforce.lattice.model.business.IBusiness;
import org.hiforce.lattice.runtime.ability.register.TemplateRegister;
import org.hiforce.lattice.runtime.cache.exension.ExtensionInvokeCache;

import java.util.Set;
import java.util.stream.Collectors;

import static org.hiforce.lattice.runtime.Lattice.getServiceProviderClasses;
import static org.hiforce.lattice.utils.LatticeAnnotationUtils.getBusinessAnnotation;

/**
 * @author Rocky Yu
 * @since 2022/10/12
 */
@SuppressWarnings("all")
public class BusinessUninstaller implements LatticeUninstaller {

    @Override

    public DestroyResult uninstall(LatticeClassLoader classLoader, PluginFileInfo fileInfo) {
        Set<Class> businessExtClassSet = getServiceProviderClasses(IBusiness.class.getName(), classLoader);
        if (CollectionUtils.isEmpty(businessExtClassSet)) {
            return DestroyResult.success();
        }
        businessExtClassSet = businessExtClassSet.stream().filter(p -> isPluginDefined(p, fileInfo))
                .collect(Collectors.toSet());
        for (Class businessClass : businessExtClassSet) {
            BusinessAnnotation annotation = getBusinessAnnotation(businessClass);
            if (null == annotation) {
                continue;
            }
            TemplateRegister.getInstance().getBusinesses().removeIf(p -> StringUtils.equals(p.getCode(), annotation.getCode()));
            TemplateRegister.getInstance().getRealizations().removeIf(p -> StringUtils.equals(p.getCode(), annotation.getCode()));
            ExtensionInvokeCache.getInstance().clear();
        }
        return DestroyResult.success();
    }

    private boolean isPluginDefined(Class targetClass, PluginFileInfo fileInfo) {
        String path = targetClass.getProtectionDomain().getCodeSource()
                .getLocation().getPath();
        return StringUtils.equals(path, fileInfo.getFile().getPath());
    }
}

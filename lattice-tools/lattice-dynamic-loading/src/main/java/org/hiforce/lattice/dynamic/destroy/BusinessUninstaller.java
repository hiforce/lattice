package org.hiforce.lattice.dynamic.destroy;

import org.apache.commons.collections4.CollectionUtils;
import org.hiforce.lattice.annotation.model.BusinessAnnotation;
import org.hiforce.lattice.dynamic.classloader.LatticeClassLoader;
import org.hiforce.lattice.dynamic.model.PluginFileInfo;
import org.hiforce.lattice.model.business.IBusiness;
import org.hiforce.lattice.runtime.Lattice;

import java.util.Set;
import java.util.stream.Collectors;

import static org.hiforce.lattice.dynamic.utils.DynamicUtils.isPluginDefined;
import static org.hiforce.lattice.runtime.Lattice.getServiceProviderClasses;
import static org.hiforce.lattice.utils.LatticeAnnotationUtils.getBusinessAnnotation;

/**
 * @author Rocky Yu
 * @since 2022/10/12
 */
@SuppressWarnings("all")
public class BusinessUninstaller implements LatticeUninstaller {

    @Override
    public synchronized DestroyResult uninstall(LatticeClassLoader classLoader, PluginFileInfo fileInfo) {
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
            Lattice.getInstance().getRuntimeCache().clearBusinessCache(annotation.getCode());
        }
        return DestroyResult.success();
    }


}

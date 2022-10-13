package org.hiforce.lattice.dynamic.destroy;

import org.apache.commons.collections4.CollectionUtils;
import org.hiforce.lattice.annotation.model.ProductAnnotation;
import org.hiforce.lattice.dynamic.classloader.LatticeClassLoader;
import org.hiforce.lattice.dynamic.model.PluginFileInfo;
import org.hiforce.lattice.model.business.IProduct;
import org.hiforce.lattice.runtime.Lattice;

import java.util.Set;
import java.util.stream.Collectors;

import static org.hiforce.lattice.dynamic.utils.DynamicUtils.isPluginDefined;
import static org.hiforce.lattice.runtime.Lattice.getServiceProviderClasses;
import static org.hiforce.lattice.utils.LatticeAnnotationUtils.getProductAnnotation;

/**
 * @author Rocky Yu
 * @since 2022/10/13
 */
public class ProductUninstaller implements LatticeUninstaller {
    @Override
    @SuppressWarnings("all")
    public DestroyResult uninstall(LatticeClassLoader classLoader, PluginFileInfo fileInfo) {
        Set<Class> classSet = getServiceProviderClasses(IProduct.class.getName(), classLoader);
        if (CollectionUtils.isEmpty(classSet)) {
            return DestroyResult.success();
        }
        classSet = classSet.stream().filter(p -> isPluginDefined(p, fileInfo))
                .collect(Collectors.toSet());
        for (Class productClass : classSet) {
            ProductAnnotation annotation = getProductAnnotation(productClass);
            if (null == annotation) {
                continue;
            }
            Lattice.getInstance().getRuntimeCache().clearProductCache(annotation.getCode());
        }
        return DestroyResult.success();
    }
}

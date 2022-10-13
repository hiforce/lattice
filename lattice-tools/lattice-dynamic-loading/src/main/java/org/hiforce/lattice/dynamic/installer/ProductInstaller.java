package org.hiforce.lattice.dynamic.installer;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.hiforce.lattice.dynamic.classloader.LatticeClassLoader;
import org.hiforce.lattice.dynamic.model.PluginFileInfo;
import org.hiforce.lattice.model.ability.IBusinessExt;
import org.hiforce.lattice.model.business.IProduct;
import org.hiforce.lattice.model.register.BusinessSpec;
import org.hiforce.lattice.model.register.ProductSpec;
import org.hiforce.lattice.model.register.RealizationSpec;
import org.hiforce.lattice.runtime.Lattice;
import org.hiforce.lattice.runtime.ability.register.TemplateRegister;
import org.hiforce.lattice.runtime.cache.config.BusinessConfigCache;

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
public class ProductInstaller implements LatticeInstaller {
    @Override
    public InstallResult install(LatticeClassLoader classLoader, PluginFileInfo fileInfo) {
        if (null == fileInfo) {
            return InstallResult.success(null);
        }
        Set<Class> productClassSet = getServiceProviderClasses(IProduct.class.getName(), classLoader);
        productClassSet = productClassSet.stream().filter(p -> isPluginDefined(p, fileInfo))
                .collect(Collectors.toSet());
        if (CollectionUtils.isEmpty(productClassSet)) {
            return InstallResult.success(null);
        }

        Set<Class> classSet = getServiceProviderClasses(IBusinessExt.class.getName(), classLoader);
        classSet = classSet.stream().filter(p -> isPluginDefined(p, fileInfo))
                .collect(Collectors.toSet());

        if (CollectionUtils.isNotEmpty(classSet)) {
            log.info("Lattice dynamic install realization: " + fileInfo.getFile().getPath());
            List<RealizationSpec> installed = TemplateRegister.getInstance().registerRealizations(classSet);
            log.info("---> realization installed: " + installed.stream()
                    .filter(p -> null != p.getBusinessExt())
                    .map(p -> p.getBusinessExt().getClass().getName())
                    .collect(Collectors.joining(",")));
        }

        if (CollectionUtils.isNotEmpty(productClassSet)) {
            log.info("Lattice dynamic install product: " + fileInfo.getFile().getPath());
            productClassSet = productClassSet.stream().filter(p -> isPluginDefined(p, fileInfo))
                    .collect(Collectors.toSet());
            List<ProductSpec> productSpecs = TemplateRegister.getInstance().registerProducts(productClassSet);
            fileInfo.getProductCodes().addAll(productSpecs
                    .stream().map(p -> p.getCode()).collect(Collectors.toSet()));
            log.info("---> product installed: " + productSpecs.stream()
                    .map(p -> String.format("[%s]-%s", p.getCode(), p.getName()))
                    .collect(Collectors.joining(",")));
        }

        return InstallResult.success(fileInfo);
    }
}

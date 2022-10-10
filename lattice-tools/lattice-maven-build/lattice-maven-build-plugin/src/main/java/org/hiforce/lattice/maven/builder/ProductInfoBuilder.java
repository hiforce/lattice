package org.hiforce.lattice.maven.builder;

import com.google.common.collect.Lists;
import org.apache.maven.model.Dependency;
import org.hiforce.lattice.model.ability.IBusinessExt;
import org.hiforce.lattice.model.business.IProduct;
import org.hiforce.lattice.model.register.ProductSpec;
import org.hiforce.lattice.maven.LatticeBuildPlugin;
import org.hiforce.lattice.maven.model.ProductInfo;
import org.hiforce.lattice.maven.model.RealizationInfo;
import org.hiforce.lattice.runtime.ability.register.TemplateRegister;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author Rocky Yu
 * @since 2022/10/8
 */
public class ProductInfoBuilder extends LatticeInfoBuilder {
    public ProductInfoBuilder(LatticeBuildPlugin plugin) {
        super(plugin);
    }

    @Override
    public String getSpiClassName() {
        return IProduct.class.getName();
    }

    @Override
    public void build() {
        List<ProductInfo> provided = getProductInfo(getProvidedInfoClassNames());
        getPlugin().getLatticeInfo().getProduct().getProviding().addAll(provided);

        TemplateRegister.getInstance().getProducts().clear();

        List<ProductInfo> imported = getProductInfo(getImportInfoClassNames());
        getPlugin().getLatticeInfo().getProduct().getUsing().addAll(imported);
    }

    @SuppressWarnings("all")
    private List<ProductInfo> getProductInfo(List<String> classNames) {
        try {
            List<ProductSpec> specs = TemplateRegister.getInstance()
                    .registerProducts(loadTargetClassList(classNames));
            return specs.stream()
                    .map(p -> buildProductInfo(p))
                    .collect(Collectors.toList());
        } catch (Throwable th) {
            th.printStackTrace();
            return Lists.newArrayList();
        }
    }

    @SuppressWarnings("all")
    private ProductInfo buildProductInfo(ProductSpec spec) {
        ProductInfo info = new ProductInfo();
        List<Dependency> dependencies = getPlugin().getMavenProject().getRuntimeDependencies();
        info.setCode(spec.getCode());
        info.setName(spec.getName());
        info.setClassName(spec.getProductClass().getName());
        List<RealizationInfo> realizationInfos =
                spec.getRealizations().stream()
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
        info.setDependency(getDependencyInfo(spec.getProductClass()));
        return info;
    }
}

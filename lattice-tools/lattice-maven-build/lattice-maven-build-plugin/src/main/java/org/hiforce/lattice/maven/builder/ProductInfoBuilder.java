package org.hiforce.lattice.maven.builder;

import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;
import org.apache.maven.model.Dependency;
import org.hifforce.lattice.model.ability.IBusinessExt;
import org.hifforce.lattice.model.business.IProduct;
import org.hifforce.lattice.model.register.ProductSpec;
import org.hiforce.lattice.maven.LatticeBuildPlugin;
import org.hiforce.lattice.maven.model.DependencyInfo;
import org.hiforce.lattice.maven.model.ProductInfo;
import org.hiforce.lattice.maven.model.RealizationInfo;
import org.hiforce.lattice.runtime.ability.register.TemplateRegister;

import java.io.File;
import java.net.URI;
import java.security.CodeSource;
import java.security.ProtectionDomain;
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


        try {
            ProtectionDomain protectionDomain = spec.getProductClass().getProtectionDomain();
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
}

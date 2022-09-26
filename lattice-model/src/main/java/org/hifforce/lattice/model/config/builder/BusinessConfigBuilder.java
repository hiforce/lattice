package org.hifforce.lattice.model.config.builder;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import lombok.Getter;
import lombok.Setter;
import org.hifforce.lattice.model.config.BusinessConfig;
import org.hifforce.lattice.model.config.ExtPriorityConfig;
import org.hifforce.lattice.model.config.ProductConfig;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Rocky Yu
 * @since 2022/9/26
 */
@SuppressWarnings("unused")
public class BusinessConfigBuilder {

    @Getter
    @Setter
    private String bizCode;

    @Getter
    @Setter
    private int priority = 1000;

    @Getter
    private final Set<ProductConfig> products = Sets.newHashSet();

    @Getter
    private final List<ExtPriorityConfig> extensions = Lists.newArrayList();


    private BusinessConfigBuilder() {

    }

    public static BusinessConfigBuilder builder() {
        return new BusinessConfigBuilder();
    }

    public BusinessConfigBuilder bizCode(String bizCode) {
        this.bizCode = bizCode;
        return this;
    }

    public BusinessConfigBuilder priority(int priority) {
        this.priority = priority;
        return this;
    }

    public BusinessConfigBuilder install(String... productCodes) {
        if (null == productCodes) {
            return this;
        }
        this.products.addAll(Arrays.stream(productCodes).map(ProductConfig::of)
                .collect(Collectors.toList()));
        return this;
    }

    public BusinessConfigBuilder install(ProductConfig... products) {
        if (null == products) {
            return this;
        }
        this.products.addAll(Arrays.asList(products));
        return this;
    }

    public BusinessConfigBuilder install(List<ProductConfig> products) {
        if (null == products) {
            return this;
        }
        this.products.addAll(products);
        return this;
    }

    public BusinessConfigBuilder extension(ExtPriorityConfig... extConfigs) {
        if (null == extConfigs) {
            return this;
        }
        this.extensions.addAll(Arrays.asList(extConfigs));
        return this;
    }


    public BusinessConfigBuilder extension(List<ExtPriorityConfig> extConfigs) {
        if (null == extConfigs) {
            return this;
        }
        this.extensions.addAll(extConfigs);
        return this;
    }

    public BusinessConfig build() {
        BusinessConfig config = new BusinessConfig();
        config.setPriority(priority);
        config.setBizCode(bizCode);
        config.getProducts().addAll(products);
        config.getExtensions().addAll(extensions);
        return config;
    }
}

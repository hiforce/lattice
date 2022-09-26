package org.hifforce.lattice.model.config;

import com.google.common.collect.Lists;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.hifforce.lattice.model.business.TemplateType;

import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Rocky Yu
 * @since 2022/9/19
 */
@SuppressWarnings("all")
public class BusinessConfig implements Serializable {

    private static final long serialVersionUID = 2955186375325583813L;

    @Getter
    @Setter
    private String bizCode;

    @Getter
    @Setter
    private int priority = 1000;

    @Getter
    private List<ProductConfig> products = Lists.newArrayList();

    @Getter
    private List<ExtPriorityConfig> extensions = Lists.newArrayList();

    public boolean productInstalled(String productCode) {
        return products.stream().filter(p -> StringUtils.equals(p.getCode(), productCode))
                .findFirst().isPresent();
    }

    public boolean notContainExtCode(String extCode) {
        return !extensions.stream()
                .filter(p -> StringUtils.equals(extCode, p.getExtCode()))
                .findFirst().isPresent();
    }

    public ProductConfig getProductConfig(String productCode) {
        return products.stream()
                .filter(p -> StringUtils.equals(productCode, p.getCode()))
                .findFirst().orElse(null);
    }

    /**
     * @param extCode       The code of extension
     * @param isOnlyProduct only load the product type priority config.
     * @return found extension priority config.
     */
    public List<ExtPriority> getExtPriorityByCode(String extCode, boolean isOnlyProduct) {
        ExtPriorityConfig priorityConfig = extensions.stream()
                .filter(p -> StringUtils.equals(extCode, p.getExtCode()))
                .findFirst().orElse(null);
        if (null == priorityConfig) {
            return null;
        }
        List<ExtPriority> priorities = priorityConfig.getPriorities().stream()
                .filter(p -> isOnlyProduct ? p.getType() == TemplateType.PRODUCT : true)
                .collect(Collectors.toList());
        return priorities;
    }


}

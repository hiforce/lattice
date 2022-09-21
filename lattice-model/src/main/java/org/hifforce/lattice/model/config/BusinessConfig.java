package org.hifforce.lattice.model.config;

import com.google.common.collect.Lists;
import lombok.Builder;
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
@Builder
@SuppressWarnings("all")
public class BusinessConfig implements Serializable {

    private static final long serialVersionUID = 2955186375325583813L;

    @Getter
    @Setter
    private String bizCode;

    @Getter
    private List<ProductConfig> installedProducts = Lists.newArrayList();

    @Getter
    private List<PriorityConfig> priorityConfigs = Lists.newArrayList();

    public boolean productInstalled(String productCode) {
        return installedProducts.stream().filter(p -> StringUtils.equals(p.getCode(), productCode))
                .findFirst().isPresent();
    }

    public boolean notContainExtCode(String extCode) {
        return !priorityConfigs.stream()
                .filter(p -> StringUtils.equals(extCode, p.getExtCode()))
                .findFirst().isPresent();
    }

    public List<ExtPriority> getProductConfigByExtCode(String extCode, boolean isOnlyProduct) {
        PriorityConfig priorityConfig = priorityConfigs.stream()
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

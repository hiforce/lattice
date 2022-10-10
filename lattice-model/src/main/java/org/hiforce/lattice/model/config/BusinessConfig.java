package org.hiforce.lattice.model.config;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Rocky Yu
 * @since 2022/9/19
 */
@NoArgsConstructor
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
    private final Set<ProductConfig> products = Sets.newHashSet();

    @Getter
    private List<ExtPriorityConfig> extensions = Lists.newArrayList();

    public BusinessConfig(String bizCode, int priority, Set<ProductConfig> products, List<ExtPriorityConfig> extensions) {
        this.bizCode = bizCode;
        this.priority = priority;
        this.products.addAll(products);
        this.extensions.addAll(extensions);
    }

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

    public ExtPriorityConfig getExtPriorityConfigByExtCode(String extCode) {
        return extensions.stream()
                .filter(p -> StringUtils.equals(extCode, p.getExtCode()))
                .findFirst().orElse(null);
    }

    /**
     * @param extCode        The code of extension
     * @param onlyHorizontal only load the Horizontal type template.
     * @return found extension priority config.
     */
    public List<ExtPriority> getExtPriorityByCode(String extCode, boolean onlyHorizontal) {
        ExtPriorityConfig priorityConfig = extensions.stream()
                .filter(p -> StringUtils.equals(extCode, p.getExtCode()))
                .findFirst().orElse(null);
        if (null == priorityConfig) {
            return null;
        }
        List<ExtPriority> priorities = priorityConfig.getPriorities().stream()
                .filter(p -> onlyHorizontal ? p.getType().isHorizontal() : true)
                .collect(Collectors.toList());
        return priorities;
    }

}

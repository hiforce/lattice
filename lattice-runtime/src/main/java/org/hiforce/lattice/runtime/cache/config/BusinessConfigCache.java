package org.hiforce.lattice.runtime.cache.config;

import com.google.common.collect.Lists;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.hiforce.lattice.model.config.BusinessConfig;
import org.hiforce.lattice.model.config.ReadonlyBusinessConfig;
import org.hiforce.lattice.runtime.cache.LatticeCache;

import java.util.List;

/**
 * @author Rocky Yu
 * @since 2022/10/12
 */
public class BusinessConfigCache implements LatticeCache {

    private static BusinessConfigCache instance;

    @Getter
    private final List<BusinessConfig> businessConfigs = Lists.newArrayList();

    private BusinessConfigCache() {

    }

    public static BusinessConfigCache getInstance() {
        if (null == instance) {
            instance = new BusinessConfigCache();
        }
        return instance;
    }

    @Override
    public void init() {

    }

    @Override
    public void clear() {
        businessConfigs.clear();
    }

    public void addBusinessConfigs(List<BusinessConfig> configs) {
        businessConfigs.addAll(configs);
    }

    public void removeBusinessConfig(String bizCode) {
        businessConfigs.removeIf(p -> StringUtils.equals(bizCode, p.getBizCode()));
    }

    public BusinessConfig getBusinessConfigByBizCode(String bizCode) {
        BusinessConfig config = businessConfigs.stream().filter(p -> StringUtils.equals(bizCode, p.getBizCode()))
                .findFirst().orElse(null);
        if (null == config) {
            return null;
        }
        return new ReadonlyBusinessConfig(config.getBizCode(), config.getPriority(),
                config.getProducts(), config.getExtensions());
    }
}

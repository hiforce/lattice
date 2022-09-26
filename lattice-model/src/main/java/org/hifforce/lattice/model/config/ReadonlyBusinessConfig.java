package org.hifforce.lattice.model.config;

import org.hifforce.lattice.exception.LatticeRuntimeException;

import java.util.List;
import java.util.Set;

/**
 * @author Rocky Yu
 * @since 2022/9/21
 */
public class ReadonlyBusinessConfig extends BusinessConfig {

    private static final long serialVersionUID = 248112387639393117L;

    public ReadonlyBusinessConfig(String bizCode, int priority, Set<ProductConfig> products, List<ExtPriorityConfig> priorities) {
        super(bizCode, priority, products, priorities);
    }

    public void setBizCode(String bizCode) {
        throw new LatticeRuntimeException("LATTICE-CORE-003");
    }

    public void setInstalledProducts(List<ProductConfig> installedProducts) {
        throw new LatticeRuntimeException("LATTICE-CORE-003");
    }

    public void setPriorityConfigs(List<ExtPriorityConfig> priorityConfigs) {
        throw new LatticeRuntimeException("LATTICE-CORE-003");
    }
}

package org.hiforce.lattice.spi.config;

import org.hiforce.lattice.model.config.BusinessConfig;

import java.util.List;

/**
 * @author Rocky Yu
 * @since 2022/9/26
 */
public interface BusinessConfigLoadSpi {

    void setClassLoader(ClassLoader classLoader);

    /**
     * @return the priority of loader.
     */
    int getPriority();

    /**
     * @return the business config list.
     */
    List<BusinessConfig> loadBusinessConfigs(List<String> bizCodes);
}

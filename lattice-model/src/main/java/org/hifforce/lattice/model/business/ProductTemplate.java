package org.hifforce.lattice.model.business;

import org.hifforce.lattice.model.scenario.ScenarioRequest;

/**
 * @author Rocky Yu
 * @since 2022/9/20
 */
public abstract class ProductTemplate extends Template implements IProduct {

    @Override
    public TemplateType getType() {
        return TemplateType.PRODUCT;
    }


    /**
     * Whether current product effected for specific Scenario.
     *
     * @param request The request of Scenario.
     * @return true or false.
     */
    public abstract boolean isEffect(ScenarioRequest request);
}

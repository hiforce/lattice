package org.hiforce.lattice.model.business;

import lombok.Getter;
import lombok.Setter;
import org.hiforce.lattice.model.scenario.ScenarioRequest;

/**
 * @author Rocky Yu
 * @since 2022/9/21
 */
public abstract class BusinessTemplate extends Template implements IBusiness {

    @Getter
    @Setter
    private int priority;

    @Override
    public TemplateType getType() {
        return TemplateType.BUSINESS;
    }

    public boolean isEffect(ScenarioRequest request) {
        return false;
    }
}

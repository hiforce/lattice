package org.hifforce.lattice.model.business;

import org.hifforce.lattice.model.scenario.ScenarioRequest;

/**
 * A Use Case Template is an abstracted Scenario Pattern.
 * About Use Case, You can refer to 'Clean Architecture'
 * <p>
 * UseCase Template is similar with Product, but business no need to install the use case.
 *
 * @author Rocky Yu
 * @since 2022/9/28
 */
public abstract class UseCaseTemplate extends ProductTemplate {

    @Override
    public TemplateType getType() {
        return TemplateType.USE_CASE;
    }


    /**
     * Whether current use case effected for specific Scenario.
     *
     * @param request The request of Scenario.
     * @return true or false.
     */
    public abstract boolean isEffect(ScenarioRequest request);
}

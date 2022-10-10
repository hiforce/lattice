package org.hiforce.lattice.model.business;

import org.hiforce.lattice.model.scenario.ScenarioRequest;

/**
 * @author Rocky Yu
 * @since 2022/9/18
 */
public interface ITemplate {

    String getCode();

    TemplateType getType();

    boolean isEffect(ScenarioRequest request);


    Long getInternalId();


    boolean isPatternTemplateCode();

}

package org.hifforce.lattice.model.scenario;

import org.hifforce.lattice.model.business.IBizObject;

import java.io.Serializable;

/**
 * @author Rocky Yu
 * @since 2022/9/20
 */
public interface ScenarioRequest extends Serializable {

    /**
     * @return the business object in current scenario.
     */
    IBizObject getBizObject();
}

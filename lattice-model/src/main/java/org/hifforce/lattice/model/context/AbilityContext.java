package org.hifforce.lattice.model.context;

import lombok.Getter;
import lombok.Setter;
import org.hifforce.lattice.model.business.IBizObject;

import java.io.Serializable;

/**
 * @author Rocky Yu
 * @since 2022/9/18
 */
public class AbilityContext implements Serializable {

    private static final long serialVersionUID = 5369006327755731627L;

    @Getter
    private final IBizObject bizObject;

    @Getter
    @Setter
    private String scenario;

    @Getter
    @Setter
    private boolean onlyProductExt;

    public AbilityContext(IBizObject bizObject, String scenario) {
        this.bizObject = bizObject;
        this.scenario = scenario;
    }
}

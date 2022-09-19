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
    private final String scenario;

    @Getter
    private final String bizCode;

    @Getter
    @Setter
    private boolean onlyProductExt;

    public AbilityContext(IBizObject bizObject) {
        this.bizObject = bizObject;
        this.scenario = bizObject.getBizContext().getScenario();
        this.bizCode = bizObject.getBizCode();
    }
}

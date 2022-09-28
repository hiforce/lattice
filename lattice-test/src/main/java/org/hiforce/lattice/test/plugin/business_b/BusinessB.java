package org.hiforce.lattice.test.plugin.business_b;

import org.hifforce.lattice.annotation.Business;
import org.hifforce.lattice.model.business.BusinessTemplate;
import org.hifforce.lattice.model.scenario.ScenarioRequest;

/**
 * @author Rocky Yu
 * @since 2022/9/21
 */
@Business(code = BusinessB.BUSINESS_B_CODE, name = "Business B")
public class BusinessB extends BusinessTemplate {
    public static final String BUSINESS_B_CODE = "business.b";

    @Override
    public boolean isEffect(ScenarioRequest request) {
        return true;
    }
}

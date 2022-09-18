package org.hiforce.lattice.test.ability;

import lombok.NoArgsConstructor;
import org.hifforce.lattice.annotation.Ability;
import org.hifforce.lattice.model.business.IBizObject;
import org.hifforce.lattice.model.context.AbilityContext;
import org.hiforce.lattice.runtime.ability.BaseLatticeAbility;
import org.hiforce.lattice.test.ability.ext.BlankSampleBusinessExt;

/**
 * @author Rocky Yu
 * @since 2022/9/16
 */
@NoArgsConstructor
@Ability(name = "SampleAbility")
public class SampleAbility extends BaseLatticeAbility<Object, BlankSampleBusinessExt> {

    private IBizObject bizObject;

    private String scenario;


    public SampleAbility(IBizObject bizObject, String scenario) {
        this.bizObject = bizObject;
        this.scenario = scenario;
    }

    @Override
    public BlankSampleBusinessExt getDefaultRealization(String bizCode, Object o) {
        return new BlankSampleBusinessExt();
    }

    @Override
    public AbilityContext getContext() {
        return new AbilityContext(bizObject, scenario);
    }
}

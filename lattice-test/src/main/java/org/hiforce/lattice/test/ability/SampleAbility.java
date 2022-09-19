package org.hiforce.lattice.test.ability;

import org.hifforce.lattice.annotation.Ability;
import org.hifforce.lattice.model.business.IBizObject;
import org.hiforce.lattice.runtime.ability.BaseLatticeAbility;
import org.hiforce.lattice.test.ability.ext.BlankSampleBusinessExt;

/**
 * @author Rocky Yu
 * @since 2022/9/16
 */
@Ability(name = "SampleAbility")
public class SampleAbility extends BaseLatticeAbility<BlankSampleBusinessExt> {

    public SampleAbility(IBizObject bizObject, String scenario) {
        super(bizObject, scenario);
    }

    @Override
    public BlankSampleBusinessExt getDefaultRealization() {
        return new BlankSampleBusinessExt();
    }


}

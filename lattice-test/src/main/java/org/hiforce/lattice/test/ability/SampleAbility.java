package org.hiforce.lattice.test.ability;

import org.hifforce.lattice.annotation.Ability;
import org.hiforce.lattice.runtime.ability.BaseLatticeAbility;
import org.hiforce.lattice.test.ability.ext.BlankSampleBusinessExt;

/**
 * @author Rocky Yu
 * @since 2022/9/16
 */
@Ability(name = "SampleAbility")
public class SampleAbility extends BaseLatticeAbility<Object, BlankSampleBusinessExt> {

    @Override
    public BlankSampleBusinessExt getDefaultRealization(String bizCode, Object o) {
        return new BlankSampleBusinessExt();
    }
}

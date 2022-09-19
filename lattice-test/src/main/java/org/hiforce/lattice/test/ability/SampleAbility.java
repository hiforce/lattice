package org.hiforce.lattice.test.ability;

import org.hifforce.lattice.annotation.Ability;
import org.hifforce.lattice.model.business.IBizObject;
import org.hiforce.lattice.runtime.ability.BaseLatticeAbility;
import org.hiforce.lattice.runtime.ability.reduce.Reducers;
import org.hiforce.lattice.test.ability.ext.BlankSampleBusinessExt;

import java.util.Objects;

import static org.hiforce.lattice.test.ability.ext.SampleBusinessExt.SAMPLE_GET_SAMPLE_EXTENSION_POINT_01;

/**
 * @author Rocky Yu
 * @since 2022/9/16
 */
@Ability(name = "SampleAbility")
public class SampleAbility extends BaseLatticeAbility<BlankSampleBusinessExt> {

    public SampleAbility(IBizObject bizObject) {
        super(bizObject);
    }

    @Override
    public BlankSampleBusinessExt getDefaultRealization() {
        return new BlankSampleBusinessExt();
    }

    public String invokeTheSampleSampleExtensionPoint_01() {

        return this.reduceExecute(SAMPLE_GET_SAMPLE_EXTENSION_POINT_01, p -> p.getSampleExtensionPoint_01("test"),
                Reducers.firstOf(Objects::nonNull));
    }

}

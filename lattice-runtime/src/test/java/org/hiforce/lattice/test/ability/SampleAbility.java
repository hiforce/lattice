package org.hiforce.lattice.test.ability;

import org.hifforce.lattice.annotation.Ability;
import org.hifforce.lattice.model.business.IBizObject;
import org.hiforce.lattice.runtime.ability.BaseLatticeAbility;
import org.hiforce.lattice.runtime.ability.reduce.Reducers;
import org.hiforce.lattice.test.ability.ext.BlankSampleAbilityExt;

import java.util.Objects;

@Ability(name = "SampleAbility")
public class SampleAbility extends BaseLatticeAbility<BlankSampleAbilityExt> {
    public SampleAbility(IBizObject bizObject) {
        super(bizObject);
    }


    public void invokeExtension() {
        String value = this.reduceExecute(
                extension -> extension.sampleExtensionInvoke("Rocky", "Yu"),
                Reducers.firstOf(Objects::nonNull));
        System.out.println(value);
    }

    @Override
    public BlankSampleAbilityExt getDefaultRealization() {
        return new BlankSampleAbilityExt();
    }
}

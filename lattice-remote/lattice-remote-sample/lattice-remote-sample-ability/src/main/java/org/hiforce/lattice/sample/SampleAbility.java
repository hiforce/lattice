package org.hiforce.lattice.sample;

import lombok.extern.slf4j.Slf4j;
import org.hifforce.lattice.annotation.Ability;
import org.hifforce.lattice.model.business.IBizObject;
import org.hiforce.lattice.runtime.ability.BaseLatticeAbility;
import org.hiforce.lattice.runtime.ability.reduce.Reducers;
import org.hiforce.lattice.sample.sdk.BlankSampleBusinessExt;

import java.util.Objects;

import static org.hiforce.lattice.sample.sdk.SampleBusinessExt.EXT_HELLO;


/**
 * @author Rocky Yu
 * @since 2022/9/16
 */
@Slf4j
@Ability(name = "SampleAbility")
public class SampleAbility extends BaseLatticeAbility<BlankSampleBusinessExt> {

    public SampleAbility(IBizObject bizObject) {
        super(bizObject);
    }

    @Override
    public BlankSampleBusinessExt getDefaultRealization() {
        return new BlankSampleBusinessExt();
    }

    public String doHello() {

        return this.reduceExecute(EXT_HELLO,
                p -> p.hello("Jack"),
                Reducers.firstOf(Objects::nonNull));
    }

}

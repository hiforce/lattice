package org.hiforce.lattice.test.ability;

import lombok.extern.slf4j.Slf4j;
import org.hifforce.lattice.annotation.Ability;
import org.hiforce.lattice.runtime.ability.BaseLatticeAbility;
import org.hiforce.lattice.runtime.ability.reduce.Reducers;
import org.hiforce.lattice.test.ability.ext.BlankSampleBusinessExt;
import org.hiforce.lattice.test.model.OrderLine;

import java.util.Objects;

import static org.hiforce.lattice.test.ability.ext.SampleBusinessExt.SAMPLE_GET_SAMPLE_EXTENSION_POINT_01;

/**
 * @author Rocky Yu
 * @since 2022/9/16
 */
@Slf4j
@Ability(name = "SampleAbility")
public class SampleAbility extends BaseLatticeAbility<BlankSampleBusinessExt> {

    public SampleAbility(OrderLine orderLine) {
        super(orderLine);
    }

    @Override
    public BlankSampleBusinessExt getDefaultRealization() {
        return new BlankSampleBusinessExt();
    }

    public String invokeTheSampleSampleExtensionPoint_01() {

        String value = this.reduceExecute(SAMPLE_GET_SAMPLE_EXTENSION_POINT_01,
                p -> p.getSampleExtensionPoint_01("test"),
                Reducers.firstOf(Objects::nonNull));
        log.error(">>>>> Invoke SAMPLE_GET_SAMPLE_EXTENSION_POINT_01: " + value);
        return value;
    }

}

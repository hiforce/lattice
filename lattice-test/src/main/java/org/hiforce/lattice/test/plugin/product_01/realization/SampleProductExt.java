package org.hiforce.lattice.test.plugin.product_01.realization;

import org.hifforce.lattice.annotation.Realization;
import org.hiforce.lattice.test.ability.ext.BlankSampleBusinessExt;
import org.hiforce.lattice.test.plugin.product_01.SampleProduct01;

/**
 * @author Rocky Yu
 * @since 2022/9/20
 */
@Realization(codes = SampleProduct01.CODE)
public class SampleProductExt extends BlankSampleBusinessExt {

    @Override
    public String getSampleExtensionPoint_01(String someInput) {
        return "[SampleProduct] execute the getSampleExtensionPoint_01!";
    }
}

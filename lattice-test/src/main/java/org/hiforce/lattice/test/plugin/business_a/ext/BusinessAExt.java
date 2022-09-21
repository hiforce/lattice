package org.hiforce.lattice.test.plugin.business_a.ext;

import org.hifforce.lattice.annotation.Realization;
import org.hiforce.lattice.test.ability.ext.BlankSampleBusinessExt;

import static org.hiforce.lattice.test.plugin.business_a.BusinessA.BUSINESS_A_CODE;

/**
 * @author Rocky Yu
 * @since 2022/9/18
 */
@Realization(codes = BUSINESS_A_CODE)
public class BusinessAExt extends BlankSampleBusinessExt {

    @Override
    public String getSampleExtensionPoint_01(String someInput) {
        return "Hello, Business A Realization Ext 01";
    }
}

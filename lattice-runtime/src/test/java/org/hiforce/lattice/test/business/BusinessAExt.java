package org.hiforce.lattice.test.business;

import org.hiforce.lattice.annotation.Realization;
import org.hiforce.lattice.test.ability.ext.BlankSampleAbilityExt;

@Realization(codes = BusinessA.CODE)
public class BusinessAExt extends BlankSampleAbilityExt {

    @Override
    public String sampleExtensionInvoke(String arg1, String arg2) {
        return String.format("BusinessAExt: %s, %s", arg1, arg2);
    }
}

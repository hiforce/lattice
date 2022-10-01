package org.hiforce.lattice.test.ability.ext;

import org.hifforce.lattice.model.ability.BusinessExt;

public class BlankSampleAbilityExt extends BusinessExt implements SampleAbilityExt {
    @Override
    public String sampleExtensionInvoke(String arg1, String arg2) {
        return "Not Blank Result";
    }
}

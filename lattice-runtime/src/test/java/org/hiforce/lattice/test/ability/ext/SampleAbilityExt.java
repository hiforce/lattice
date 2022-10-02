package org.hiforce.lattice.test.ability.ext;

import org.hifforce.lattice.annotation.Extension;
import org.hifforce.lattice.model.ability.IBusinessExt;

public interface SampleAbilityExt extends IBusinessExt {

    @Extension
    String sampleExtensionInvoke(String arg1, String arg2);
}

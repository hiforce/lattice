package org.hiforce.lattice.test.ability.ext;

import org.hifforce.lattice.annotation.Extension;
import org.hifforce.lattice.model.ability.IBusinessExt;

public interface SampleAbilityExt extends IBusinessExt {

    String EXT_SAMPLE_EXTENSION = "EXT_SAMPLE_EXTENSION";

    @Extension(
            code = EXT_SAMPLE_EXTENSION
    )
    String sampleExtensionInvoke(String arg1, String arg2);
}

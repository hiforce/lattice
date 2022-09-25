package org.hiforce.lattice.test.ability.ext;

import org.hifforce.lattice.annotation.Extension;
import org.hifforce.lattice.annotation.model.ReduceType;
import org.hifforce.lattice.model.ability.IBusinessExt;

/**
 * @author Rocky Yu
 * @since 2022/9/16
 */
public interface SampleBusinessExt extends IBusinessExt {

    String SAMPLE_GET_SAMPLE_EXTENSION_POINT_01 = "SAMPLE#getSampleExtensionPoint_01";

    @Extension(name = "SampleExtension", code = SAMPLE_GET_SAMPLE_EXTENSION_POINT_01, reduceType = ReduceType.FIRST)
    String getSampleExtensionPoint_01(String someInput);
}

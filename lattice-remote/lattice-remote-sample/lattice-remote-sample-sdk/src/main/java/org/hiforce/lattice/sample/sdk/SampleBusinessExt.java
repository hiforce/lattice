package org.hiforce.lattice.sample.sdk;

import org.hifforce.lattice.annotation.Extension;
import org.hifforce.lattice.annotation.model.ProtocolType;
import org.hifforce.lattice.annotation.model.ReduceType;
import org.hifforce.lattice.model.ability.IBusinessExt;

/**
 * @author Rocky Yu
 * @since 2022/9/16
 */
public interface SampleBusinessExt extends IBusinessExt {

    String EXT_HELLO = "SAMPLE#EXT_HELLO";

    @Extension(
            name = "EXT_HELLO",
            code = EXT_HELLO,
            protocolType = ProtocolType.REMOTE,
            reduceType = ReduceType.FIRST
    )
    String hello(String word);
}

package org.hifforce.lattice.cache;

import org.hifforce.lattice.model.register.BaseSpec;

/**
 * @author Rocky Yu
 * @since 2022/9/16
 */
public interface ITemplateCache extends IMultiKeyCache<String, Long, BaseSpec> {

    boolean templateCodeMatched(String code, String specificCode);

}

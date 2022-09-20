package org.hifforce.lattice.cache;

import org.hifforce.lattice.model.business.ITemplate;

/**
 * @author Rocky Yu
 * @since 2022/9/16
 */
public interface ITemplateCache extends IMultiKeyCache<String, Long, ITemplate> {

    boolean templateCodeMatched(ITemplate realization, ITemplate specificTemplate);

    boolean templateCodeMatched(String code, String specificCode);


    boolean templateCodeMatched(String [] codes, String specificCode);
}

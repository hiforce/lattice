package org.hifforce.lattice.model.ability.cache;

import org.hifforce.lattice.model.ability.IBusinessExt;

import java.util.List;

/**
 * @author Rocky Yu
 * @since 2022/9/16
 */
public interface IBusinessExtCache {

    /**
     * Find the BusinessExt Facade by extension code and scenario.
     *
     * @param businessExt Current Parent BusinessExt Facade.
     * @param extCode     Extension's code.
     * @param scenario    Scenario.
     * @return found business extension facade.
     */
    IBusinessExt getCachedBusinessExt(IBusinessExt businessExt, String extCode, String scenario);

    /**
     * Get children of current BusinessExt facade.
     *
     * @param businessExt Current BusinessExt facade.
     * @return All the child BusinessExt of current BusinessExt.
     */
    List<IBusinessExt> getAllSubBusinessExt(IBusinessExt businessExt);
}

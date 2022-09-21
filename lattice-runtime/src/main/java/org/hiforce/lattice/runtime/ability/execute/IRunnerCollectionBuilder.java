package org.hiforce.lattice.runtime.ability.execute;


import org.hifforce.lattice.model.ability.IBusinessExt;
import org.hiforce.lattice.runtime.ability.BaseLatticeAbility;

/**
 * @author Rocky Yu
 * @since 2022/9/19
 */
@SuppressWarnings("all")
public interface IRunnerCollectionBuilder<ExtensionPoints extends IBusinessExt> {

    boolean isSupport(BaseLatticeAbility ability, String extCode);

    <R> RunnerCollection<ExtensionPoints, R> buildCustomRunnerCollection(
            BaseLatticeAbility ability, String extCode);
}

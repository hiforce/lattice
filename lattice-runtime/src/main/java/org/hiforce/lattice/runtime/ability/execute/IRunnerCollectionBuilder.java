package org.hiforce.lattice.runtime.ability.execute;


import org.hifforce.lattice.model.ability.IBusinessExt;
import org.hiforce.lattice.runtime.ability.BaseLatticeAbility;
import org.hiforce.lattice.runtime.session.SessionConfig;

/**
 * @author Rocky Yu
 * @since 2022/9/19
 */
public interface IRunnerCollectionBuilder<ExtensionPoints extends IBusinessExt> {

    boolean isSupport(BaseLatticeAbility ability, String extCode, SessionConfig sessionConfig);

    <R> RunnerCollection<ExtensionPoints, R> buildCustomRunnerCollection(
            BaseLatticeAbility ability, String extCode, SessionConfig sessionConfig);
}

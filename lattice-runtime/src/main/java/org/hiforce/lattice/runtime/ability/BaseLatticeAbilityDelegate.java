package org.hiforce.lattice.runtime.ability;

import org.hifforce.lattice.model.ability.IBusinessExt;
import org.hiforce.lattice.runtime.ability.execute.RunnerCollection;

import javax.annotation.Nonnull;

/**
 * @author Rocky Yu
 * @since 2022/9/16
 */
public class BaseLatticeAbilityDelegate {

    @SuppressWarnings("rawtypes")
    private BaseLatticeAbility ability;

    @SuppressWarnings("rawtypes")
    public BaseLatticeAbilityDelegate(BaseLatticeAbility ability) {
        this.ability = ability;
    }

    public <BusinessExt extends IBusinessExt, R> RunnerCollection<BusinessExt, R> loadExtensionRunners(@Nonnull String extCode) {
        String scenario = ability.getContext().getScenario();
        String bizCode = ability.getContext().getBizObject().getBizCode();

        boolean supportCustomization = ability.supportCustomization();

        return null;
    }
}

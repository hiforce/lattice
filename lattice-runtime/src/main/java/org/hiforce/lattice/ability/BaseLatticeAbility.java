package org.hiforce.lattice.ability;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.hifforce.lattice.model.ability.IAbility;
import org.hifforce.lattice.model.ability.IBusinessExt;
import org.hifforce.lattice.model.business.IBizObject;
import org.hiforce.lattice.ability.reduce.Reducer;

import javax.annotation.Nonnull;

/**
 * @author Rocky Yu
 * @since 2022/9/16
 */
@Slf4j
public abstract class BaseLatticeAbility<Target, BusinessExt extends IBusinessExt>
        implements IAbility<Target, BusinessExt> {


    @Getter
    @Setter
    private String abilityCode;

    @Getter
    private final String instanceCode;

    private BaseLatticeAbilityDelegate delegate;

    public BaseLatticeAbility() {
        this.instanceCode = this.getClass().getName();
        this.delegate = new BaseLatticeAbilityDelegate(this);
    }


    @Override
    public boolean supportChecking(String bizCode, Target target) {
        return true;
    }

    @Override
    public boolean supportCustomization(String bizCode, Target target) {
        return true;
    }

    @Override
    public boolean hasDefaultExtension(String bizCode, Target target) {
        return true;
    }

    /**
     * The ability will execute the extension's realization with reducer
     * When multi extension realization found.
     *
     * @param bizObject
     * @param extensionCode
     * @param callback
     * @param reducer
     * @return
     */
    public final <T, R> R reduceExecute(@Nonnull IBizObject bizObject,
                                        String extensionCode,
                                        ExtensionCallback<BusinessExt, T> callback,
                                        @Nonnull Reducer<T, R> reducer) {

        return null;
    }
}

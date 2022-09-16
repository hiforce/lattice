package org.hiforce.lattice.runtime.ability.provider;

import org.hifforce.lattice.model.ability.IAbility;
import org.hifforce.lattice.model.ability.provider.IAbilityProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.google.common.collect.Maps.newHashMap;

/**
 * @author Rocky Yu
 * @since 2022/9/16
 */
@SuppressWarnings("all")
public abstract class BaseAbilityProvider<Target, Ability extends IAbility>
        implements IAbilityProvider<Target, Ability> {


    public List<Ability> realizations = new ArrayList<>();
    private final Map<String, Ability> instanceCodeToRealizations = newHashMap();

    @Override
    public final Ability getRealization(String instanceCode) {
        return instanceCodeToRealizations.get(instanceCode);
    }

    @Override
    public void registerRealization(Ability realization, String instaceCode) {
        if (null == realization)
            return;
        realizations.add(realization);
        instanceCodeToRealizations.putIfAbsent(instaceCode, realization);
    }
}

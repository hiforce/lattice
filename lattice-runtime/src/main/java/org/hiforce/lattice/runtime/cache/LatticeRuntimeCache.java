package org.hiforce.lattice.runtime.cache;

import com.google.auto.service.AutoService;
import lombok.Getter;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.hifforce.lattice.annotation.model.AbilityAnnotation;
import org.hifforce.lattice.cache.ILatticeRuntimeCache;
import org.hifforce.lattice.model.ability.IAbility;
import org.hifforce.lattice.model.ability.cache.IBusinessExtCache;
import org.hifforce.lattice.model.business.BusinessTemplate;
import org.hifforce.lattice.model.business.IProduct;
import org.hifforce.lattice.model.register.AbilitySpec;
import org.hifforce.lattice.model.register.BaseSpec;
import org.hifforce.lattice.model.register.ExtensionPointSpec;
import org.hifforce.lattice.model.register.RealizationSpec;
import org.hifforce.lattice.sequence.SequenceGenerator;
import org.hiforce.lattice.runtime.Lattice;
import org.hiforce.lattice.runtime.ability.cache.BusinessExtCache;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * @author Rocky Yu
 * @since 2022/9/16
 */
@SuppressWarnings("unused")
@AutoService(ILatticeRuntimeCache.class)
public class LatticeRuntimeCache extends SimpleCache implements ILatticeRuntimeCache {

    public TemplateCache CACHE_TEMPLATE_REALIZATION = new TemplateCache();

    private final Map<Class<?>, Map<Long, Object>> abilityExtRunnerCollectionCache = new ConcurrentHashMap<>();

    @Override
    public IBusinessExtCache getBusinessExtCache() {
        return BusinessExtCache.getInstance();
    }


    public AbilitySpec doCacheAbilitySpec(AbilityAnnotation ability, Class<?> targetClass) {
        String abilityCode = StringUtils.isEmpty(ability.getCode()) ? targetClass.getName() : ability.getCode();
        AbilitySpec abilitySpec = getAbilitySpecEntry(abilityCode);
        if (null == abilitySpec) {
            abilitySpec = AbilitySpec.of(abilityCode, ability.getName(), ability.getDesc());
            abilitySpec.setAbilityClass(targetClass);
            if (StringUtils.isNotEmpty(ability.getParent())) {
                abilitySpec.setParentCode(ability.getParent());
            }
            return doCacheObjectAbilitySpec(abilityCode, abilitySpec);
        }
        return abilitySpec;
    }

    public Collection<AbilitySpec> getAllCachedAbilities() {
        return getAllCacheAbilitySpec();
    }


    public TemplateCache getTemplateCache() {
        return CACHE_TEMPLATE_REALIZATION;
    }


    @SuppressWarnings("rawtypes")
    public Object getCachedExtensionRunner(IAbility ability, ExtensionRunnerCacheKey key) {
        Map<Long, Object> cache = abilityExtRunnerCollectionCache.get(ability.getClass());
        if (null == cache) {
            return null;
        }
        return cache.get(key.getUniqueId());
    }

    @Getter
    private boolean extensionRunnerCacheReady = false;

    public static Map<String, Long> BIZ_CODE_IDX_MAP = new ConcurrentHashMap<>(120);

    public static Map<String, Long> SCENARIO_IDX_MAP = new ConcurrentHashMap<>(120);

    public synchronized void buildExtensionRunnerCache() {
        if (extensionRunnerCacheReady)
            return;

        List<ExtensionPointSpec> extensions = Lattice.getInstance().getAllRegisteredAbilities().stream()
                .flatMap(p -> p.getAbilityInstances().stream())
                .flatMap(p -> p.getExtensions().stream()).collect(Collectors.toList());

        extensions.forEach(p -> getExtensionSpecCache().put(p.getCode().intern(), p.getInternalId(), p));

        Lattice.getInstance().getAllRegisteredBusinesses().stream()
                .map(BaseSpec::getCode)
                .forEach(bizCode -> BIZ_CODE_IDX_MAP.put(bizCode, SequenceGenerator.next(BusinessTemplate.class.getName())));

        long scenarioIdx = 1;
        List<RealizationSpec> realizationSpecs = Lattice.getInstance().getAllRealizations();
        for (RealizationSpec realizationSpec : realizationSpecs) {
            if (StringUtils.isEmpty(realizationSpec.getScenario()))
                continue;
            SCENARIO_IDX_MAP.putIfAbsent(realizationSpec.getScenario(), scenarioIdx++);
        }

        LatticeRuntimeCache latticeRuntimeCache = Lattice.getInstance().getLatticeRuntimeCache();

        Lattice.getInstance().getAllRegisteredProducts()
                .forEach(p -> latticeRuntimeCache.getTemplateCache()
                        .put(p.getCode(), SequenceGenerator.next(IProduct.class.getName()), p));

        Lattice.getInstance().getAllRegisteredBusinesses()
                .forEach(p -> latticeRuntimeCache.getTemplateCache()
                        .put(p.getCode(), SequenceGenerator.next(IProduct.class.getName()), p));

        extensionRunnerCacheReady = true;
    }

    @SuppressWarnings("rawtypes")
    public void doCacheExtensionRunner(IAbility ability, ExtensionRunnerCacheKey key, Object runner) {
        Map<Long, Object> cache = abilityExtRunnerCollectionCache.get(ability.getClass());
        if (MapUtils.isEmpty(cache)) {
            cache = new ConcurrentHashMap<>(200);
            abilityExtRunnerCollectionCache.put(ability.getClass(), cache);
        }
        cache.put(key.getUniqueId(), runner);
    }
}

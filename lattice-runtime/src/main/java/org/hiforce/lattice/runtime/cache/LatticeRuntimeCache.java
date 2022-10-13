package org.hiforce.lattice.runtime.cache;

import com.google.auto.service.AutoService;
import lombok.Getter;
import org.hiforce.lattice.cache.ILatticeRuntimeCache;
import org.hiforce.lattice.runtime.ability.cache.BusinessExtCache;
import org.hiforce.lattice.runtime.cache.ability.AbilityCache;
import org.hiforce.lattice.runtime.cache.config.BusinessConfigCache;
import org.hiforce.lattice.runtime.cache.exension.ExtensionInvokeCache;
import org.hiforce.lattice.runtime.cache.exension.ExtensionCache;
import org.hiforce.lattice.runtime.cache.index.TemplateIndex;

/**
 * @author Rocky Yu
 * @since 2022/9/16
 */
@SuppressWarnings("unused")
@AutoService(ILatticeRuntimeCache.class)
public class LatticeRuntimeCache implements ILatticeRuntimeCache, LatticeCache {

    @Getter
    private final TemplateIndex templateIndex = TemplateIndex.getInstance();

    @Getter
    private final ExtensionCache extensionCache = ExtensionCache.getInstance();

    @Getter
    private final AbilityCache abilityCache = AbilityCache.getInstance();

    @Getter
    private final BusinessConfigCache businessConfigCache = BusinessConfigCache.getInstance();

    @Getter
    private final ExtensionInvokeCache invokeCache = ExtensionInvokeCache.getInstance();

    @Getter
    private final BusinessExtCache businessExtCache = BusinessExtCache.getInstance();


    @Override
    public synchronized void init() {
        getAbilityCache().init();
        getExtensionCache().init();
        getTemplateIndex().init();
        getInvokeCache().init();
        getBusinessConfigCache().init();
        getBusinessExtCache().init();
    }

    public void clear() {
        getTemplateIndex().clear();
        getExtensionCache().clear();
        getAbilityCache().clear();
        getInvokeCache().clear();
        getBusinessConfigCache().clear();
        getBusinessExtCache().clear();
    }
}

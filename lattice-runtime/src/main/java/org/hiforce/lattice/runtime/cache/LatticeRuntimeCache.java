package org.hiforce.lattice.runtime.cache;

import com.google.auto.service.AutoService;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.hiforce.lattice.cache.ILatticeRuntimeCache;
import org.hiforce.lattice.runtime.ability.cache.BusinessExtCache;
import org.hiforce.lattice.runtime.ability.register.TemplateRegister;
import org.hiforce.lattice.runtime.cache.ability.AbilityCache;
import org.hiforce.lattice.runtime.cache.config.BusinessConfigCache;
import org.hiforce.lattice.runtime.cache.exension.ExtensionCache;
import org.hiforce.lattice.runtime.cache.exension.ExtensionInvokeCache;
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

    public synchronized void clearProductCache(String code) {
        TemplateRegister.getInstance().getProducts().removeIf(p -> StringUtils.equals(p.getCode(), code));
        TemplateRegister.getInstance().getRealizations().removeIf(p -> StringUtils.equals(p.getCode(), code));
        TemplateIndex.getInstance().remove(code);
        ExtensionInvokeCache.getInstance().clear();
        BusinessExtCache.getInstance().clear();
        AbilityCache.getInstance().clear();
    }

    public synchronized void clearBusinessCache(String bizCode) {
        TemplateRegister.getInstance().getBusinesses().removeIf(p -> StringUtils.equals(p.getCode(), bizCode));
        TemplateRegister.getInstance().getRealizations().removeIf(p -> StringUtils.equals(p.getCode(), bizCode));
        TemplateIndex.getInstance().remove(bizCode);
        ExtensionInvokeCache.getInstance().clear();
        BusinessConfigCache.getInstance().removeBusinessConfig(bizCode);
        BusinessExtCache.getInstance().clear();
        AbilityCache.getInstance().clear();
    }


    @Override
    public synchronized void init() {
        getAbilityCache().init();
        getExtensionCache().init();
        getTemplateIndex().init();
        getInvokeCache().init();
        getBusinessConfigCache().init();
        getBusinessExtCache().init();
    }

    public synchronized void clear() {
        getTemplateIndex().clear();
        getExtensionCache().clear();
        getAbilityCache().clear();
        getInvokeCache().clear();
        getBusinessConfigCache().clear();
        getBusinessExtCache().clear();
    }
}

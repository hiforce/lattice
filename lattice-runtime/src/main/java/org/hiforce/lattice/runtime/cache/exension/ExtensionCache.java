package org.hiforce.lattice.runtime.cache.exension;

import com.google.common.collect.Maps;
import lombok.Getter;
import org.hiforce.lattice.model.register.ExtensionSpec;
import org.hiforce.lattice.runtime.Lattice;
import org.hiforce.lattice.runtime.cache.LatticeCache;
import org.hiforce.lattice.runtime.cache.index.ExtensionIndex;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Rocky Yu
 * @since 2022/10/12
 */
public class ExtensionCache implements LatticeCache {

    private static ExtensionCache instance;

    private final Map<String, ExtensionSpec> extensionSpecCache = Maps.newConcurrentMap();

    @Getter
    private final ExtensionIndex extensionIndex = new ExtensionIndex();


    private ExtensionCache() {

    }

    public static ExtensionCache getInstance() {
        if (null == instance) {
            instance = new ExtensionCache();
        }
        return instance;
    }

    public void doCacheExtensionSpec(Set<ExtensionSpec> extensionSet) {
        extensionSet.forEach(p -> extensionSpecCache.put(p.getCode(), p));
    }

    public ExtensionSpec getExtensionSpecByCode(String extCode) {
        return extensionSpecCache.get(extCode);
    }

    @Override
    public void init() {
        List<ExtensionSpec> extensions = Lattice.getInstance().getAllRegisteredAbilities().stream()
                .flatMap(p -> p.getAbilityInstances().stream())
                .flatMap(p -> p.getExtensions().stream()).collect(Collectors.toList());
        extensions.forEach(p -> extensionIndex.put(p.getCode().intern(), p.getInternalId(), p));
    }

    @Override
    public void clear() {
        extensionIndex.clear();
        extensionSpecCache.clear();
    }
}

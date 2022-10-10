package org.hiforce.lattice.cache;

import java.util.Optional;
import java.util.ServiceLoader;
import java.util.stream.StreamSupport;

/**
 * @author Rocky Yu
 * @since 2022/9/16
 */
public class LatticeCacheFactory {

    private static LatticeCacheFactory instance;

    private ILatticeRuntimeCache runtimeCache;

    private LatticeCacheFactory() {

    }

    public static LatticeCacheFactory getInstance() {
        if (null == instance) {
            synchronized (LatticeCacheFactory.class) {
                if (null == instance) {
                    instance = new LatticeCacheFactory();
                }
            }
        }
        return instance;
    }

    public ILatticeRuntimeCache getRuntimeCache() {
        if (null != runtimeCache) {
            return runtimeCache;
        }
        return getRuntimeCache(null);
    }

    public ILatticeRuntimeCache getRuntimeCache(ILatticeRuntimeCache defaultCache) {
        if (null != runtimeCache) {
            return runtimeCache;
        }
        synchronized (LatticeCacheFactory.class) {
            if (null == runtimeCache) {
                ClassLoader mainClassLoader = LatticeCacheFactory.class.getClassLoader();
                ServiceLoader<ILatticeRuntimeCache> serializers = ServiceLoader.load(ILatticeRuntimeCache.class,mainClassLoader);
                final Optional<ILatticeRuntimeCache> serializer = StreamSupport.stream(serializers.spliterator(), false)
                        .findFirst();
                runtimeCache = serializer.orElse(defaultCache);
            }
        }
        return runtimeCache;
    }
}

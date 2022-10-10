package org.hiforce.lattice.runtime.cache;


import org.hiforce.lattice.cache.IMultiKeyCache;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author zhenxin.yzx ( Rocky )
 * @since 2020/1/29
 */
public abstract class MultiKeyCache<K1, K2, V> implements IMultiKeyCache<K1, K2, V> {

    protected Map<K1, V> firstKeyCacheMap;

    protected Map<K2, V> secondKeyCacheMap;

    protected Map<K1, K2> firstKey2SecondKeyMap;

    public int size() {
        if (null == firstKeyCacheMap)
            return 0;
        return firstKeyCacheMap.size();
    }

    public void clear() {
        firstKeyCacheMap.clear();
        secondKeyCacheMap.clear();
        firstKey2SecondKeyMap.clear();
    }

    public void remove(K1 key1) {
        V v = firstKeyCacheMap.remove(key1);
        if (null != v) {
            K2 key2 = firstKey2SecondKeyMap.remove(key1);
            if (null != key2) {
                secondKeyCacheMap.remove(key2);
            }
        }
    }

    public MultiKeyCache() {
        firstKeyCacheMap = new ConcurrentHashMap<K1, V>(120);
        secondKeyCacheMap = new ConcurrentHashMap<K2, V>(120);
        firstKey2SecondKeyMap = new ConcurrentHashMap<K1, K2>(120);
    }

    public MultiKeyCache(int initialCapacity) {
        firstKeyCacheMap = new ConcurrentHashMap<K1, V>(initialCapacity);
        secondKeyCacheMap = new ConcurrentHashMap<K2, V>(initialCapacity);
        firstKey2SecondKeyMap = new ConcurrentHashMap<K1, K2>(initialCapacity);
    }

    public K2 getSecondKeyViaFirstKey(K1 key1) {
        return firstKey2SecondKeyMap.get(key1);
    }

    public Collection<V> values() {
        return firstKeyCacheMap.values();
    }

    public V put(K1 firstKey, K2 secondKey, V cacheValue) {
        if (null == cacheValue)
            return null;

        if (null != firstKey) {
            firstKeyCacheMap.put(firstKey, cacheValue);
        }
        if (null != secondKey) {
            secondKeyCacheMap.put(secondKey, cacheValue);
        }
        if (null != firstKey && null != secondKey) {
            firstKey2SecondKeyMap.put(firstKey, secondKey);
        }
        return cacheValue;
    }

    /**
     * 优先从First Key中获取数据，如果First Key为空，则会根据Second Key进行获取。
     *
     * @param firstKey  The First Key to find the object in Cache.
     * @param secondKey The Second Key to find the object in Cache.
     * @return The found cached object.
     */
    public V getFirstKeyFirst(K1 firstKey, K2 secondKey) {
        if (null != firstKey) {
            V v = firstKeyCacheMap.get(firstKey);
            if (null != v)
                return v;
        }
        if (null != secondKey) {
            V v = secondKeyCacheMap.get(secondKey);
            if (null != v) {
                return v;
            }
        }
        return null;
    }

    /**
     * 优先从Second Key中获取数据，如果First Key为空，则会根据Second Key进行获取。
     *
     * @param firstKey  The First Key to find the object in Cache.
     * @param secondKey The Second Key to find the object in Cache.
     * @return The found cached object.
     */
    public V getSecondKeyFirst(K1 firstKey, K2 secondKey) {
        if (null != secondKey) {
            V v = secondKeyCacheMap.get(secondKey);
            if (null != v) {
                return v;
            }
        }
        if (null != firstKey) {
            V v = firstKeyCacheMap.get(firstKey);
            if (null != v)
                return v;
        }
        return null;
    }

    /**
     * 只根据First Key 从缓存中查找数据
     *
     * @param firstKey The First Key to find the object in Cache.
     * @return The found cached object.
     */
    public V getKey1Only(K1 firstKey) {
        if (null != firstKey) {
            return firstKeyCacheMap.get(firstKey);
        }
        return null;
    }

    /**
     * 只根据Second Key 从缓存中查找数据
     *
     * @param secondKey The Second Key to find the object in Cache.
     * @return The found cached object.
     */
    public V getKey2Only(K2 secondKey) {
        if (null != secondKey) {
            return secondKeyCacheMap.get(secondKey);
        }
        return null;
    }
}

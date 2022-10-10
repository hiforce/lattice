package org.hiforce.lattice.cache;

/**
 * @author Rocky Yu
 * @since 2022/9/16
 */
public interface IMultiKeyCache<K1, K2, V> {

	K2 getSecondKeyViaFirstKey(K1 key1);
}

package org.hiforce.lattice.cache.invoke;

/**
 * @author Rocky Yu
 * @since 2022/9/20
 */
public class InvokeCacheThreadLocal extends ThreadLocal<InvokeCache> {

    @Override
    protected InvokeCache initialValue() {
        return new InvokeCache();
    }

    @Override
    public void remove() {
        super.remove();
    }
}

package org.hifforce.lattice.cache.invoke;

import lombok.Getter;

/**
 * @author Rocky Yu
 * @since 2022/9/20
 */
public class InvokeCacheThreadLocal extends ThreadLocal<InvokeCache>{

    @Getter
    public boolean init;

    @Override
    protected InvokeCache initialValue() {
        init = true;
        return new InvokeCache();
    }

    @Override
    public void remove() {
        super.remove();
        init = false;
    }
}

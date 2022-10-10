package org.hiforce.lattice.remote.runner.key;

import lombok.Getter;

import java.io.Serializable;
import java.util.Objects;

/**
 * @author Rocky Yu
 * @since 2022/9/19
 */
public class DubboInvokeCacheKey implements Serializable {

    private static final long serialVersionUID = 4349644401835385138L;

    @Getter
    private final String bizCode;

    @Getter
    private final String scenario;

    @Getter
    private final String extensionCode;

    private int hash;

    public DubboInvokeCacheKey(String bizCode, String scenario, String extCode) {
        this.scenario = scenario;
        this.bizCode = bizCode.intern();
        this.extensionCode = extCode;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DubboInvokeCacheKey key = (DubboInvokeCacheKey) o;
        return hash == key.hash && bizCode.equals(key.bizCode)
                && Objects.equals(scenario, key.scenario) && extensionCode.equals(key.extensionCode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(bizCode, scenario, extensionCode, hash);
    }
}

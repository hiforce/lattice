package org.hiforce.lattice.runtime.ability.cache;


import java.util.Objects;

public final class AbilityInstCacheKey {
    private String bizCode;
    private String abilityCode;

    private int hash;

    private AbilityInstCacheKey() {
    }

    public AbilityInstCacheKey(String bizCode, String abilityCode) {
        this.bizCode = bizCode;
        this.abilityCode = abilityCode;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) { return true; }
        if (o == null || getClass() != o.getClass()) { return false; }

        AbilityInstCacheKey that = (AbilityInstCacheKey)o;

        if (!Objects.equals(bizCode, that.bizCode)) { return false; }
        return Objects.equals(abilityCode, that.abilityCode);
    }

    @Override
    public int hashCode() {
        if (hash == 0) {
            int result = bizCode != null ? bizCode.hashCode() : 0;
            hash = 31 * result + (abilityCode != null ? abilityCode.hashCode() : 0);
        }
        return hash;
    }
}

package org.hiforce.lattice.runtime.ability.execute.filter;

import lombok.Getter;
import lombok.Setter;

/**
 * @author Rocky Yu
 * @since 2022/9/21
 */
@SuppressWarnings("unused")
public class ExtensionFilter {
    public static final ExtensionFilter DEFAULT_FILTER = of();

    @Getter
    @Setter
    private boolean loadBusinessExt;//Whether load the business's realization;


    @Getter
    @Setter
    private ProductFilter productFilter;

    public static ExtensionFilter of() {
        return new ExtensionFilter();
    }


    public ExtensionFilter setOnlyProductExt(boolean loadBusinessExt) {
        this.loadBusinessExt = loadBusinessExt;
        return this;
    }
}

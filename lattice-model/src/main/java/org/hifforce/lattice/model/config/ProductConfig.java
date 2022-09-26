package org.hifforce.lattice.model.config;

import lombok.Getter;
import lombok.Setter;

import javax.annotation.Nonnull;
import java.io.Serializable;

/**
 * @author Rocky Yu
 * @since 2022/9/21
 */
public class ProductConfig implements Serializable {

    private static final long serialVersionUID = -8571944467506060106L;

    @Getter
    @Setter
    private String code;

    public static ProductConfig of(@Nonnull String code) {
        ProductConfig productConfig = new ProductConfig();
        productConfig.setCode(code);
        return productConfig;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ProductConfig config = (ProductConfig) o;

        return code.equals(config.code);
    }

    @Override
    public int hashCode() {
        return code.hashCode();
    }
}

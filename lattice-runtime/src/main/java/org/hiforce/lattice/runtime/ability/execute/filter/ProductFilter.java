package org.hiforce.lattice.runtime.ability.execute.filter;

import lombok.Getter;

import java.io.Serializable;
import java.util.Collections;
import java.util.Set;

/**
 * @author Rocky Yu
 * @since 2022/9/21
 */
public class ProductFilter implements Serializable {

    private static final long serialVersionUID = 2203739715380176426L;

    @Getter
    private final Set<String> allowedProductCodes = Collections.emptySet();
}

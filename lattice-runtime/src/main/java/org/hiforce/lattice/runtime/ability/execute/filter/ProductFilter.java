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


    private static final long serialVersionUID = -8023924263923470083L;

    @Getter
    private final Set<String> allowedCodes = Collections.emptySet();
}

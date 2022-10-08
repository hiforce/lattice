package org.hiforce.lattice.maven.model;

import com.google.common.collect.Lists;
import lombok.Getter;

import java.io.Serializable;
import java.util.List;

/**
 * @author Rocky Yu
 * @since 2022/10/8
 */
public class ProductSet implements Serializable {

    private static final long serialVersionUID = -251469187777445811L;

    @Getter
    private final List<ProductInfo> providing = Lists.newArrayList();

    @Getter
    private final List<ProductInfo> using = Lists.newArrayList();
}

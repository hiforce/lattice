package org.hiforce.lattice.maven.model;

import com.google.common.collect.Lists;
import lombok.Getter;

import java.io.Serializable;
import java.util.List;

/**
 * @author Rocky Yu
 * @since 2022/10/8
 */
public class BusinessSet implements Serializable {

    private static final long serialVersionUID = 5900339655498094973L;

    @Getter
    private final List<BusinessInfo> providing = Lists.newArrayList();

    @Getter
    private final List<BusinessInfo> installed = Lists.newArrayList();
}

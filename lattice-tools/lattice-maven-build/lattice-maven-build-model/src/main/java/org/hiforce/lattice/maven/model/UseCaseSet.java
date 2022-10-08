package org.hiforce.lattice.maven.model;

import com.google.common.collect.Lists;
import lombok.Getter;

import java.io.Serializable;
import java.util.List;

/**
 * @author Rocky Yu
 * @since 2022/10/8
 */
public class UseCaseSet implements Serializable {
    private static final long serialVersionUID = -1334102456564721401L;

    @Getter
    private final List<UseCaseInfo> providing = Lists.newArrayList();

    @Getter
    private final List<UseCaseInfo> using = Lists.newArrayList();
}

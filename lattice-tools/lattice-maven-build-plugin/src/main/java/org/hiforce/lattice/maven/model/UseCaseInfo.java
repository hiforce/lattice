package org.hiforce.lattice.maven.model;

import lombok.Getter;
import lombok.Setter;

/**
 * @author Rocky Yu
 * @since 2022/10/7
 */
public class UseCaseInfo extends BaseInfo{

    private static final long serialVersionUID = -5883007527249357936L;

    @Getter
    @Setter
    private DependencyInfo dependency;
}

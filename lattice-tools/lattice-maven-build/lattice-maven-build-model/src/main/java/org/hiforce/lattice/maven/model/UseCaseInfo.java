package org.hiforce.lattice.maven.model;

import com.google.common.collect.Sets;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

/**
 * @author Rocky Yu
 * @since 2022/10/7
 */
public class UseCaseInfo extends BaseInfo {

    private static final long serialVersionUID = -5883007527249357936L;

    @Getter
    @Setter
    private String sdk;

    @Getter
    private final Set<ExtensionInfo> openExtensions = Sets.newHashSet();

    @Getter
    @Setter
    private DependencyInfo dependency;
}

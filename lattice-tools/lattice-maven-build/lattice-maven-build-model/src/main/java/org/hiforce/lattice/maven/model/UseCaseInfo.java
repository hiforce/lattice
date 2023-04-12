package org.hiforce.lattice.maven.model;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Set;

/**
 * @author Rocky Yu
 * @since 2022/10/7
 */
public class UseCaseInfo extends BaseInfo {

    private static final long serialVersionUID = -5883007527249357936L;

    @Getter
    @Setter
    private String rootSdkClass;

    @Getter
    @Setter
    private SDKInfo sdkInfo;

    @Getter
    @Setter
    private int priority;

    @Getter
    private final Set<ExtensionInfo> extensions = Sets.newHashSet();

    @Getter
    private final List<ExtensionInfo> customized = Lists.newArrayList();

    @Getter
    @Setter
    private DependencyInfo dependency;
}

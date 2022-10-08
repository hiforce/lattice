package org.hiforce.lattice.maven.model;

import com.google.common.collect.Lists;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * @author Rocky Yu
 * @since 2022/10/8
 */
public class ProductInfo extends BaseInfo{

    private static final long serialVersionUID = 9058964307592604130L;

    @Getter
    private final List<ExtensionInfo> customized = Lists.newArrayList();

    @Getter
    @Setter
    private DependencyInfo dependency;
}

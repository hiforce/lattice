package org.hiforce.lattice.maven.builder;

import lombok.Getter;
import org.hiforce.lattice.maven.LatticeBuildPlugin;

/**
 * @author Rocky Yu
 * @since 2022/10/6
 */
public abstract class BaseBuilder {

    @Getter
    private final LatticeBuildPlugin plugin;

    @Getter
    private ClassLoader classLoader;

    public BaseBuilder(LatticeBuildPlugin plugin, ClassLoader classLoader) {
        this.plugin = plugin;
        this.classLoader = classLoader;
    }

}

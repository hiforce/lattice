package org.hiforce.lattice.maven.builder;

import com.google.common.collect.Sets;
import lombok.Getter;
import org.hiforce.lattice.maven.LatticeBuildPlugin;

import java.util.List;
import java.util.Set;

/**
 * @author Rocky Yu
 * @since 2022/10/6
 */
public abstract class LatticeInfoBuilder {

    @Getter
    private final LatticeBuildPlugin plugin;

    @Getter
    private final ClassLoader totalClassLoader;

    @Getter
    private final ClassLoader importClassLoader;

    @Getter
    private final ClassLoader projectClassLoader;

    public LatticeInfoBuilder(LatticeBuildPlugin plugin) {

        this.plugin = plugin;
        this.projectClassLoader = plugin.getProjectClassLoader();
        this.totalClassLoader = plugin.getTotalClassLoader();
        this.importClassLoader = plugin.getImportClassLoader();
    }

    public abstract void build();


    @SuppressWarnings("all")
    protected Set<Class> loadTargetClassList(List<String> classNames) {
        Set<Class> classList = Sets.newHashSet();
        for (String name : classNames) {
            try {
                Class<?> abilityClass = getTotalClassLoader().loadClass(name);
                classList.add(abilityClass);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return classList;
    }
}

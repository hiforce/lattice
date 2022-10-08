package org.hiforce.lattice.maven.builder;

import com.google.common.collect.Sets;
import lombok.Getter;
import org.apache.maven.plugin.logging.Log;
import org.hiforce.lattice.maven.LatticeBuildPlugin;
import org.hiforce.lattice.runtime.Lattice;

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

    @SuppressWarnings("all")
    public abstract String getSpiClassName();

    public List<String> getProvidedInfoClassNames() {
        return Lattice.getServiceProviderValues(
                getSpiClassName(), getProjectClassLoader());
    }

    public List<String> getImportInfoClassNames() {
        return Lattice.getServiceProviderValues(
                getSpiClassName(), getImportClassLoader());
    }

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

    public Log getLog() {
        return getPlugin().getLog();
    }
}

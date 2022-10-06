package org.hiforce.lattice.maven.builder;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.hifforce.lattice.model.ability.IAbility;
import org.hiforce.lattice.maven.LatticeBuildPlugin;
import org.hiforce.lattice.runtime.Lattice;

import java.util.Set;

/**
 * @author Rocky Yu
 * @since 2022/10/6
 */
public class AbilityInfoBuilder extends BaseBuilder {

    public AbilityInfoBuilder(LatticeBuildPlugin plugin, ClassLoader classLoader) {
        super(plugin, classLoader);
    }

    public void build() throws MojoExecutionException, MojoFailureException {
        getLog().info(">>> AbilityInfoBuilder build...");
        Set<Class<?>> classSet = Lattice.getServiceProviderClasses(IAbility.class.getName(), getClassLoader());
        classSet.forEach(p -> getLog().info(p.getName()));
    }

    private Log getLog() {
        return getPlugin().getLog();
    }
}

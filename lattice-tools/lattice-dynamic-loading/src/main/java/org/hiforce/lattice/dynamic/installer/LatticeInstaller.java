package org.hiforce.lattice.dynamic.installer;

import org.hiforce.lattice.dynamic.classloader.LatticeClassLoader;
import org.hiforce.lattice.dynamic.model.PluginFileInfo;

/**
 * @author Rocky Yu
 * @since 2022/10/13
 */
public interface LatticeInstaller {

    InstallResult install(LatticeClassLoader classLoader, PluginFileInfo fileInfo);
}

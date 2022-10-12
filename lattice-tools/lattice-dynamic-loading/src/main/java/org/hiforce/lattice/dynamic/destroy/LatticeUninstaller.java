package org.hiforce.lattice.dynamic.destroy;

import org.hiforce.lattice.dynamic.classloader.LatticeClassLoader;
import org.hiforce.lattice.dynamic.model.PluginFileInfo;

/**
 * @author Rocky Yu
 * @since 2022/10/12
 */
public interface LatticeUninstaller {

    DestroyResult uninstall(LatticeClassLoader classLoader, PluginFileInfo fileInfo);
}

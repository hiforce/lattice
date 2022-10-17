package org.hiforce.lattice.dynamic.classloader;

import com.google.auto.service.AutoService;
import lombok.extern.slf4j.Slf4j;
import org.hiforce.lattice.dynamic.LatticeDynamic;
import org.hiforce.lattice.spi.classloader.CustomClassLoaderSpi;

import java.net.URL;

import static org.hiforce.lattice.dynamic.LatticeDynamic.getLatticePluginUrls;

/**
 * @author Rocky Yu
 * @since 2022/10/10
 */
@Slf4j
@AutoService(CustomClassLoaderSpi.class)
public class LatticeDynamicClassLoaderBuilder implements CustomClassLoaderSpi {

    @Override
    public ClassLoader getCustomClassLoader() {
        URL[] urlArrays = getLatticePluginUrls().toArray(new URL[0]);
        log.info(">>> Lattice Dynamic Plug-in installed: " + LatticeDynamic.getInstance().getPluginFileInfos());
        return new LatticeClassLoader(urlArrays, LatticeDynamicClassLoaderBuilder.class.getClassLoader());
    }


}

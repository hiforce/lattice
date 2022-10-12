package org.hiforce.lattice.dynamic.classloader;

import com.google.auto.service.AutoService;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.hiforce.lattice.dynamic.LatticeDynamic;
import org.hiforce.lattice.dynamic.properties.LatticeDynamicProperties;
import org.hiforce.lattice.exception.LatticeRuntimeException;
import org.hiforce.lattice.spi.classloader.CustomClassLoaderSpi;

import java.io.File;
import java.net.URL;
import java.util.List;

/**
 * @author Rocky Yu
 * @since 2022/10/10
 */
@Slf4j
@AutoService(CustomClassLoaderSpi.class)
public class LatticeDynamicClassLoaderBuilder implements CustomClassLoaderSpi {


    @Override
    public ClassLoader getCustomClassLoader() {
        LatticeDynamic.getInstance().init();

        String[] dirs = LatticeDynamicProperties.getInstance().getPluginDirs();
        List<URL> urls = Lists.newArrayList();
        for (String dir : dirs) {
            urls.addAll(buildJarURLList(new File(dir)));
        }
        URL[] urlArrays = urls.toArray(new URL[0]);
        log.info(">>> Lattice Dynamic Plug-in installed: " + LatticeDynamic.getInstance().getPluginFileInfos());
        return new LatticeClassLoader(urlArrays, LatticeDynamicClassLoaderBuilder.class.getClassLoader());
    }

    private List<URL> buildJarURLList(File dir) {
        List<URL> urls = Lists.newArrayList();
        try {
            if (!dir.exists() || !dir.isDirectory()) {
                return Lists.newArrayList();
            }
            File[] files = dir.listFiles();
            if (null == files) {
                return Lists.newArrayList();
            }
            for (File file : files) {
                if (file.isDirectory()) {
                    urls.addAll(buildJarURLList(file));
                }
                if (!file.getPath().endsWith(".jar") && !file.getPath().endsWith(".zip")) {
                    continue;
                }
                LatticeDynamic.getInstance().loadFile(file);
                urls.add(new URL("file:" + file.getPath()));
            }
            return urls;
        } catch (Exception ex) {
            throw new LatticeRuntimeException(ex);
        }
    }
}

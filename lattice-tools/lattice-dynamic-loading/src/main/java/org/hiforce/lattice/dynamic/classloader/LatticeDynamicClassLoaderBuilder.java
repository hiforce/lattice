package org.hiforce.lattice.dynamic.classloader;

import com.google.auto.service.AutoService;
import com.google.common.collect.Lists;
import org.hiforce.lattice.dynamic.LatticeDynamic;
import org.hiforce.lattice.dynamic.properties.LatticeDynamicProperties;
import org.hiforce.lattice.spi.classloader.CustomClassLoaderSpi;

import java.io.File;
import java.net.URL;
import java.util.List;

/**
 * @author Rocky Yu
 * @since 2022/10/10
 */
@AutoService(CustomClassLoaderSpi.class)
public class LatticeDynamicClassLoaderBuilder implements CustomClassLoaderSpi {


    @Override
    public ClassLoader getCustomClassLoader() {
        LatticeDynamic.getInstance().init();

        String[] dirs = LatticeDynamicProperties.getInstance().getPluginDirs();
        List<URL> urls = Lists.newArrayList();
        for (String dir : dirs) {
            urls.addAll(buildJarURLList(dir));
        }
        URL[] urlArrays = urls.toArray(new URL[0]);
        return new LatticeClassLoader(urlArrays, LatticeDynamicClassLoaderBuilder.class.getClassLoader());
    }

    private List<URL> buildJarURLList(String dirStr) {
        List<URL> urls = Lists.newArrayList();
        try {
            File dir = new File(dirStr);
            if (!dir.exists() || !dir.isDirectory()) {
                return Lists.newArrayList();
            }
            File[] jars = dir.listFiles(file -> file.getPath().endsWith(".jar")
                    || file.getPath().endsWith(".zip"));
            if (null == jars) {
                return urls;
            }
            for (File file : jars) {
                urls.add(new URL("file:" + file.getPath()));
            }
            return urls;
        } catch (Exception ex) {
            return Lists.newArrayList();
        }
    }
}

package org.hiforce.lattice.dynamic.classloader;

import com.google.auto.service.AutoService;
import com.google.common.collect.Lists;
import org.hiforce.lattice.spi.classloader.CustomClassLoaderSpi;
import org.hiforce.lattice.dynamic.properties.LatticeDynamicProperties;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;

/**
 * @author Rocky Yu
 * @since 2022/10/10
 */
@AutoService(CustomClassLoaderSpi.class)
public class LatticeDynamicClassLoaderBuilder implements CustomClassLoaderSpi {

    @Override
    public ClassLoader getCustomClassLoader() {
        String[] dirs = LatticeDynamicProperties.getInstance().getPluginDirs();
        List<URL> urls = Lists.newArrayList();
        for (String dir : dirs) {
            urls.addAll(buildJarURLList(dir));
        }
        URL[] urlArrays = urls.toArray(new URL[0]);
        return new URLClassLoader(urlArrays, LatticeDynamicClassLoaderBuilder.class.getClassLoader());
    }

    private List<URL> buildJarURLList(String dirStr) {
        List<URL> urls = Lists.newArrayList();
        try {
            File dir = new File(dirStr);
            if (!dir.exists() || !dir.isDirectory()) {
                return Lists.newArrayList();
            }
            File[] jars = dir.listFiles(pathname -> pathname.getPath().endsWith(".jar"));
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

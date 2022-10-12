package org.hiforce.lattice.dynamic.classloader;

import java.net.URL;
import java.net.URLClassLoader;

/**
 * @author Rocky Yu
 * @since 2022/10/12
 */
public class LatticeClassLoader extends URLClassLoader {

    public LatticeClassLoader(URL[] urls, ClassLoader parent) {
        super(urls, parent);
    }
}

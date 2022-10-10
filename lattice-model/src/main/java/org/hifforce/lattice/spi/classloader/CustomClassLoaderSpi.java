package org.hifforce.lattice.spi.classloader;

/**
 * @author Rocky Yu
 * @since 2022/10/10
 */
public interface CustomClassLoaderSpi {

    ClassLoader getCustomClassLoader();
}

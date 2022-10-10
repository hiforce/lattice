package org.hifforce.lattice.spi.classloader;

import com.google.common.collect.Lists;
import lombok.Getter;
import org.apache.commons.collections4.iterators.IteratorEnumeration;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.List;
import java.util.Objects;

/**
 * @author Rocky Yu
 * @since 2022/10/10
 */
public class LatticeClassLoader extends ClassLoader {

    @Getter
    private final List<ClassLoader> customLoaders = Lists.newArrayList();

    public LatticeClassLoader(ClassLoader parent) {
        super(parent);
    }

    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException {
        try {
            return super.loadClass(name);
        } catch (ClassNotFoundException ex) {
            for (ClassLoader loader : customLoaders) {
                try {
                    return loader.loadClass(name);
                } catch (ClassNotFoundException ignored) {

                }
            }
            throw new ClassNotFoundException(name);
        }
    }


    @Nullable
    @Override
    public URL getResource(String name) {
        URL url = super.getResource(name);
        if (null != url) {
            return url;
        }
        return getCustomLoaders().stream()
                .map(p -> p.getResource(name))
                .filter(Objects::nonNull)
                .findFirst().orElse(null);
    }

    @Override
    public Enumeration<URL> getResources(String name) throws IOException {
        List<URL> urls = Lists.newArrayList();
        Enumeration<URL> enumeration = super.getResources(name);
        while (enumeration.hasMoreElements()) {
            urls.add(enumeration.nextElement());
        }
        for (ClassLoader classLoader : getCustomLoaders()) {
            enumeration = classLoader.getResources(name);
            while (enumeration.hasMoreElements()) {
                urls.add(enumeration.nextElement());
            }
        }
        return new IteratorEnumeration<>(urls.iterator());
    }


    @Nullable
    @Override
    public InputStream getResourceAsStream(String name) {
        InputStream inputStream = super.getResourceAsStream(name);
        if (null != inputStream) {
            return inputStream;
        }
        return getCustomLoaders().stream()
                .map(p -> p.getResourceAsStream(name))
                .filter(Objects::nonNull)
                .findFirst().orElse(null);
    }
}

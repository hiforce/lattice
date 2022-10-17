package org.hiforce.lattice.dynamic;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.hiforce.lattice.dynamic.classloader.LatticeClassLoader;
import org.hiforce.lattice.dynamic.destroy.*;
import org.hiforce.lattice.dynamic.installer.*;
import org.hiforce.lattice.dynamic.model.PluginFileInfo;
import org.hiforce.lattice.dynamic.properties.LatticeDynamicProperties;
import org.hiforce.lattice.exception.LatticeRuntimeException;
import org.hiforce.lattice.message.Message;
import org.hiforce.lattice.runtime.Lattice;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Rocky Yu
 * @since 2022/10/12
 */
@Slf4j
@Service
@DependsOn(value = "latticeSpringApplicationContextHolder")
@SuppressWarnings("unused")
public class LatticeDynamic implements InitializingBean {

    @Getter
    private static LatticeDynamic instance;

    @Getter
    private final Set<PluginFileInfo> currentFiles = Sets.newConcurrentHashSet();


    private LatticeDynamic() {

    }

    public static List<URL> getLatticePluginUrls() {
        String[] dirs = LatticeDynamicProperties.getInstance().getPluginDirs();
        List<URL> urls = Lists.newArrayList();
        for (String dir : dirs) {
            urls.addAll(buildJarURLList(new File(dir)));
        }
        return urls;
    }

    public void init() {
        currentFiles.clear();
        URL[] urlArrays = getLatticePluginUrls().toArray(new URL[0]);
        log.info(">>> Lattice Dynamic Plug-in installed: " + LatticeDynamic.getInstance().getPluginFileInfos());
        currentFiles.forEach(p -> installPlugin(p, false));
    }

    private static List<URL> buildJarURLList(File dir) {
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

    private PluginFileInfo copyAndCreatePluginFile(PluginFileInfo source) {
        String firstDir = LatticeDynamicProperties.getInstance().getPluginDirs()[0];
        File destDir = new File(firstDir);

        try {
            FileUtils.delete(new File(firstDir + File.separator + source.getFile().getName()));
        } catch (IOException ignored) {

        }

        try {
            FileUtils.copyFileToDirectory(source.getFile(), destDir);
            File destFile = new File(firstDir + File.separator + source.getFile().getName());
            if (!destFile.exists()) {
                throw new LatticeRuntimeException("LATTICE-DYNAMIC-0001");
            }
            return new PluginFileInfo(destFile);
        } catch (LatticeRuntimeException ex) {
            throw ex;
        } catch (Exception e) {
            throw new LatticeRuntimeException(e);
        }
    }

    public synchronized void installPlugin(PluginFileInfo originFile) {
        installPlugin(originFile, true);
    }

    public synchronized void installPlugin(PluginFileInfo originFile, boolean needCopy) {
        if (null == LatticeDynamicProperties.getInstance().getPluginDirs()
                || 0 == LatticeDynamicProperties.getInstance().getPluginDirs().length)
            return;

        PluginFileInfo pluginFile = needCopy ? copyAndCreatePluginFile(originFile) : originFile;

        log.info("Lattice dynamic install plugin: " + pluginFile.getFile().getName());
        List<LatticeInstaller> installers = Lists.newArrayList(
                new BusinessInstaller(),
                new ProductInstaller(),
                new SpringInstaller()
        );

        try {
            URL[] urls = new URL[]{new URL("file:" + pluginFile.getFile().getPath())};
            try (LatticeClassLoader classLoader = new LatticeClassLoader(urls, LatticeDynamic.class.getClassLoader())) {
                InstallResult result = installers.stream()
                        .map(p -> p.install(classLoader, pluginFile))
                        .filter(p -> !p.isSuccess())
                        .findFirst().orElse(null);
                if (null != result) {
                    throw new LatticeRuntimeException(Message.of(result.getErrCode(), result.getErrText()));
                }
                currentFiles.add(pluginFile);
                Lattice.getInstance().reload();
                log.info("....... Lattice plugin " + pluginFile.getFile().getName() + "...installed successfully.");
            }
        } catch (LatticeRuntimeException ex) {
            throw ex;
        } catch (Exception e) {
            throw new LatticeRuntimeException(e);
        }
    }

    public synchronized void uninstallPlugin(String id) {
        PluginFileInfo info = currentFiles.stream().filter(p -> StringUtils.equals(id, p.getId()))
                .findFirst().orElse(null);
        if (null == info) {
            return;
        }

        List<LatticeUninstaller> uninstallers = Lists.newArrayList(
                new BusinessUninstaller(),
                new ProductUninstaller(),
                new SpringUninstaller()
        );

        try {
            URL[] urls = new URL[]{new URL("file:" + info.getFile().getPath())};
            try (LatticeClassLoader classLoader = new LatticeClassLoader(urls, LatticeDynamic.class.getClassLoader())) {
                DestroyResult result = uninstallers.stream()
                        .map(p -> p.uninstall(classLoader, info))
                        .filter(p -> !p.isSuccess())
                        .findFirst().orElse(null);
                if (null != result) {
                    throw new LatticeRuntimeException(Message.of(result.getErrCode(), result.getErrText()));
                }
            }
            info.getFile().delete();
            currentFiles.remove(info);
        } catch (LatticeRuntimeException ex) {
            throw ex;
        } catch (Exception e) {
            throw new LatticeRuntimeException(e);
        }
    }


    public void loadFile(File file) {
        currentFiles.add(new PluginFileInfo(file));
    }

    public String getPluginFileInfos() {
        return currentFiles.stream()
                .map(p -> p.getFile().getName())
                .collect(Collectors.joining(","));
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        instance = this;
        init();
    }
}

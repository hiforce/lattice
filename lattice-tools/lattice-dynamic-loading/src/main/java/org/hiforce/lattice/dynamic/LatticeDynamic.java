package org.hiforce.lattice.dynamic;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.hiforce.lattice.dynamic.classloader.LatticeClassLoader;
import org.hiforce.lattice.dynamic.destroy.BusinessUninstaller;
import org.hiforce.lattice.dynamic.destroy.DestroyResult;
import org.hiforce.lattice.dynamic.destroy.LatticeUninstaller;
import org.hiforce.lattice.dynamic.destroy.ProductUninstaller;
import org.hiforce.lattice.dynamic.installer.BusinessInstaller;
import org.hiforce.lattice.dynamic.installer.InstallResult;
import org.hiforce.lattice.dynamic.installer.LatticeInstaller;
import org.hiforce.lattice.dynamic.installer.ProductInstaller;
import org.hiforce.lattice.dynamic.model.PluginFileInfo;
import org.hiforce.lattice.dynamic.properties.LatticeDynamicProperties;
import org.hiforce.lattice.exception.LatticeRuntimeException;
import org.hiforce.lattice.message.Message;
import org.hiforce.lattice.model.register.BusinessSpec;
import org.hiforce.lattice.runtime.Lattice;
import org.hiforce.lattice.runtime.cache.ability.AbilityCache;

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
@SuppressWarnings("unused")
public class LatticeDynamic {

    private static LatticeDynamic instance;

    @Getter
    private final Set<PluginFileInfo> currentFiles = Sets.newConcurrentHashSet();


    private LatticeDynamic() {

    }

    public static LatticeDynamic getInstance() {
        if (null == instance) {
            instance = new LatticeDynamic();
        }
        return instance;
    }


    public void init() {
        currentFiles.clear();
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
        if (null == LatticeDynamicProperties.getInstance().getPluginDirs()
                || 0 == LatticeDynamicProperties.getInstance().getPluginDirs().length)
            return;

        PluginFileInfo pluginFile = copyAndCreatePluginFile(originFile);
        log.info("Lattice dynamic install plugin: " + pluginFile.getFile().getName());

        List<LatticeInstaller> installers = Lists.newArrayList(
                new BusinessInstaller(),
                new ProductInstaller()
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
                new ProductUninstaller()
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
}

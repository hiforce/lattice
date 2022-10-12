package org.hiforce.lattice.dynamic;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.apache.commons.lang3.StringUtils;
import org.hiforce.lattice.dynamic.classloader.LatticeClassLoader;
import org.hiforce.lattice.dynamic.destroy.BusinessUninstaller;
import org.hiforce.lattice.dynamic.destroy.DestroyResult;
import org.hiforce.lattice.dynamic.destroy.LatticeUninstaller;
import org.hiforce.lattice.dynamic.model.PluginFileInfo;
import org.hiforce.lattice.exception.LatticeRuntimeException;
import org.hiforce.lattice.message.Message;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Rocky Yu
 * @since 2022/10/12
 */
@SuppressWarnings("unused")
public class LatticeDynamic {

    private static LatticeDynamic instance;

    private final Set<PluginFileInfo> currentFiles = Sets.newHashSet();


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

    public void uninstallPlugin(String id) {
        PluginFileInfo info = currentFiles.stream().filter(p -> StringUtils.equals(id, p.getId()))
                .findFirst().orElse(null);
        if (null == info) {
            return;
        }

        List<LatticeUninstaller> uninstallers = Lists.newArrayList(
                new BusinessUninstaller()
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

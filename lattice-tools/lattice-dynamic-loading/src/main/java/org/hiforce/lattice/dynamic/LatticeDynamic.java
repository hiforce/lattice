package org.hiforce.lattice.dynamic;

import com.google.common.collect.Maps;
import org.hiforce.lattice.dynamic.model.PluginFileInfo;

import java.io.File;
import java.util.Map;

/**
 * @author Rocky Yu
 * @since 2022/10/12
 */

public class LatticeDynamic {

    private static LatticeDynamic instance;

    private final Map<String, PluginFileInfo> currentFiles = Maps.newHashMap();


    private final Map<String, PluginFileInfo> previousFiles = Maps.newHashMap();

    private LatticeDynamic() {

    }

    public static LatticeDynamic getInstance() {
        if (null == instance) {
            instance = new LatticeDynamic();
        }
        return instance;
    }

    public void init() {
        previousFiles.clear();
        previousFiles.putAll(currentFiles);
        currentFiles.clear();
    }

    public void loadFile(File file) {
        currentFiles.put(file.getPath(), new PluginFileInfo(file));
    }
}

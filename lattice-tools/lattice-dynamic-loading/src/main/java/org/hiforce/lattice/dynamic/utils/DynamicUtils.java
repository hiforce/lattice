package org.hiforce.lattice.dynamic.utils;

import org.apache.commons.lang3.StringUtils;
import org.hiforce.lattice.dynamic.model.PluginFileInfo;

/**
 * @author Rocky Yu
 * @since 2022/10/13
 */
@SuppressWarnings("all")
public class DynamicUtils {

    public static boolean isPluginDefined(Class targetClass, PluginFileInfo fileInfo) {
        String path = targetClass.getProtectionDomain().getCodeSource()
                .getLocation().getPath();
        return StringUtils.equals(path, fileInfo.getFile().getPath());
    }
}

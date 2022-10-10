package org.hiforce.lattice.dynamic.properties;

import org.apache.commons.lang3.StringUtils;

/**
 * @author Rocky Yu
 * @since 2022/10/9
 */
@SuppressWarnings("ALL")
public class LatticeDynamicProperties {

    private static LatticeDynamicProperties instance;

    private String[] pluginDirs;

    private LatticeDynamicProperties() {

    }

    public static LatticeDynamicProperties getInstance() {
        if (null == instance) {
            instance = new LatticeDynamicProperties();
        }
        return instance;
    }

    public String[] getPluginDirs() {
        if (null == pluginDirs) {
            String value = DynamicApplicationProperties.getValueString("lattice.plugin.dirs");
            if (StringUtils.isEmpty(value)) {
                pluginDirs = new String[]{};
            } else {
                pluginDirs = StringUtils.split(value, ",");
            }
        }
        return pluginDirs;
    }
}

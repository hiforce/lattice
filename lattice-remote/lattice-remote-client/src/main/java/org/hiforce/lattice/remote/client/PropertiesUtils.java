package org.hiforce.lattice.remote.client;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Configurations;
import org.apache.commons.configuration2.ex.ConfigurationException;

/**
 * @author Rocky Yu
 * @since 2022/9/30
 */
@Slf4j
public class PropertiesUtils {
    private static PropertiesConfiguration propertiesConfiguration = null;

    private static void initProperties() {
        Configurations configurations = new Configurations();
        FileBasedConfigurationBuilder.setDefaultEncoding(PropertiesConfiguration.class, "UTF-8");
        try {
            propertiesConfiguration = configurations.properties(
                    PropertiesUtils.class.getClassLoader().getResource("application.properties"));
        } catch (ConfigurationException e) {
            log.error(e.getMessage(), e);
        }
    }

    static {
        initProperties();
    }

    /**
     * 获取String类型的value
     *
     * @param key
     * @return
     */
    public static String getValueString(String key) {
        if (propertiesConfiguration == null) {
            initProperties();
        }
        return propertiesConfiguration.getString(key);
    }

    /**
     * 获取int类型的value
     *
     * @param key
     * @return
     */
    public static int getValueInt(String key) {
        if (propertiesConfiguration == null) {
            initProperties();
        }
        return propertiesConfiguration.getInt(key, 0);
    }
}

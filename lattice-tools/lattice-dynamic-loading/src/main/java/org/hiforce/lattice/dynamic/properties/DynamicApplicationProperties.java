package org.hiforce.lattice.dynamic.properties;

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
public class DynamicApplicationProperties {
    private static PropertiesConfiguration propertiesConfiguration = null;

    private static void initProperties() {
        Configurations configurations = new Configurations();
        FileBasedConfigurationBuilder.setDefaultEncoding(PropertiesConfiguration.class, "UTF-8");
        try {
            propertiesConfiguration = configurations.properties(
                    DynamicApplicationProperties.class.getClassLoader()
                            .getResource("application.properties"));
        } catch (ConfigurationException e) {
            log.error(e.getMessage(), e);
        }
    }

    static {
        initProperties();
    }

    @SuppressWarnings("all")
    public static String getValueString(String key) {
        if (propertiesConfiguration == null) {
            initProperties();
        }
        return propertiesConfiguration.getString(key);
    }

    @SuppressWarnings("all")
    public static int getValueInt(String key) {
        if (propertiesConfiguration == null) {
            initProperties();
        }
        return propertiesConfiguration.getInt(key, 0);
    }
}

package org.hiforce.lattice.tool;

import com.google.auto.service.AutoService;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.hifforce.lattice.model.config.BusinessConfig;
import org.hifforce.lattice.spi.config.BusinessConfigLoadSpi;
import org.hifforce.lattice.utils.JacksonUtils;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Optional;

/**
 * @author Rocky Yu
 * @since 2022/9/26
 */
@Slf4j
@SuppressWarnings("unused")
@AutoService(BusinessConfigLoadSpi.class)
public class BizConfigResourceLoader implements BusinessConfigLoadSpi {

    private ClassLoader classLoader;

    @Override
    public void setClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    @Override
    public int getPriority() {
        return 100;
    }

    @Override
    public List<BusinessConfig> loadBusinessConfigs(List<String> bizCodes) {
        List<BusinessConfig> configs = Lists.newArrayList();
        for (String bizCode : bizCodes) {
            InputStream is = null;
            try {
                String resourceFile = String.format("lattice/lattice-%s.json", bizCode);

                is = Optional.ofNullable(classLoader).orElse(Thread.currentThread().getContextClassLoader()).
                        getResourceAsStream(resourceFile);
                String jsonStr = getStringByInputStream(is);
                if (StringUtils.isEmpty(jsonStr)) {
                    continue;
                }
                log.warn("Lattice business [{}] local config file: {} found!", bizCode, resourceFile);
                BusinessConfig businessConfig = JacksonUtils.deserializeIgnoreException(jsonStr, BusinessConfig.class);
                configs.add(businessConfig);
                log.warn("Lattice business [{}] local config loaded!", bizCode);
            } finally {
                try {
                    if (null != is) {
                        is.close();
                    }
                } catch (Exception ignored) {
                }
            }
        }
        return configs;
    }

    public static String getStringByInputStream(InputStream inputStream) {
        if (null == inputStream) {
            return null;
        }
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        try {
            StringBuilder result = new StringBuilder();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                result.append(line);
            }
            return result.toString();
        } catch (Exception e) {
            try {
                inputStream.close();
                bufferedReader.close();
            } catch (Exception ignored) {
            }
        }
        return null;
    }
}

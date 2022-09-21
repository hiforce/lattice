package org.hiforce.lattice.runtime.cache.key;

import lombok.Getter;
import org.hifforce.lattice.model.register.TemplateSpec;

import java.util.Objects;

/**
 * @author Rocky Yu
 * @since 2022/9/19
 */
public class ExtensionInvokeCacheKey extends BaseObjectCacheKey {

    @Getter
    private final String templateCode;

    @Getter
    private final String scenario;

    @Getter
    private final String extensionCode;

    private int hash;

    public ExtensionInvokeCacheKey(String scenario, TemplateSpec template, String extCode) {
        this.scenario = scenario;
        this.templateCode = template.getCode();
        this.extensionCode = extCode;
        this.setTemplateIndex(template.getInternalId());
        buildObjectCacheUniqueId();
    }

    @Override
    public boolean customEquals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ExtensionInvokeCacheKey key = (ExtensionInvokeCacheKey) o;
        if (scenario != null ? !scenario.equals(key.getScenario()) : key.scenario != null) {
            return false;
        }
        if (!Objects.equals(templateCode, key.templateCode)) {
            return false;
        }
        return getExtensionCode() != null ? getExtensionCode().equals(key.getExtensionCode()) : key.getExtensionCode() == null;
    }

    @Override
    public int hashCode() {
        if (hash == 0) {
            int result = scenario != null ? scenario.hashCode() : 0;
            result = 31 * result + (templateCode != null ? templateCode.hashCode() : 0);
            result = 31 * result + (getExtensionCode() != null ? getExtensionCode().hashCode() : 0);
            hash = result;
        }
        return hash;
    }

    @Override
    public String getBizCode() {
        return null;
    }

    @Override
    public boolean isSupportCustomization() {
        return false;
    }

    @Override
    public boolean isOnlyProduct() {
        return false;
    }

    @Override
    public boolean validateIndex() {
        return null != getTemplateCode() && null != getExtensionCode();
    }
}

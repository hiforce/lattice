package org.hiforce.lattice.model.register;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.hiforce.lattice.annotation.model.ProtocolType;
import org.hiforce.lattice.annotation.model.ReduceType;
import org.hiforce.lattice.sequence.SequenceGenerator;

import java.lang.reflect.Method;

/**
 * @author Rocky Yu
 * @since 2022/9/17
 */
public class ExtensionSpec extends BaseSpec {

    @Getter
    private final long internalId = SequenceGenerator.next(ExtensionSpec.class.getName());

    @Getter
    @Setter
    private int priority;

    @Getter
    private String abilityCode;

    @Getter
    private final Method invokeMethod;

    @Getter
    @Setter
    private ReduceType reduceType = ReduceType.UNKNOWN;

    @Getter
    @Setter
    private ProtocolType protocolType = ProtocolType.LOCAL;

    @Getter
    @Setter
    private String groupCode;

    @Getter
    @Setter
    private String groupName;

    @Getter
    @Setter
    private Class<?> itfClass;

    public ExtensionSpec(Method invokeMethod) {
        this.invokeMethod = invokeMethod;
    }

    public static ExtensionSpec of(Method invokeMethod, String abilityCode,
                                   String extensionCode, String extensionName, String extensionDesc) {
        ExtensionSpec extensionSpec = new ExtensionSpec(invokeMethod);
        extensionSpec.abilityCode = abilityCode;
        extensionSpec.setCode(extensionCode);
        extensionSpec.setName(
                StringUtils.isNotEmpty(extensionCode) ? invokeMethod.getName() : extensionName);
        extensionSpec.setDescription(extensionDesc);
        return extensionSpec;
    }

    @Override
    public boolean equals(Object obj) {
        if (null == obj) {
            return false;
        }

        if (!(obj instanceof ExtensionSpec)) {
            return false;
        }
        ExtensionSpec target = (ExtensionSpec) obj;
        return StringUtils.equals(target.getCode(), this.getCode());
    }

    @Override
    public int hashCode() {
        if (StringUtils.isNotEmpty(getCode())) {
            return getCode().hashCode();
        }
        return super.hashCode();
    }

    @Override
    public String toString() {
        return "ExtensionPointSpec{" +
                "name='" + getName() + '\'' +
                ", code='" + getCode() + '\'' +
                ", ability='" + abilityCode + '\'' +
                ", method=" + invokeMethod +
                '}';
    }
}

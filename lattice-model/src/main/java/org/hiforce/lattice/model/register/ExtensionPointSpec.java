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
public class ExtensionPointSpec extends BaseSpec {

    @Getter
    private final long internalId = SequenceGenerator.next(ExtensionPointSpec.class.getName());

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

    public ExtensionPointSpec(Method invokeMethod) {
        this.invokeMethod = invokeMethod;
    }

    public static ExtensionPointSpec of(Method invokeMethod, String abilityCode,
                                        String extensionCode, String extensionName, String extensionDesc) {
        ExtensionPointSpec extensionPointSpec = new ExtensionPointSpec(invokeMethod);
        extensionPointSpec.abilityCode = abilityCode;
        extensionPointSpec.setCode(extensionCode);
        extensionPointSpec.setName(
                StringUtils.isNotEmpty(extensionCode) ? invokeMethod.getName() : extensionName);
        extensionPointSpec.setDescription(extensionDesc);
        return extensionPointSpec;
    }

    @Override
    public boolean equals(Object obj) {
        if (null == obj) {
            return false;
        }

        if (!(obj instanceof ExtensionPointSpec)) {
            return false;
        }
        ExtensionPointSpec target = (ExtensionPointSpec) obj;
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

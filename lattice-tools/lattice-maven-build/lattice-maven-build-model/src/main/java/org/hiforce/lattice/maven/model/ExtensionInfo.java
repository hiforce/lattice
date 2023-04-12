package org.hiforce.lattice.maven.model;

import com.google.common.collect.Lists;
import lombok.Getter;
import lombok.Setter;
import org.hiforce.lattice.annotation.model.ProtocolType;
import org.hiforce.lattice.annotation.model.ReduceType;

import java.util.List;

/**
 * @author Rocky Yu
 * @since 2022/10/8
 */
public class ExtensionInfo extends BaseInfo {

    private static final long serialVersionUID = -7887485026779426823L;

    @Getter
    @Setter
    private SDKInfo sdkInfo;

    @Getter
    @Setter
    private String abilityCode;

    @Getter
    @Setter
    private String groupCode;

    @Getter
    @Setter
    private String groupName;

    @Getter
    @Setter
    private ReduceType reduceType;

    @Getter
    @Setter
    private ProtocolType protocolType;

    @Getter
    @Setter
    private String methodName;

    @Getter
    @Setter
    private String returnTypeName;

    @Getter
    @Setter
    private int parameterCount;

    @Getter
    private final List<ExtParam> params = Lists.newArrayList();
}

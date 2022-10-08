package org.hiforce.lattice.maven.model;

import com.google.common.collect.Lists;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

/**
 * @author Rocky Yu
 * @since 2022/10/8
 */
public class BusinessInfo implements Serializable {
    private static final long serialVersionUID = 5930904802172618613L;

    @Getter
    @Setter
    private String bizCode;

    @Getter
    @Setter
    private String name;

    @Getter
    @Setter
    private String desc;

    @Getter
    @Setter
    private int priority;

    @Getter
    @Setter
    private String className;

    @Getter
    private final List<ExtensionInfo> customized = Lists.newArrayList();

}

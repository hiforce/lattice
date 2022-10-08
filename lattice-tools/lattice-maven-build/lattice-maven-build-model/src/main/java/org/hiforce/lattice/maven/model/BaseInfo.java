package org.hiforce.lattice.maven.model;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * @author Rocky Yu
 * @since 2022/10/7
 */
public class BaseInfo implements Serializable {

    private static final long serialVersionUID = -9040989280752014322L;

    @Getter
    @Setter
    private String code;

    @Getter
    @Setter
    private String name;

    @Getter
    @Setter
    private String className;
}

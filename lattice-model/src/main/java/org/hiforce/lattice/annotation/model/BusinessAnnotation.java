package org.hiforce.lattice.annotation.model;

import lombok.Getter;
import lombok.Setter;

/**
 * @author Rocky Yu
 * @since 2022/9/21
 */
public class BusinessAnnotation {

    @Getter
    @Setter
    private String code;

    @Getter
    @Setter
    private String name;

    @Getter
    @Setter
    private String desc;

    @Getter
    @Setter
    private int priority;
}

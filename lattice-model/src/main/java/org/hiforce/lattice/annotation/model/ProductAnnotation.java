package org.hiforce.lattice.annotation.model;

import lombok.Getter;
import lombok.Setter;

/**
 * @author Rocky Yu
 * @since 2022/9/16
 */
public class ProductAnnotation {

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

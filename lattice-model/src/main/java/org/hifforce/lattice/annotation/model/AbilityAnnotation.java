package org.hifforce.lattice.annotation.model;

import lombok.Getter;
import lombok.Setter;

/**
 * @author Rocky Yu
 * @since 2022/9/15
 */
public class AbilityAnnotation {

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
    private String parent;
}

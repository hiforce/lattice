package org.hiforce.lattice.annotation.model;

import lombok.Getter;
import lombok.Setter;
import org.hiforce.lattice.model.ability.IBusinessExt;

/**
 * @author Rocky Yu
 * @since 2022/9/28
 */
public class UseCaseAnnotation {

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
    private Class<? extends IBusinessExt> sdk;

    @Getter
    @Setter
    private int priority;
}

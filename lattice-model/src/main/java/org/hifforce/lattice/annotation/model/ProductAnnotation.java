package org.hifforce.lattice.annotation.model;

import lombok.Getter;
import lombok.Setter;
import org.hifforce.lattice.model.ability.IBusinessExt;
import org.hifforce.lattice.model.business.TemplateType;

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
    private TemplateType type;

    @Getter
    @Setter
    private Class<? extends IBusinessExt> businessExt;

    @Getter
    @Setter
    private int priority;
}

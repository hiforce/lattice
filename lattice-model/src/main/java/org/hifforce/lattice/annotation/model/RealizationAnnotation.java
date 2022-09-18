package org.hifforce.lattice.annotation.model;

import lombok.Getter;
import lombok.Setter;
import org.hifforce.lattice.model.ability.IBusinessExt;

/**
 * @author Rocky Yu
 * @since 2022/9/18
 */
public class RealizationAnnotation {

    @Getter
    @Setter
    private String[] codes;

    @Getter
    @Setter
    private String scenario;

    @Getter
    @Setter
    private Class<IBusinessExt> businessExtClass;
}

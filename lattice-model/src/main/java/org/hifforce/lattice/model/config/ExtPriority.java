package org.hifforce.lattice.model.config;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * @author Rocky Yu
 * @since 2022/9/21
 */
@Builder
public class ExtPriority implements Serializable {

    private static final long serialVersionUID = -5187745224595471480L;

    @Getter
    @Setter
    private String code;

    @Getter
    @Setter
    private int priority;
}

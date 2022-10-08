package org.hiforce.lattice.maven.model;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * @author Rocky Yu
 * @since 2022/10/8
 */
public class ExtParam implements Serializable {

    private static final long serialVersionUID = -2779335306296015700L;

    @Getter
    @Setter
    private String name;

    @Getter
    @Setter
    private String typeName;
}

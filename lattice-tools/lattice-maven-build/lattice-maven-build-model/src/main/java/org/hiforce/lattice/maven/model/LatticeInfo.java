package org.hiforce.lattice.maven.model;

import com.google.common.collect.Lists;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

/**
 * @author Rocky Yu
 * @since 2022/10/7
 */
public class LatticeInfo implements Serializable {

    private static final long serialVersionUID = 6289372399096534060L;

    @Getter
    @Setter
    private AbilitySet ability = new AbilitySet();

    @Getter
    @Setter
    private UseCaseSet useCase = new UseCaseSet();

}

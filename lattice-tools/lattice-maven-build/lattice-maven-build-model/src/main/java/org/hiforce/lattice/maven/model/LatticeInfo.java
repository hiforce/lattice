package org.hiforce.lattice.maven.model;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * @author Rocky Yu
 * @since 2022/10/7
 */
public class LatticeInfo implements Serializable {

    private static final long serialVersionUID = 6289372399096534060L;

    @Getter
    @Setter
    private String groupId;

    @Getter
    @Setter
    private String artifactId;

    @Getter
    @Setter
    private String version;

    @Getter
    @Setter
    private AbilitySet ability = new AbilitySet();

    @Getter
    @Setter
    private UseCaseSet useCase = new UseCaseSet();

    @Getter
    @Setter
    private BusinessSet business = new BusinessSet();

    @Getter
    @Setter
    private ProductSet product = new ProductSet();

}

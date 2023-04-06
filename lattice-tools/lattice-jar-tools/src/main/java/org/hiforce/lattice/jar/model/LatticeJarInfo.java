package org.hiforce.lattice.jar.model;

import lombok.Getter;
import lombok.Setter;
import org.hiforce.lattice.maven.model.LatticeInfo;

import java.io.Serializable;

/**
 * @author Rocky Yu
 * @since 2023/4/6
 */
public class LatticeJarInfo implements Serializable {

    private static final long serialVersionUID = 5673204236688466975L;

    @Getter
    @Setter
    private String fileName;

    @Getter
    @Setter
    private LatticeInfo latticeInfo;
}

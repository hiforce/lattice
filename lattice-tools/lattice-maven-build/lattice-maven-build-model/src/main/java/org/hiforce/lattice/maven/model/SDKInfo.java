package org.hiforce.lattice.maven.model;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

public class SDKInfo implements Serializable {

    @Getter
    @Setter
    private String filename;

    @Getter
    @Setter
    private String version;

    @Getter
    @Setter
    private String groupId;

    @Getter
    @Setter
    private String artifactId;
}

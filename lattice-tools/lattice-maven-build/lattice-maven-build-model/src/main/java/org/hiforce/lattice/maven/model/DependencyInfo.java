package org.hiforce.lattice.maven.model;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * @author Rocky Yu
 * @since 2022/10/7
 */
public class DependencyInfo implements Serializable {

    private static final long serialVersionUID = -3315091011104641899L;

    @Getter
    @Setter
    private String groupId;
    @Getter
    @Setter
    private String artifactId;
    @Getter
    @Setter
    private String version;

    public static DependencyInfo of(String groupId, String artifactId, String version) {
        DependencyInfo info = new DependencyInfo();
        info.setGroupId(groupId);
        info.setArtifactId(artifactId);
        info.setVersion(version);
        return info;
    }

    @Override
    public String toString() {
        return "Dependency{" +
                "groupId='" + groupId + '\'' +
                ", artifactId='" + artifactId + '\'' +
                ", version='" + version + '\'' +
                '}';
    }
}

package org.hiforce.lattice.remote.client.model;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Objects;

/**
 * @author Rocky Yu
 * @since 2022/10/14
 */
public class RemoteExtension implements Serializable {

    private static final long serialVersionUID = 1225905222315195923L;

    @Getter
    @Setter
    private String extCode;

    @Getter
    @Setter
    private boolean strongDependency;

    public static RemoteExtension of(String extCode, boolean strongDependency) {
        RemoteExtension extension = new RemoteExtension();
        extension.setExtCode(extCode);
        extension.setStrongDependency(strongDependency);
        return extension;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RemoteExtension that = (RemoteExtension) o;

        return Objects.equals(extCode, that.extCode);
    }

    @Override
    public int hashCode() {
        return extCode != null ? extCode.hashCode() : 0;
    }
}

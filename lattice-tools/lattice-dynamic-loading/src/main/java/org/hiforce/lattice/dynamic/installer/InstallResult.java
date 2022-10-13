package org.hiforce.lattice.dynamic.installer;

import lombok.Getter;

import java.io.Serializable;

/**
 * @author Rocky Yu
 * @since 2022/10/13
 */
public class InstallResult implements Serializable {

    private static final long serialVersionUID = 6816597626096293401L;

    @Getter
    private boolean success;

    @Getter
    private String errCode;

    @Getter
    private String errText;

    public static InstallResult success() {
        InstallResult result = new InstallResult();
        result.success = true;
        return result;
    }
}

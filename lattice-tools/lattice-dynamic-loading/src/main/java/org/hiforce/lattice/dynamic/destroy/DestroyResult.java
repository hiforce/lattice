package org.hiforce.lattice.dynamic.destroy;

import lombok.Getter;

import java.io.Serializable;

/**
 * @author Rocky Yu
 * @since 2022/10/12
 */
public class DestroyResult implements Serializable {

    private static final long serialVersionUID = 6816597626096293401L;

    @Getter
    private boolean success;

    @Getter
    private String errCode;

    @Getter
    private String errText;

    public static DestroyResult success() {
        DestroyResult result = new DestroyResult();
        result.success = true;
        return result;
    }
}

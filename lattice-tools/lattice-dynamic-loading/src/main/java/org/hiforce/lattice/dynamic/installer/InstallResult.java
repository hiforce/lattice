package org.hiforce.lattice.dynamic.installer;

import lombok.Getter;
import lombok.Setter;
import org.hiforce.lattice.dynamic.model.PluginFileInfo;

import java.io.Serializable;

/**
 * @author Rocky Yu
 * @since 2022/10/13
 */
public class InstallResult implements Serializable {

    private static final long serialVersionUID = 6816597626096293401L;

    @Getter
    @Setter
    private PluginFileInfo installed;

    @Getter
    private boolean success;

    @Getter
    private String errCode;

    @Getter
    private String errText;

    public static InstallResult success(PluginFileInfo model) {
        InstallResult result = new InstallResult();
        result.success = true;
        result.installed = model;
        return result;
    }
}

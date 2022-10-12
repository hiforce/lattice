package org.hiforce.lattice.dynamic.model;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.Serializable;
import java.security.MessageDigest;

/**
 * @author Rocky Yu
 * @since 2022/10/12
 */
public class PluginFileInfo implements Serializable {

    private static final long serialVersionUID = 6144274272056556714L;

    private final static String[] strHex = {"0", "1", "2", "3", "4", "5",
            "6", "7", "8", "9", "a", "b", "c", "d", "e", "f"};

    @Getter
    @Setter
    private String md5;

    @Getter
    private final File file;

    public PluginFileInfo(File file) {
        this.file = file;
        buildMD5Value();
    }

    private void buildMD5Value() {
        StringBuilder sb = new StringBuilder();
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            byte[] bytes = digest.digest(FileUtils.readFileToByteArray(file));
            for (int aByte : bytes) {
                int d = aByte;
                if (d < 0) {
                    d += 256;
                }
                int d1 = d / 16;
                int d2 = d % 16;
                sb.append(strHex[d1]).append(strHex[d2]);
            }
            md5 = sb.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}

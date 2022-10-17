package org.hiforce.lattice.dynamic.model;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.security.MessageDigest;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.jar.JarFile;

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
    private String id;

    @Getter
    private final File file;

    @Getter
    private final Set<String> productCodes = Sets.newHashSet();

    @Getter
    private final Set<String> bizCodes = Sets.newHashSet();

    @Getter
    private final JarFile jarFile;

    @Getter
    private final List<SpringBeanInfo> beans = Lists.newArrayList();

    public PluginFileInfo(File file) {
        this.file = file;
        buildMD5Value();
        try {
            jarFile = new JarFile(file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
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
            id = sb.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PluginFileInfo that = (PluginFileInfo) o;

        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}

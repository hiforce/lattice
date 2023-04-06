package org.hiforce.lattice.jar;

import org.hiforce.lattice.jar.model.LatticeJarInfo;
import org.hiforce.lattice.maven.model.LatticeInfo;
import org.hiforce.lattice.utils.JacksonUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

/**
 * @author Rocky Yu
 * @since 2023/4/6
 */
public class LatticeJarUtils {

    public static final String INFO_FILE = "META-INF/lattice/lattice.json";

    public static LatticeJarInfo parseLatticeJar(String fileName, InputStream inputStream) throws Exception {
        JarInputStream jarInput = new JarInputStream(inputStream);
        LatticeJarInfo jarInfo = new LatticeJarInfo();
        jarInfo.setFileName(fileName);

        JarEntry entry = jarInput.getNextJarEntry();
        while (entry != null) {
            if (INFO_FILE.equals(entry.getName())) {
                String json = copyInputStream(jarInput);
                jarInfo.setLatticeInfo(JacksonUtils.deserializeIgnoreException(json, LatticeInfo.class));
            }
            entry = jarInput.getNextJarEntry();
        }
        return jarInfo;
    }

    private static String copyInputStream(InputStream in) throws IOException {

        ByteArrayOutputStream _copy = new ByteArrayOutputStream();
        int read = 0;
        int chunk = 0;
        byte[] data = new byte[256];
        while (-1 != (chunk = in.read(data))) {
            read += data.length;
            _copy.write(data, 0, chunk);
        }
        return _copy.toString();
    }
}

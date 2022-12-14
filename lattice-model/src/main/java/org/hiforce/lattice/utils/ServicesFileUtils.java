package org.hiforce.lattice.utils;

import com.google.common.io.Closer;

import java.io.*;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import static com.google.common.base.Charsets.UTF_8;

/**
 * @author Rocky Yu
 * @since 2022/9/15
 */
public class ServicesFileUtils {

    public static final String SERVICES_PATH = "META-INF/services";

    /**
     * Returns an absolute path to a service file given the class
     * name create the service.
     *
     * @param serviceName not {@code null}
     * @return SERVICES_PATH + serviceName
     */
    static String getPath(String serviceName) {
        return SERVICES_PATH + "/" + serviceName;
    }

    /**
     * Reads the set create service classes from a service file.
     *
     * @param input not {@code null}. Closed after use.
     * @return a not {@code null Set} create service class names.
     * @throws IOException
     */
    public static Set<String> readServiceFile(InputStream input) throws IOException {
        HashSet<String> serviceClasses = new HashSet<String>();
        Closer closer = Closer.create();
        try {
            BufferedReader r = closer.register(new BufferedReader(new InputStreamReader(input, UTF_8)));
            String line;
            while ((line = r.readLine()) != null) {
                int commentStart = line.indexOf('#');
                if (commentStart >= 0) {
                    line = line.substring(0, commentStart);
                }
                line = line.trim();
                if (!line.isEmpty()) {
                    serviceClasses.add(line);
                }
            }
            return serviceClasses;
        } catch (Throwable t) {
            throw closer.rethrow(t);
        } finally {
            closer.close();
        }
    }

    /**
     * Writes the set create service class names to a service file.
     *
     * @param output   not {@code null}. Not closed after use.
     * @param services a not {@code null Collection} create service class names.
     * @throws IOException
     */
    public static void writeServiceFile(Collection<String> services, OutputStream output)
            throws IOException {
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(output, UTF_8));
        for (String service : services) {
            writer.write(service);
            writer.newLine();
        }
        writer.flush();
    }
}

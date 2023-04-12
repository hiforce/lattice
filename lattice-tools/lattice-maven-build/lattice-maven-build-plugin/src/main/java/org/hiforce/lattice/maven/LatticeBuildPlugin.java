package org.hiforce.lattice.maven;

import com.google.common.collect.Lists;
import lombok.Getter;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.hiforce.lattice.runtime.Lattice;
import org.hiforce.lattice.utils.JacksonUtils;
import org.hiforce.lattice.maven.builder.*;
import org.hiforce.lattice.maven.model.LatticeInfo;

import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Rocky Yu
 * @since 2022/10/6
 */
@Mojo(name = "lattice", requiresDependencyResolution = ResolutionScope.COMPILE)
public class LatticeBuildPlugin extends AbstractMojo {

    @Getter
    private final LatticeInfo latticeInfo = new LatticeInfo();

    private static final String LATTICE_DIR = "META-INF" + File.separator + "lattice";

    private static final String LATTICE_FILE = LATTICE_DIR + File.separator + "lattice.json";

    @Getter
    @Parameter(defaultValue = "${project}")
    public MavenProject mavenProject;

    @Parameter(defaultValue = "${project.build.outputDirectory}")
    protected String outputDirectory;

    @Getter
    private ClassLoader totalClassLoader;

    @Getter
    private ClassLoader projectClassLoader;

    @Getter
    private ClassLoader importClassLoader;


    @Override
    public synchronized void execute() throws MojoExecutionException, MojoFailureException {
        Lattice.getInstance().clear();
        latticeInfo.setGroupId(mavenProject.getGroupId());
        latticeInfo.setArtifactId(mavenProject.getArtifactId());
        latticeInfo.setVersion(mavenProject.getVersion());

        totalClassLoader = loadClassLoader(false, false);
        projectClassLoader = loadClassLoader(true, false);
        importClassLoader = loadClassLoader(false, true);

        List<LatticeInfoBuilder> builders = Lists.newArrayList(
                new RealizationInfoBuilder(this),
                new AbilityInfoBuilder(this),
                new ProductInfoBuilder(this),
                new UseCaseInfoBuilder(this),
                new BusinessInfoBuilder(this)
        );
        builders.forEach(LatticeInfoBuilder::build);

        writeLatticeInfo();
    }

    @SuppressWarnings("all")
    private void writeLatticeInfo() throws MojoExecutionException {

        String fullFileDir = outputDirectory + File.separator + LATTICE_DIR;
        String fullFilePath = outputDirectory + File.separator + LATTICE_FILE;

        OutputStreamWriter oStreamWriter = null;
        try {
            File resourceFile = new File(fullFilePath);
            if (resourceFile.exists()) {
                resourceFile.delete();
            } else {
                File tmp = new File(fullFileDir);
                tmp.mkdirs();
                resourceFile.createNewFile();
            }
            oStreamWriter = new OutputStreamWriter(Files.newOutputStream(resourceFile.toPath()), StandardCharsets.UTF_8);
            oStreamWriter.append(JacksonUtils.serializeWithoutException(getLatticeInfo()));
            oStreamWriter.flush();
        } catch (IOException e) {
            throw new MojoExecutionException(">> Lattice maven plugin write file failed: " + fullFilePath, e);
        } finally {
            if (oStreamWriter != null) {
                try {
                    oStreamWriter.close();
                } catch (IOException ignored) {

                }
            }
        }
    }


    @SuppressWarnings("all")
    public ClassLoader loadClassLoader(boolean onlyProject, boolean onlyImport) {
        ClassLoader originClassLoader = Thread.currentThread().getContextClassLoader();
        try {
            List<String> classPath = mavenProject.getCompileClasspathElements();
            List<URL> urls = new ArrayList<>();
            for (String cls : classPath) {
                try {
                    if (onlyProject && cls.endsWith(".jar")) {
                        continue;
                    }
                    if (onlyImport && (cls.endsWith("classes") || cls.endsWith("classes" + File.separator))) {
                        continue;
                    }

                    if (!cls.endsWith(".jar") && !cls.endsWith(File.separator)) {
                        cls = cls + File.separator;
                    }
                    urls.add(new URL("file:" + cls));
                } catch (MalformedURLException e) {
                    getLog().info(e.getMessage());
                }
            }
            URL[] urlArrays = urls.toArray(new URL[urls.size()]);
            return new URLClassLoader(urlArrays, originClassLoader);
        } catch (Exception ex) {
            getLog().error(ex.getMessage());
            return originClassLoader;
        }
    }
}

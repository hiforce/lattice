package org.hiforce.lattice.maven;

import lombok.Getter;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.hiforce.lattice.maven.builder.AbilityInfoBuilder;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Rocky Yu
 * @since 2022/10/6
 */
@Mojo(name = "lattice", requiresDependencyResolution = ResolutionScope.COMPILE)
public class LatticeBuildPlugin extends AbstractMojo {

    @Getter
    @Parameter(defaultValue = "${project}")
    public MavenProject mavenProject;

    @Parameter(defaultValue = "${project.build.outputDirectory}")
    protected String outputDirectory;

    private ClassLoader classLoader;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        getLog().info(">> LatticeBuildPlugin start....");
        getLog().info(">> Project: " + mavenProject.getName());

        AbilityInfoBuilder abilityInfoBuilder = new AbilityInfoBuilder(this, getClassLoader());
        abilityInfoBuilder.build();
    }


    public ClassLoader getClassLoader() {
        if (null != classLoader) {
            return classLoader;
        }
        ClassLoader originClassLoader = Thread.currentThread().getContextClassLoader();
        try {
            List<String> classPath = mavenProject.getCompileClasspathElements();
            List<URL> urls = new ArrayList<>();
            for (String cls : classPath) {
                try {
                    getLog().warn(">>>> cls: " + cls);
                    if (!cls.endsWith(".jar") && !cls.endsWith(File.separator)) {
                        cls = cls + File.separator;
                    }
                    urls.add(new URL("file:" + cls));
                } catch (MalformedURLException e) {
                    getLog().info(e.getMessage());
                }
            }
            URL[] urlArrays = urls.toArray(new URL[urls.size()]);
            classLoader = new URLClassLoader(urlArrays, originClassLoader);
            Thread.currentThread().setContextClassLoader(classLoader);
            getLog().warn(">>>> Lattice ClassLoader: " + classLoader);
            return classLoader;
        }catch (Exception ex){
            getLog().error(ex.getMessage());
            return originClassLoader;
        }
    }
}

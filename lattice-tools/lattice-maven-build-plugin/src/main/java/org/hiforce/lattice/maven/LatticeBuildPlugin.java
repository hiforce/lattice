package org.hiforce.lattice.maven;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;

/**
 * @author Rocky Yu
 * @since 2022/10/6
 */
@Mojo(name = "lattice", requiresDependencyResolution = ResolutionScope.COMPILE)
public class LatticeBuildPlugin extends AbstractMojo {

    @Parameter(defaultValue = "${project}")
    public MavenProject mavenProject;

    @Override
    public void execute() {
        getLog().info(">> LatticeBuildPlugin start....");
        getLog().info(">> Project: " + mavenProject.getName());
    }
}

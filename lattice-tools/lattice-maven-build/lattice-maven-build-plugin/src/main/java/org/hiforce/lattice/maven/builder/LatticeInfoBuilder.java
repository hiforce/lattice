package org.hiforce.lattice.maven.builder;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.apache.maven.model.Dependency;
import org.apache.maven.plugin.logging.Log;
import org.hiforce.lattice.annotation.Schema;
import org.hiforce.lattice.annotation.model.ExtensionAnnotation;
import org.hiforce.lattice.jar.LatticeJarUtils;
import org.hiforce.lattice.jar.model.LatticeJarInfo;
import org.hiforce.lattice.model.ability.IBusinessExt;
import org.hiforce.lattice.model.business.IBusiness;
import org.hiforce.lattice.model.register.RealizationSpec;
import org.hiforce.lattice.maven.LatticeBuildPlugin;
import org.hiforce.lattice.maven.model.DependencyInfo;
import org.hiforce.lattice.maven.model.ExtensionInfo;
import org.hiforce.lattice.maven.model.RealizationInfo;
import org.hiforce.lattice.runtime.Lattice;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URI;
import java.nio.file.Files;
import java.security.CodeSource;
import java.security.ProtectionDomain;
import java.util.List;
import java.util.Set;

import static org.hiforce.lattice.utils.LatticeAnnotationUtils.getExtensionAnnotation;
import static org.hiforce.lattice.maven.builder.BusinessInfoBuilder.buildExtensionInfo;

/**
 * @author Rocky Yu
 * @since 2022/10/6
 */
public abstract class LatticeInfoBuilder {

    @Getter
    private final LatticeBuildPlugin plugin;

    @Getter
    private final ClassLoader totalClassLoader;

    @Getter
    private final ClassLoader importClassLoader;

    @Getter
    private final ClassLoader projectClassLoader;

    @SuppressWarnings("all")
    public abstract String getSpiClassName();

    public List<String> getProvidedInfoClassNames() {
        return Lattice.getServiceProviderValues(
                getSpiClassName(), getProjectClassLoader());
    }

    public List<String> getImportInfoClassNames() {
        return Lattice.getServiceProviderValues(
                getSpiClassName(), getImportClassLoader());
    }

    public LatticeInfoBuilder(LatticeBuildPlugin plugin) {

        this.plugin = plugin;
        this.projectClassLoader = plugin.getProjectClassLoader();
        this.totalClassLoader = plugin.getTotalClassLoader();
        this.importClassLoader = plugin.getImportClassLoader();
    }

    public abstract void build();


    @SuppressWarnings("all")
    protected Set<Class> loadTargetClassList(List<String> classNames) {
        Set<Class> classList = Sets.newHashSet();
        for (String name : classNames) {
            try {
                Class<?> abilityClass = getTotalClassLoader().loadClass(name);
                classList.add(abilityClass);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return classList;
    }

    public Log getLog() {
        return getPlugin().getLog();
    }


    public static RealizationInfo buildRealizationInfo(RealizationSpec spec) {
        if (null == spec) {
            return null;
        }
        RealizationInfo info = new RealizationInfo();
        info.setScenario(spec.getScenario());
        info.setBusinessExtClass(spec.getBusinessExtClass().getName());
        info.getExtensionCodes().addAll(spec.getExtensionCodes());
        return info;
    }


    public static LatticeJarInfo getSdkLatticeJarInfo(boolean findSuperClass, Class<?> facadeClass) {
        Schema schema = facadeClass.getDeclaredAnnotation(Schema.class);
        if( null != schema){
            return getPath(facadeClass);
        }
        if( !findSuperClass){
            return getPath(facadeClass);
        }

        Class<?> superClass = facadeClass.getSuperclass();
        while (null != superClass) {
            schema = superClass.getDeclaredAnnotation(Schema.class);
            if (null != schema && schema.root()) {
                return getPath(superClass);
            }
            if( Object.class.equals(superClass.getSuperclass())){
                return getPath(superClass);
            }
            superClass = superClass.getSuperclass();
        }
        return null;
    }

    public static LatticeJarInfo getPath(Class<?> targetClass) {
        String path = targetClass.getProtectionDomain().getCodeSource().getLocation().getPath();

        try {
            File file = new File(path);
            return LatticeJarUtils.parseLatticeJar(file.getName(), Files.newInputStream(file.toPath()));
        } catch (Exception e) {

        }
        return null;
    }

    @SuppressWarnings("unchecked")
    public DependencyInfo getDependencyInfo(Class<?> targetClass) {
        if (null == targetClass) {
            return null;
        }

        try {

            ProtectionDomain protectionDomain = targetClass.getProtectionDomain();
            CodeSource codeSource = protectionDomain.getCodeSource();
            URI location = (codeSource != null) ? codeSource.getLocation().toURI() : null;
            String path = (location != null) ? location.getSchemeSpecificPart() : null;
            if (null == path) {
                return null;
            }

            File file = new File(path);

            List<Dependency> dependencies = getPlugin().getMavenProject().getRuntimeDependencies();
            return dependencies.stream()
                    .filter(p -> StringUtils.equals(file.getName(),
                            String.format("%s-%s.jar", p.getArtifactId(), p.getVersion())))
                    .findFirst()
                    .map(p -> DependencyInfo.of(p.getGroupId(), p.getArtifactId(), p.getVersion()))
                    .orElse(null);
        } catch (Exception ex) {
            getLog().error(ex.getMessage(), ex);
            return null;
        }
    }

    public List<ExtensionInfo> buildCustomizedExtensionInfos(IBusinessExt businessExt) {
        List<ExtensionInfo> extensionInfos = Lists.newArrayList();
        for (Method method : businessExt.getClass().getMethods()) {
            ExtensionAnnotation annotation = getExtensionAnnotation(method);
            if (null == annotation) {
                continue;
            }
            Class<?> businessExtClass = method.getDeclaringClass();
            Schema schema = businessExtClass.getDeclaredAnnotation(Schema.class);
            if( null != schema && schema.root()){
                continue;
            }
            ExtensionInfo extensionInfo = buildExtensionInfo(businessExt.getClass(), annotation, method);
            extensionInfos.add(extensionInfo);
        }
        for (IBusinessExt subBusinessExt : businessExt.getAllSubBusinessExt()) {
            extensionInfos.addAll(buildCustomizedExtensionInfos(subBusinessExt));
        }
        return extensionInfos;
    }
}

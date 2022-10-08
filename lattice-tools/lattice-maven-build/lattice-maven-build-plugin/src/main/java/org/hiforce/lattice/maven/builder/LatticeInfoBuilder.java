package org.hiforce.lattice.maven.builder;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import lombok.Getter;
import org.apache.maven.plugin.logging.Log;
import org.hifforce.lattice.annotation.model.ExtensionAnnotation;
import org.hifforce.lattice.model.ability.IBusinessExt;
import org.hifforce.lattice.model.register.RealizationSpec;
import org.hiforce.lattice.maven.LatticeBuildPlugin;
import org.hiforce.lattice.maven.model.ExtensionInfo;
import org.hiforce.lattice.maven.model.RealizationInfo;
import org.hiforce.lattice.runtime.Lattice;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Set;

import static org.hifforce.lattice.utils.LatticeAnnotationUtils.getExtensionAnnotation;
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

    public List<ExtensionInfo> buildCustomizedExtensionInfos(IBusinessExt businessExt) {
        List<ExtensionInfo> extensionInfos = Lists.newArrayList();
        for (Method method : businessExt.getClass().getDeclaredMethods()) {
            ExtensionAnnotation annotation = getExtensionAnnotation(method);
            if (null != annotation) {
                ExtensionInfo extensionInfo = buildExtensionInfo(businessExt.getClass(), annotation, method);
                extensionInfos.add(extensionInfo);
            }
        }
        for (IBusinessExt subBusinessExt : businessExt.getAllSubBusinessExt()) {
            extensionInfos.addAll(buildCustomizedExtensionInfos(subBusinessExt));
        }
        return extensionInfos;
    }
}

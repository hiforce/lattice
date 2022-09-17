package org.hiforce.lattice.runtime;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.hifforce.lattice.cache.LatticeCacheFactory;
import org.hifforce.lattice.model.ability.IAbility;
import org.hifforce.lattice.model.ability.provider.IAbilityProvider;
import org.hifforce.lattice.model.register.AbilitySpec;
import org.hiforce.lattice.runtime.ability.register.AbilityBuildRequest;
import org.hiforce.lattice.runtime.ability.register.AbilityRegister;
import org.hiforce.lattice.runtime.cache.LatticeRuntimeCache;
import org.hiforce.lattice.runtime.spi.LatticeSpiFactory;
import org.hiforce.lattice.runtime.utils.ClassLoaderUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Rocky Yu
 * @since 2022/9/16
 */
@Slf4j
@SuppressWarnings("unused")
public class Lattice {

    private static Lattice instance;


    @Getter
    private final LatticeRuntimeCache latticeRuntimeCache = (LatticeRuntimeCache) LatticeCacheFactory.getInstance()
            .getRuntimeCache(new LatticeRuntimeCache());

    @Getter
    @SuppressWarnings("all")
    private final IAbilityProvider abilityProvider = LatticeSpiFactory.getInstance()
            .getAbilityProviderCreator().createAbilityProvider();

    private Lattice() {

    }

    public static Lattice getInstance() {
        if (null == instance) {
            instance = new Lattice();
        }
        return instance;
    }

    public final void start() {
        registerAbility();//Register the Ability Instances during runtime.
    }


    public Collection<AbilitySpec> getAllRegisteredAbilities() {
        return Lattice.getInstance().getLatticeRuntimeCache().getAllCachedAbilities();
    }


    public static Set<Class> getServiceProviderClasses(String spiClassName) {
        Set<Class> classList = Sets.newHashSet();
        try {
            List<ClassLoader> classLoaders = Lists.newArrayList(Thread.currentThread().getContextClassLoader());
            for (ClassLoader classLoader : classLoaders) {
                Enumeration<URL> enumeration = classLoader.getResources("META-INF/services/" + spiClassName);
                while (enumeration.hasMoreElements()) {
                    URL url = enumeration.nextElement();
                    List<String> classNames = loadSpiFileContent(url);
                    for (String className : classNames) {
                        try {
                            classList.add(Class.forName(className));
                        } catch (Exception e) {
                            log.warn(e.getMessage(), e);
                        }
                    }
                }
            }
        } catch (IOException e) {
            log.warn(e.getMessage(), e);
        }
        return classList;
    }

    private static List<String> loadSpiFileContent(URL url) {
        List<String> contentList = new ArrayList<>();
        try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(url.openStream(), StandardCharsets.UTF_8))) {
            String eachLine;
            while (StringUtils.isNotBlank(eachLine = bufferedReader.readLine())) {
                eachLine = eachLine.trim();
                contentList.add(eachLine);
            }
        } catch (Exception e) {
            log.warn(e.getMessage(), e);
        }
        return contentList;
    }


    private void registerAbility() {
        Set<Class> abilityClasses = getServiceProviderClasses(IAbility.class.getName());
        AbilityRegister.getInstance().register(new AbilityBuildRequest(null, mergeAbilityInstancePackage(abilityClasses)));
    }

    private Set<Class> mergeAbilityInstancePackage(Set<Class> abilityClasses) {
        Set<Class> classesSet = Sets.newHashSet(abilityClasses);
        Set<String> packageSet = abilityClasses.stream().map(p -> p.getPackage().getName()).collect(Collectors.toSet());
        for (String pkg : packageSet) {
            classesSet.addAll(ClassLoaderUtil.scanLatticeClasses(pkg));
        }

        return classesSet;
    }
}

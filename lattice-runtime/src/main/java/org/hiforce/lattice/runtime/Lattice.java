package org.hiforce.lattice.runtime;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.hifforce.lattice.model.ability.IAbility;
import org.hiforce.lattice.runtime.ability.AbilityRegister;
import org.hiforce.lattice.runtime.utils.ClassLoaderUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Rocky Yu
 * @since 2022/9/16
 */
@Slf4j
@SuppressWarnings("unused")
public class Lattice {

    public final void start() {

        registerAbility();//Register the Ability Instances during runtime.
    }


    public static Set<Class<?>> getServiceProviderClasses(String spiClassName) {
        Set<Class<?>> classList = Sets.newHashSet();
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
        Set<Class<?>> abilityClasses = getServiceProviderClasses(IAbility.class.getName());
        AbilityRegister.getInstance().register(mergeAbilityInstancePackage(abilityClasses));
    }

    private Set<Class<?>> mergeAbilityInstancePackage(Set<Class<?>> abilityClasses) {
        Set<Class<?>> classesSet = Sets.newHashSet(abilityClasses);
        Set<String> packageSet = abilityClasses.stream().map(p -> p.getPackage().getName()).collect(Collectors.toSet());
        for (String pkg : packageSet) {
            classesSet.addAll(ClassLoaderUtil.scanLatticeClasses(pkg));
        }

        return classesSet;
    }
}

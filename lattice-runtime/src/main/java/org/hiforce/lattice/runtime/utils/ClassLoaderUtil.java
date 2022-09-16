package org.hiforce.lattice.runtime.utils;


import org.hifforce.lattice.model.ability.IAbility;
import org.hifforce.lattice.model.ability.IBusinessExt;
import org.hiforce.lattice.runtime.spi.LatticeSpiFactory;

import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

/**
 * @author Rocky Yu
 * @since 2022/9/16
 */
public abstract class ClassLoaderUtil {

    public static Set<Class<?>> scanLatticeClasses(String... classPackages) {
        ClassPathScanHandler handler = new ClassPathScanHandler();
        TreeSet<Class<?>> classSet = new TreeSet<>(new ClassNameComparator());

        for (String classPackage : classPackages) {
            classSet.addAll(handler.getPackageAllClasses(classPackage, true));
        }

        return classSet.stream().filter(ClassLoaderUtil::isLatticeModelClass).collect(Collectors.toSet());
    }

    private static boolean isLatticeModelClass(Class<?> targetClass) {
        if (null == targetClass)
            return false;

        if (LatticeClassUtils.isSubClassOf(targetClass, IBusinessExt.class)) {
            return true;
        }
        if (LatticeClassUtils.isSubClassOf(targetClass, IAbility.class)) {
            return true;
        }
        if (isLatticeExtendAnnotationClass(targetClass)) {
            return true;
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    private static boolean isLatticeExtendAnnotationClass(Class<?> targetClass) {
        if (LatticeSpiFactory.getInstance().getAbilityAnnotationParsers()
                .stream().map(p -> null != targetClass.getAnnotation(p.getAnnotationClass()))
                .findFirst().isPresent()) {
            return true;
        }
        if (LatticeSpiFactory.getInstance().getExtensionAnnotationParsers()
                .stream().map(p -> null != targetClass.getAnnotation(p.getAnnotationClass()))
                .findFirst().isPresent()) {
            return true;
        }
        return false;
    }
}

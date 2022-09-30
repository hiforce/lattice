package org.hifforce.lattice.utils;

import org.apache.commons.lang3.ClassUtils;

/**
 * @author Rocky Yu
 * @since 2022/9/16
 */
public class LatticeClassUtils extends ClassUtils {

    public static boolean isSubClassOf(Class<?> subClass, Class targetSuperClass) {
        if (ClassUtils.isAssignable(subClass, targetSuperClass)) {
            return true;
        }

        Class<?> superClass = subClass.getSuperclass();
        while (null != superClass) {
            if (superClass.equals(Object.class)) {
                return false;
            }
            if (ClassUtils.isAssignable(superClass, targetSuperClass)) {
                return true;
            }
            superClass = superClass.getSuperclass();
        }

        return false;
    }

}

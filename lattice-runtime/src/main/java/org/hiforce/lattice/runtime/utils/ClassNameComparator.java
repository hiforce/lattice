package org.hiforce.lattice.runtime.utils;

import java.util.Comparator;

/**
 * @author Rocky Yu
 * @since 2022/9/16
 */
public class ClassNameComparator implements Comparator<Class<?>> {
    @Override
    public int compare(Class<?> o1, Class<?> o2) {
        if (o1 == null)
            return -1;
        if (o2 == null)
            return 1;
        return o1.getName().compareToIgnoreCase(o2.getName());
    }
}

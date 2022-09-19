package org.hiforce.lattice.test;

import lombok.Getter;

import java.lang.reflect.Constructor;

/**
 * @author Rocky Yu
 * @since 2022/9/19
 */
public class Person {

    @Getter
    private String name;

    @Getter
    private int age;

    public Person(String name, int age) {
        this.name = name;
        this.age = age;
    }

    public static void main(String args[]) throws Exception {
        String name = "Jack";
        int age = 40;
        Class<?>[] params = new Class<?>[2];
        params[0] = String.class;
        params[1] = int.class;

        Class clz = Class.forName("org.hiforce.lattice.test.Person");
        Constructor<?>[] cons = clz.getConstructors();
        for (Constructor<?> constructor : cons) {
            if (!checkConstructMatched(constructor, null, age)) {
                continue;
            }
            Object obj = constructor.newInstance(null, age);
            System.out.println(obj);
        }
    }

    private static boolean checkConstructMatched(Constructor<?> constructor, Object... values) {

        Class<?>[] parameterTypes = constructor.getParameterTypes();
        if (parameterTypes.length != values.length) {
            return false;
        }
        for (int i = 0; i < values.length; i++) {
            if (null != values[i]) {
                if (!formatParamType(parameterTypes[i]).equals(values[i].getClass())) {
                    return false;
                }
            }
        }
        return true;
    }

    private static Class<?> formatParamType(Class<?> paramClass) {
        if (!paramClass.isPrimitive()) {
            return paramClass;
        }
        if (int.class.equals(paramClass)) {
            return Integer.class;
        }
        return paramClass;
    }

}

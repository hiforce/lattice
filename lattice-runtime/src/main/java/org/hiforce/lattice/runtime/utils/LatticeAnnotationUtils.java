package org.hiforce.lattice.runtime.utils;

import com.google.common.collect.Lists;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.hifforce.lattice.annotation.model.AbilityAnnotation;
import org.hifforce.lattice.annotation.model.ScanSkipAnnotation;
import org.hifforce.lattice.annotation.parser.AbilityAnnotationParser;
import org.hifforce.lattice.annotation.parser.ScanSkipAnnotationParser;
import org.hiforce.lattice.runtime.spi.LatticeSpiFactory;
import org.springframework.core.annotation.AnnotationUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * @author Rocky Yu
 * @since 2022/9/16
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public class LatticeAnnotationUtils extends AnnotationUtils {

    private static final Map<Class<?>, Boolean> annotatedInterfaceCache = new WeakHashMap<>();

    public static AbilityAnnotation getAbilityAnnotation(Class<?> abilityClass) {

        for (AbilityAnnotationParser parser : LatticeSpiFactory.getInstance().getAbilityAnnotationParsers()) {
            Annotation annotation = AnnotationUtils.findAnnotation(abilityClass, parser.getAnnotationClass());
            if (null == annotation) {
                continue;
            }
            return parser.buildAnnotationInfo(annotation, abilityClass);
        }
        return null;
    }

    public static ScanSkipAnnotation getScanSkipAnnotation(Method method) {
        for (ScanSkipAnnotationParser parser : LatticeSpiFactory.getInstance().getScanSkipAnnotationParsers()) {
            Annotation annotation = AnnotationUtils.findAnnotation(method, parser.getAnnotationClass());
            if (null == annotation) {
                continue;
            }
            return parser.buildAnnotationInfo(annotation);
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    public static ScanSkipAnnotation getScanSkipAnnotation(Class<?> targetClass) {
        for (ScanSkipAnnotationParser parser : LatticeSpiFactory.getInstance().getScanSkipAnnotationParsers()) {
            Annotation annotation = AnnotationUtils.getAnnotation(targetClass, parser.getAnnotationClass());
            if (null == annotation) {
                continue;
            }
            return parser.buildAnnotationInfo(annotation);
        }
        return null;
    }

    public static <A extends Annotation> A findAnnotation(Method method, Class<A> annotationType) {
        A annotation = method.getAnnotation(annotationType);
        if (null != annotation)
            return annotation;
        annotation = AnnotationUtils.findAnnotation(method, annotationType);
        if (null != annotation)
            return annotation;

        Class<?> clazz = method.getDeclaringClass();
        annotation = searchOnInterfaces(method, annotationType, clazz.getInterfaces());

        while (annotation == null) {
            clazz = clazz.getSuperclass();
            if (clazz == null || clazz.equals(Object.class)) {
                break;
            }
            try {
                Method equivalentMethod = clazz.getDeclaredMethod(method.getName(), method.getParameterTypes());
                annotation = getAnnotation(equivalentMethod, annotationType);
            } catch (NoSuchMethodException ex) {
                // No equivalent method found
            }
            if (annotation == null) {
                annotation = searchOnInterfaces(method, annotationType, clazz.getInterfaces());
            }
        }
        return annotation;
    }

    private static boolean isInterfaceWithAnnotatedMethods(Class<?> iface) {
        synchronized (annotatedInterfaceCache) {
            Boolean flag = annotatedInterfaceCache.get(iface);
            if (flag != null) {
                return flag;
            }
            boolean found = false;
            for (Method ifcMethod : iface.getMethods()) {
                if (ifcMethod.getAnnotations().length > 0) {
                    found = true;
                    break;
                }
            }
            annotatedInterfaceCache.put(iface, found);
            return found;
        }
    }

    private static boolean isParameterIsEqual(Class<?>[] targetParams, Class<?>[] methodParams) {
        if (null == targetParams && null == methodParams)
            return true;
        if (null == targetParams || methodParams == null)
            return false;
        if (targetParams.length != methodParams.length)
            return false;

        for (int i = 0; i < targetParams.length; i++) {
            if (!LatticeClassUtils.isSubClassOf(targetParams[0], methodParams[0]))
                return false;
        }
        return true;

    }

    private static List<Method> findSameNameMethodWithSameParamNum(Method targetMethod, Method[] methods) {
        List<Method> output = Lists.newArrayList();
        for (Method method : methods) {
            if (!StringUtils.equals(targetMethod.getName(), method.getName()))
                continue;
            if (targetMethod.getParameterTypes().length != method.getParameterTypes().length)
                continue;
            if (isParameterIsEqual(targetMethod.getParameterTypes(), method.getParameterTypes())) {
                output.add(method);
            }
        }
        return output;
    }

    private static <A extends Annotation> A lowLevelSearchAnnotation(Method method, Class<A> annotationType, Class<?> iface) {
        A annotation = null;
        try {
            Method equivalentMethod = iface.getMethod(method.getName(), method.getParameterTypes());
            annotation = getAnnotation(equivalentMethod, annotationType);
        } catch (NoSuchMethodException ex) {
            // if not find method, may be param is generic.
            Method[] methods = iface.getMethods();
            for (Method oneMethod : methods) {
                if (oneMethod.getName().equals(method.getName())) {
                    annotation = getAnnotation(oneMethod, annotationType);
                    if (annotation != null) {
                        break;
                    }
                }
            }
        }
        return annotation;
    }

    private static <A extends Annotation> A searchOnInterfaces(Method method, Class<A> annotationType, Class<?>[] classes) {
        A annotation = null;
        for (Class<?> clz : classes) {
            if (isInterfaceWithAnnotatedMethods(clz)) {
                //先找到可能的方法
                List<Method> maybeMethods = findSameNameMethodWithSameParamNum(method, clz.getMethods());
                //如果只有一个，就是这一个（因为父类可能是泛型，没法做到精确匹配）
                if (CollectionUtils.isNotEmpty(maybeMethods)) {
                    if (maybeMethods.size() == 1) {
                        annotation = getAnnotation(maybeMethods.get(0), annotationType);
                    } else {
                        //有多个，只能用比较低效的方式进行最精确参数匹配了
                        annotation = lowLevelSearchAnnotation(method, annotationType, clz);
                    }
                }
                if (annotation != null) {
                    break;
                }

                Class<?>[] superIfcs = clz.getInterfaces();
                annotation = searchOnInterfaces(method, annotationType, superIfcs);
            }
        }
        return annotation;
    }
}

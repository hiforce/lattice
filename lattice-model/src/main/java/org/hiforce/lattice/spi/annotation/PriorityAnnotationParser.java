package org.hiforce.lattice.spi.annotation;

import org.hiforce.lattice.annotation.model.PriorityAnnotation;
import org.hiforce.lattice.spi.LatticeAnnotationSpiFactory;
import org.hiforce.lattice.utils.LatticeAnnotationUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

/**
 * @author Rocky Yu
 * @since 2023/1/28
 */
@SuppressWarnings("all")
public abstract class PriorityAnnotationParser<T extends Annotation> extends LatticeAnnotationParser<T> {

    public abstract int getValue(T annotation);

    public PriorityAnnotation buildAnnotationInfo(T annotation) {
        if (null == annotation) {
            return null;
        }
        PriorityAnnotation info = new PriorityAnnotation();
        info.setValue(getValue(annotation));
        return info;
    }

    public static PriorityAnnotation getPriorityAnnotationInfo(Class<?> targetClass) {
        for (PriorityAnnotationParser parser : LatticeAnnotationSpiFactory.getInstance().getPriorityAnnotationParsers()) {
            Annotation annotation = targetClass.getDeclaredAnnotation(parser.getAnnotationClass());
            if (null == annotation) {
                continue;
            }
            return parser.buildAnnotationInfo(annotation);
        }
        return null;
    }

    public static PriorityAnnotation getPriorityAnnotationInfo(Method method) {
        for (PriorityAnnotationParser parser : LatticeAnnotationSpiFactory.getInstance().getPriorityAnnotationParsers()) {
            Annotation annotation = LatticeAnnotationUtils.findAnnotation(method, parser.getAnnotationClass());
            if (null == annotation) {
                continue;
            }
            return parser.buildAnnotationInfo(annotation);
        }
        return null;
    }
}
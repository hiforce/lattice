package org.hiforce.lattice.utils;

import org.hifforce.lattice.annotation.model.ExtensionAnnotation;
import org.hifforce.lattice.annotation.parser.ExtensionAnnotationParser;
import org.hiforce.lattice.spi.LatticeSpiFactory;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

/**
 * @author Rocky Yu
 * @since 2022/9/16
 */
public class ExtensionUtils {

    @SuppressWarnings("all")
    public static ExtensionAnnotation getExtensionAnnotation(Method method) {
        for (ExtensionAnnotationParser parser : LatticeSpiFactory.getInstance().getExtensionAnnotationParsers()) {
            Annotation annotation = LatticeAnnotationUtils.findAnnotation(method, parser.getAnnotationClass());
            if (null == annotation) {
                continue;
            }
            return parser.buildAnnotationInfo(annotation);
        }
        return null;
    }
}

package org.hiforce.lattice.spi.annotation;

import org.hiforce.lattice.annotation.model.ScanSkipAnnotation;

import java.lang.annotation.Annotation;

/**
 * @author Rocky Yu
 * @since 2022/9/16
 */
public abstract class ScanSkipAnnotationParser<T extends Annotation> extends LatticeAnnotationParser<T> {

    public ScanSkipAnnotation buildAnnotationInfo(T annotation) {
        if (null == annotation) {
            return null;
        }
        return new ScanSkipAnnotation();
    }
}

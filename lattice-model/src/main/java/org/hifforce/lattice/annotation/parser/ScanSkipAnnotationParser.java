package org.hifforce.lattice.annotation.parser;

import org.hifforce.lattice.annotation.model.ScanSkipAnnotation;
import org.hifforce.lattice.annotation.parser.LatticeAnnotationParser;

import java.lang.annotation.Annotation;

/**
 * @author Rocky Yu
 * @since 2022/9/16
 */
public abstract class ScanSkipAnnotationParser<T extends Annotation> extends LatticeAnnotationParser<T, ScanSkipAnnotation> {

    @Override
    public ScanSkipAnnotation buildAnnotationInfo(T annotation) {
        if (null == annotation) {
            return null;
        }
        return new ScanSkipAnnotation();
    }
}

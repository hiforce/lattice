package org.hifforce.lattice.annotation.parser;

import java.lang.annotation.Annotation;

/**
 * @author Rocky Yu
 * @since 2022/9/15
 */
public abstract class LatticeAnnotationParser<T extends Annotation, Model> {

	public abstract Class<T> getAnnotationClass();

	public abstract Model buildAnnotationInfo(T annotation);
}

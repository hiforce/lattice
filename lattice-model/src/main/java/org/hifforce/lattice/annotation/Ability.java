package org.hifforce.lattice.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 *
 * @author Rocky Yu
 * @since 2022/9/15
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface Ability {

	/**
	 * the namespace current ability belong to.
	 */
	String parent() default "";

	/**
	 * the ability's name.
	 */
	String name();

	/**
	 * the ability's unique code.
	 */
	String code() default "";

	/**
	 * the ability's description.
	 */
	String desc() default "";
}

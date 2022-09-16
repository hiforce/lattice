package org.hiforce.lattice.runtime.ability;


import org.hifforce.lattice.model.ability.IBusinessExt;

import java.util.function.Function;

/**
 * @param <T>
 * @param <R>
 * @author zhenxin.yzx ( Rocky )
 * @since 15/9/23
 */
public interface ExtensionCallback<T extends IBusinessExt, R> extends Function<T, R> {

    R apply(T extension);
}

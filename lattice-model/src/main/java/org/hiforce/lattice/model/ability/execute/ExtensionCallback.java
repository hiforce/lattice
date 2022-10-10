package org.hiforce.lattice.model.ability.execute;

import org.hiforce.lattice.model.ability.IBusinessExt;

import java.util.function.Function;

public interface ExtensionCallback<T extends IBusinessExt, R> extends Function<T, R> {

    R apply(T extension);
}

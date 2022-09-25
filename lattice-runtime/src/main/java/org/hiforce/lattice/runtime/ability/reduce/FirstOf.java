package org.hiforce.lattice.runtime.ability.reduce;

import org.apache.commons.collections4.CollectionUtils;
import org.hifforce.lattice.annotation.model.ReduceType;
import org.hifforce.lattice.model.ability.execute.Reducer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Objects;
import java.util.function.Predicate;

/**
 * @author Rocky Yu
 * @since 2022/9/16
 */
public class FirstOf<T> extends Reducer<T, T> {

    private Predicate<T> predicate;

    public FirstOf(@Nonnull Predicate<T> predicate) {
        Objects.requireNonNull(predicate);
        this.predicate = predicate;
    }

    public FirstOf() {
    }

    @Override
    public boolean willBreak(Collection<T> elements) {
        if (CollectionUtils.isEmpty(elements)) {
            return false;
        }

        for (T element : elements) {
            if (null == predicate || predicate.test(element)) {
                this.setResult(element);
                this.setBreak();
                return true;
            }
        }
        return false;
    }

    @Override
    public ReduceType reducerType() {
        return ReduceType.FIRST;
    }


    @Nullable
    @Override
    public T reduce(Collection<T> elements) {

        if (isHasBreak()) {
            return getResult();
        }

        if (CollectionUtils.isEmpty(elements)) {
            return null;
        }

        for (T element : elements) {
            if (null == predicate)
                return element;
            if (predicate.test(element))
                return element;
        }
        return null;
    }

}

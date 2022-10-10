package org.hiforce.lattice.runtime.ability.reduce;


import lombok.Getter;
import org.apache.commons.collections4.CollectionUtils;
import org.hiforce.lattice.annotation.model.ReduceType;
import org.hiforce.lattice.model.ability.execute.Reducer;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Objects;
import java.util.function.Predicate;

/**
 * @author Rocky Yu
 * @since 2022/9/23
 */
public class AllMatch<T> extends Reducer<T, Boolean> {

    @Getter
    private final Predicate<T> predicate;

    public AllMatch(@Nonnull Predicate<T> predicate) {
        Objects.requireNonNull(predicate);
        this.predicate = predicate;
        setResult(true);
    }

    @Override
    public boolean willBreak(Collection<T> elements) {
        if (CollectionUtils.isEmpty(elements)) {
            return false;
        } else {
            for (T element : elements) {
                if (!predicate.test(element)) {
                    this.setBreak();
                    this.setResult(false);
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public ReduceType reducerType() {
        return ReduceType.ALL;
    }

    @Override
    public Boolean reduce(Collection<T> elements) {
        if (isHasBreak()) {
            return getResult();
        }
        return true;
    }
}

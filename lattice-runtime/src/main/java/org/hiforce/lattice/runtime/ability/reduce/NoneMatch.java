package org.hiforce.lattice.runtime.ability.reduce;

import lombok.Getter;
import org.apache.commons.collections4.CollectionUtils;
import org.hiforce.lattice.annotation.model.ReduceType;
import org.hiforce.lattice.model.ability.execute.Reducer;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Objects;
import java.util.function.Predicate;

public class NoneMatch<T> extends Reducer<T, Boolean> {

    @Getter
    private final Predicate<T> predicate;

    public NoneMatch(@Nonnull Predicate<T> predicate) {
        Objects.requireNonNull(predicate);
        this.predicate = predicate;
        this.setResult(true);
    }

    @Override
    public boolean willBreak(Collection<T> elements) {
        if (CollectionUtils.isEmpty(elements)) {
            return true;
        }

        for (T element : elements) {
            if (predicate.test(element)) {
                this.setBreak();
                this.setResult(false);
                return true;
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
        return isHasBreak() ? getResult() : true;
    }
}

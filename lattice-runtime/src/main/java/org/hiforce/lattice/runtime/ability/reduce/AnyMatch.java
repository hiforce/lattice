package org.hiforce.lattice.runtime.ability.reduce;

import lombok.Getter;
import org.apache.commons.collections4.CollectionUtils;
import org.hifforce.lattice.annotation.model.ReduceType;
import org.hifforce.lattice.model.ability.execute.Reducer;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Objects;
import java.util.function.Predicate;

/**
 * @author Rocky Yu
 * @since 2022/9/23
 */
public class AnyMatch<T> extends Reducer<T, Boolean> {

    @Getter
    private final Predicate<T> predicate;

    public AnyMatch(@Nonnull Predicate<T> predicate) {
        Objects.requireNonNull(predicate);
        this.predicate = predicate;
        this.setResult(false);
    }

    @Override
    public boolean willBreak(Collection<T> elements) {
        if (CollectionUtils.isEmpty(elements)) {
            return false;
        }
        for (T element : elements) {
            if (predicate.test(element)) {
                this.setBreak();
                this.setResult(true);
                return true;
            }
        }
        return false;
    }

    @Override
    public ReduceType reducerType() {
        return ReduceType.FIRST;
    }

    @Override
    public Boolean reduce(Collection<T> elements) {

        if (isHasBreak()) {
            return getResult();
        }
        //如果之前都没有中断,说明没有命中
        return false;
    }
}

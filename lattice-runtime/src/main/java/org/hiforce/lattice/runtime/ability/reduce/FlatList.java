package org.hiforce.lattice.runtime.ability.reduce;

import com.google.common.collect.Lists;
import lombok.Getter;
import org.apache.commons.collections4.CollectionUtils;
import org.hiforce.lattice.annotation.model.ReduceType;
import org.hiforce.lattice.model.ability.execute.Reducer;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

/**
 * @author Rocky Yu
 * @since 2022/9/23
 */
public class FlatList<T> extends Reducer<List<T>, List<T>> {

    @Getter
    private final Predicate<List<T>> predicate;

    public FlatList(@Nonnull Predicate<List<T>> predicate) {
        Objects.requireNonNull(predicate);
        this.predicate = predicate;
    }

    @Override
    public boolean willBreak(Collection<List<T>> elements) {
        return false;
    }

    @Override
    public ReduceType reducerType() {
        return ReduceType.ALL;
    }

    @Override
    public List<T> reduce(Collection<List<T>> elements) {
        if (CollectionUtils.isEmpty(elements)) {
            return Lists.newArrayList();
        }
        List<T> results = Lists.newArrayList();

        for (List<T> element : elements) {
            if (predicate.test(element))
                if (CollectionUtils.isNotEmpty(element))
                    results.addAll(element);
        }
        return results;

    }
}

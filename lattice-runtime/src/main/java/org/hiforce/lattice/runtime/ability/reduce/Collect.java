package org.hiforce.lattice.runtime.ability.reduce;

import lombok.Getter;
import org.apache.commons.collections4.CollectionUtils;
import org.hifforce.lattice.model.ability.execute.Reducer;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

/**
 * @author Rocky Yu
 * @since 2022/9/23
 */
public class Collect<T> extends Reducer<T, List<T>> {

    @Getter
    private final Predicate<T> predicate;

    public Collect(@Nonnull Predicate<T> predicate) {
        Objects.requireNonNull(predicate);
        this.predicate = predicate;
    }

    @Override
    public boolean willBreak(Collection<T> elements) {
        return false;
    }

    @Override
    public List<T> reduce(Collection<T> elements) {

        if (CollectionUtils.isEmpty(elements)) {
            return new ArrayList<T>(0);
        }

        List<T> results = new ArrayList<T>(10);

        for (T element : elements) {
            if (predicate.test(element))
                results.add(element);
        }

        return results;

    }
}

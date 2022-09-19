package org.hiforce.lattice.runtime.ability.reduce;

import org.hifforce.lattice.model.ability.execute.Reducer;

import javax.annotation.Nonnull;
import java.util.function.Predicate;

/**
 * @author Rocky Yu
 * @since 2022/9/16
 */
public class Reducers {

    /**
     * Build a FirstOf Reducer.
     *
     * @param predicate the condition predicate.
     * @return FirstOf Policy Reducer.
     */
    public static <T> Reducer<T, T> firstOf(@Nonnull Predicate<T> predicate) {
        return new FirstOf<>(predicate);

    }
}

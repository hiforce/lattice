package org.hiforce.lattice.ability.reduce;

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

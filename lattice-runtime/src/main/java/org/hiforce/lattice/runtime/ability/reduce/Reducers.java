package org.hiforce.lattice.runtime.ability.reduce;

import org.hifforce.lattice.model.ability.execute.Reducer;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

/**
 * @author Rocky Yu
 * @since 2022/9/16
 */
@SuppressWarnings("all")
public class Reducers {

    /**
     * No reduce policy needed.
     *
     * @return None type reducer
     */
    public static <T> Reducer<T, List<T>> none() {
        return new None<>();
    }

    /**
     * Build a FirstOf Reducer.
     *
     * @param predicate the condition predicate.
     * @return FirstOf Policy Reducer.
     */
    public static <T> Reducer<T, T> firstOf(@Nonnull Predicate<T> predicate) {
        return new FirstOf<>(predicate);
    }


    /**
     * Build a FirstOf Reducer.
     *
     * @return FirstOf Policy Reducer.
     */
    public static <T> Reducer<T, T> firstOf() {
        return new FirstOf<>();
    }

    /**
     * Build a AllMatch Reducer
     *
     * @param predicate the condition predicate.
     * @return AllMatch Policy Reducer.
     */
    public static <T> Reducer<T, Boolean> allMatch(@Nonnull Predicate<T> predicate) {
        return new AllMatch<>(predicate);
    }

    /**
     * Build a AllMatchNotEmpty Reducer.
     *
     * @param predicate the condition predicate.
     * @return AllMatchNotEmpty Policy Reducer.
     */
    public static <T> Reducer<T, Boolean> AllMatchNotEmpty(@Nonnull Predicate<T> predicate) {
        return new AllMatchNotEmpty<T>(predicate);
    }

    /**
     * Build a AnyMatch Reducer.
     *
     * @param predicate the condition predicate.
     * @return AnyMatch Policy Reducer.
     */
    public static <T> Reducer<T, Boolean> anyMatch(@Nonnull Predicate<T> predicate) {
        return new AnyMatch<>(predicate);
    }

    /**
     * Build a NoneMatch Reducer.
     *
     * @param predicate the condition predicate.
     * @return NoneMatch Policy Reducer.
     */
    public static <T> Reducer<T, Boolean> noneMatch(@Nonnull Predicate<T> predicate) {
        return new NoneMatch<>(predicate);
    }

    /**
     * Build a FlatList Reducer.
     *
     * @param predicate the condition predicate.
     * @return FlatList Policy Reducer.
     */
    public static <T> Reducer<List<T>, List<T>> flatList(@Nonnull Predicate<List<T>> predicate) {
        return new FlatList<>(predicate);
    }

    /**
     * Build a FlatMap Reducer.
     *
     * @param predicate the condition predicate.
     * @return FlatMap Policy Reducer.
     */
    public static <K, V> Reducer<Map<K, V>, Map<K, V>> flatMap(@Nonnull Predicate<Map<K, V>> predicate) {
        return new FlatMap<>(predicate);
    }
}

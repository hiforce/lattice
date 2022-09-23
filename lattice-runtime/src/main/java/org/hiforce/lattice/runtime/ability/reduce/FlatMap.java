package org.hiforce.lattice.runtime.ability.reduce;

import com.google.common.collect.Maps;
import lombok.Getter;
import org.apache.commons.collections4.CollectionUtils;
import org.hifforce.lattice.model.ability.execute.Reducer;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;

/**
 * @author Rocky Yu
 * @since 2022/9/23
 */
public class FlatMap<K, V> extends Reducer<Map<K, V>, Map<K, V>> {

    @Getter
    private final Predicate<Map<K, V>> predicate;

    public FlatMap(@Nonnull Predicate<Map<K, V>> predicate) {
        Objects.requireNonNull(predicate);
        this.predicate = predicate;

    }

    @Override
    public boolean willBreak(Collection<Map<K, V>> elements) {
        return false;
    }

    @Override
    public Map<K, V> reduce(Collection<Map<K, V>> elements) {

        if (CollectionUtils.isEmpty(elements)) {
            return Maps.newHashMap();
        }

        Map<K, V> results = Maps.newHashMap();

        for (Map<K, V> element : elements) {
            if (predicate.test(element)) {
                results.putAll(element);
            }
        }
        return results;
    }
}

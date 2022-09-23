package org.hiforce.lattice.runtime.ability.reduce;

import com.google.common.collect.Lists;
import org.apache.commons.collections4.CollectionUtils;
import org.hifforce.lattice.model.ability.execute.Reducer;

import java.util.Collection;
import java.util.List;

/**
 * @author Rocky Yu
 * @since 2022/9/23
 */
public class None<T> extends Reducer<T, List<T>> {

    public None() {

    }

    @Override
    public boolean willBreak(Collection<T> elements) {
        return false;
    }

    @Override
    public List<T> reduce(Collection<T> elements) {
        if (CollectionUtils.isEmpty(elements)) {
            return Lists.newArrayList();
        }
        List<T> results = Lists.newArrayList();
        results.addAll(elements);
        return results;
    }

}

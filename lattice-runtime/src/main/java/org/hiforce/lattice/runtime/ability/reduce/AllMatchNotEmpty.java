package org.hiforce.lattice.runtime.ability.reduce;

import org.apache.commons.collections4.CollectionUtils;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.function.Predicate;

public class AllMatchNotEmpty<T> extends AllMatch<T> {

    public AllMatchNotEmpty(@Nonnull Predicate<T> predicate) {
        super(predicate);
    }

    @Override
    public Boolean reduce(Collection<T> elements) {
        if (isHasBreak())
            return getResult();
        return !CollectionUtils.isEmpty(elements);
    }
}

package org.hiforce.lattice.model.ability.execute;


import lombok.Getter;
import lombok.Setter;
import org.hiforce.lattice.annotation.model.ReduceType;

import java.util.Collection;

/**
 * @author Rocky Yu
 * @since 2022/9/16
 */
public abstract class Reducer<T, R> {

    @Getter
    private boolean hasBreak = false;

    @Getter
    @Setter
    private R result;

    public abstract R reduce(Collection<T> elements);

    public abstract boolean willBreak(Collection<T> elements);


    public final void setBreak() {
        this.hasBreak = true;
    }

    public abstract ReduceType reducerType();

    public String reduceName() {
        return this.getClass().getSimpleName();
    }
}

package org.hiforce.lattice.extension;


import lombok.Getter;
import lombok.Setter;
import org.hiforce.lattice.model.ability.IAbility;
import org.hiforce.lattice.model.ability.IBusinessExt;
import org.hiforce.lattice.model.ability.execute.ExtensionCallback;
import org.hiforce.lattice.model.business.IBizObject;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * @author Rocky Yu
 * @since 2022/9/18
 */
public abstract class ExtensionRunner<R> {

    @Getter
    @Setter
    private String extensionCode;

    @Getter
    @Setter
    private IBusinessExt model;

    public abstract void setAbility(IAbility ability);

    public ExtensionRunner(String extensionCode) {
        this.extensionCode = extensionCode;
    }

    public ExtensionRunner(String extensionCode, IBusinessExt model) {
        this.model = model;
        this.extensionCode = extensionCode;
    }

    public abstract R runFirstMatched(
            IBizObject bizObject, ExtensionCallback<IBusinessExt, R> callback, RunnerExecuteResult executeResult);

    @Nonnull
    public abstract List<R> runAllMatched(
            IBizObject bizObject, ExtensionCallback<IBusinessExt, R> callback, RunnerExecuteResult executeResult);

    public abstract ExtensionRunnerType getType();

    public static class RunnerExecuteResult {

        @Getter
        @Setter
        private boolean execute;

        @Getter
        @Setter
        private ExtensionRunnerType runnerType;

    }

    @SuppressWarnings("all")
    public static final class CollectionRunnerExecuteResult extends RunnerExecuteResult {
        private List results;

        public final List getResults() {
            return results;
        }

        public final void setResults(List results) {
            this.results = results;
        }
    }
}

package org.hiforce.lattice.runtime.ability.execute.runner;


import lombok.extern.slf4j.Slf4j;
import org.hiforce.lattice.extension.ExtensionRunner;
import org.hiforce.lattice.extension.ExtensionRunnerType;
import org.hiforce.lattice.message.Message;
import org.hiforce.lattice.model.ability.IAbility;
import org.hiforce.lattice.model.ability.IBusinessExt;
import org.hiforce.lattice.model.ability.execute.ExtensionCallback;
import org.hiforce.lattice.model.business.IBizObject;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;

/**
 * @author Rocky Yu
 * @since 2022/9/19
 */
@Slf4j
public class ExtensionJavaRunner<R> extends ExtensionRunner<R> {


    @Override
    public R runFirstMatched(
            IBizObject bizObject, ExtensionCallback<IBusinessExt, R> callback, RunnerExecuteResult executeResult) {
        if (null == this.getModel()) {
            log.warn(Message.code("LATTICE-CORE-RT-0009", this.getExtensionCode()).toString());
            return null;
        }
        executeResult.setExecute(true); //JAVA调用，始终是true
        executeResult.setRunnerType(getType());
        return callback.apply(getModel());
    }

    @Override
    public void setAbility(IAbility ability) {

    }

    public ExtensionJavaRunner(String extensionCode, IBusinessExt model) {
        super(extensionCode, model);
    }


    @Nonnull
    @Override
    public List<R> runAllMatched(
            IBizObject bizObject, ExtensionCallback<IBusinessExt, R> callback, RunnerExecuteResult executeResult) {
        return Collections.singletonList(runFirstMatched(bizObject, callback, executeResult));
    }

    @Override
    public ExtensionRunnerType getType() {
        return ExtensionRunnerType.JAVA;
    }
}

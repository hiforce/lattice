package org.hiforce.lattice.runtime.ability.execute.runner;


import lombok.extern.slf4j.Slf4j;
import org.hifforce.lattice.message.Message;
import org.hifforce.lattice.model.ability.IBusinessExt;
import org.hifforce.lattice.model.ability.execute.ExtensionCallback;
import org.hifforce.lattice.model.business.IBizObject;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;

/**
 * @author Rocky Yu
 * @since 2022/9/19
 */
@Slf4j
public class ExtensionJavaRunner<T extends IBusinessExt, R> extends ExtensionRunner<T, R> {


    @Override
    public R runFirstMatched(Object abilityInstance, IBizObject bizInstance,
                             ExtensionCallback<IBusinessExt, R> callback, RunnerExecuteResult executeResult) {
        if (null == this.getModel()) {
            log.warn(Message.code("LATTICE-CORE-RT-0009", this.getExtensionCode()).toString());
            return null;
        }
        executeResult.setExecute(true); //JAVA调用，始终是true
        executeResult.setRunnerType(getType());
        return callback.apply(getModel());
    }

    public ExtensionJavaRunner(String extensionCode, T model) {
        super(extensionCode, model);
    }


    @Nonnull
    @Override
    public List<R> runAllMatched(Object abilityInstance, IBizObject bizInstance, ExtensionCallback<IBusinessExt, R> callback, RunnerExecuteResult executeResult) {
        return Collections.singletonList(runFirstMatched(abilityInstance, bizInstance, callback, executeResult));//Java类的，没有多个匹配的。。。
    }

    @Override
    public ExtensionRunnerType getType() {
        return ExtensionRunnerType.JAVA;
    }
}

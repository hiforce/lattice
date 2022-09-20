package org.hiforce.lattice.runtime.ability.execute;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.collections4.CollectionUtils;
import org.hifforce.lattice.model.business.ITemplate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Rocky Yu
 * @since 2022/9/18
 */
public class ExecuteResult<R> {

    @Getter
    @Setter
    private R result;

    /**
     * The detail multi-runner execution result of the extension point.
     */
    @Getter
    private final List<RunnerExecutionStatus> detailRunnerResults = new ArrayList<>(10);

    private static final ExtensionRunner.CollectionRunnerExecuteResult DUMMY = new ExtensionRunner.CollectionRunnerExecuteResult();

    static {
        DUMMY.setResults(Collections.emptyList());
    }


    public static <T> ExecuteResult<T> success(
            T model, List<ITemplate> runners,
            List<ExtensionRunner.CollectionRunnerExecuteResult> executeResults) {

        ExecuteResult<T> result = new ExecuteResult<>();
        result.setResult(model);
        if (CollectionUtils.isNotEmpty(runners)) {
            int totalLen = executeResults.size();
            for (int i = 0; i < runners.size(); i++) {
                if (i < totalLen) {
                    result.detailRunnerResults.add(toRunnerExecutionStatus(runners.get(i), executeResults.get(i)));
                } else {
                    result.detailRunnerResults.add(toRunnerExecutionStatus(runners.get(i), DUMMY));
                }
            }
        }
        return result;
    }

    @SuppressWarnings("all")
    private static RunnerExecutionStatus toRunnerExecutionStatus(ITemplate runner, ExtensionRunner.CollectionRunnerExecuteResult executeResult) {
        RunnerExecutionStatus status = new RunnerExecutionStatus();
        if (null != runner) {
            status.setTemplateCode(runner.getCode());
            status.setTemplateType(runner.getType());
        }

        status.setExecuted(executeResult.isExecute());
        status.setInvokeResults(executeResult.getResults());
        status.setType(executeResult.getRunnerType());
        return status;
    }
}

package org.hiforce.lattice.runtime.ability.execute;

import lombok.Getter;
import lombok.Setter;
import org.hifforce.lattice.model.business.TemplateType;

import java.util.List;

/**
 * @author Rocky Yu
 * @since 2022/9/18
 */
public class RunnerExecutionStatus {

    @Getter
    @Setter
    private String templateCode;

    @Getter
    @Setter
    private TemplateType templateType;

    @Getter
    @Setter
    private boolean executed;

    @Getter
    @Setter
    private List<Object> invokeResults;

    @Getter
    @Setter
    private ExtensionRunnerType type;
}

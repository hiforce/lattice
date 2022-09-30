package org.hifforce.lattice.extension;

import lombok.Getter;
import lombok.Setter;
import org.hifforce.lattice.model.register.TemplateSpec;

/**
 * @author Rocky Yu
 * @since 2022/10/1
 */
@SuppressWarnings("all")
public class RunnerItemEntry<R> {

    @Getter
    @Setter
    private TemplateSpec template;

    @Getter
    private ExtensionRunner<R> runner;

    public ExtensionRunnerType getRunnerType() {
        return runner.getType();
    }

    public RunnerItemEntry(TemplateSpec template, ExtensionRunner<R> runner) {
        this.template = template;
        this.runner = runner;
    }

    @Override
    public String toString() {
        return "[" + template.getCode() + "|"
                + (runner.getModel() != null ? runner.getModel().getClass().getName() : null) + "]";
    }
}
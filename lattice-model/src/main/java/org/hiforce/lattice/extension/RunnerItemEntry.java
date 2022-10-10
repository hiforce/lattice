package org.hiforce.lattice.extension;

import lombok.Getter;
import lombok.Setter;
import org.hiforce.lattice.model.ability.IAbility;
import org.hiforce.lattice.model.register.TemplateSpec;

/**
 * @author Rocky Yu
 * @since 2022/10/1
 */
@SuppressWarnings("all")
public class RunnerItemEntry<R> {

    @Getter
    private IAbility ability;

    @Getter
    @Setter
    private TemplateSpec template;

    @Getter
    private ExtensionRunner<R> runner;

    public ExtensionRunnerType getRunnerType() {
        return runner.getType();
    }

    public RunnerItemEntry(IAbility ability, TemplateSpec template, ExtensionRunner<R> runner) {
        this.template = template;
        this.runner = runner;
        this.ability = ability;
    }

    @Override
    public String toString() {
        return "[" + template.getCode() + "|"
                + (runner.getModel() != null ? runner.getModel().getClass().getName() : null) + "]";
    }
}
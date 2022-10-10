package org.hiforce.lattice.runtime.ability.register;

import lombok.Getter;
import org.hiforce.lattice.message.Message;
import org.hiforce.lattice.model.register.AbilityInstSpec;

/**
 * @author Rocky Yu
 * @since 2022/9/16
 */
public class AbilityInstBuildResult {

    @Getter
    private boolean success;

    @Getter
    private boolean registered;

    @Getter
    private Message message;

    @Getter
    private AbilityInstSpec instanceSpec;

    private AbilityInstBuildResult() {

    }

    public static AbilityInstBuildResult registered() {
        AbilityInstBuildResult result = new AbilityInstBuildResult();
        result.registered = true;
        return result;
    }

    public static AbilityInstBuildResult success(AbilityInstSpec instanceSpec) {
        AbilityInstBuildResult result = new AbilityInstBuildResult();
        result.success = true;
        result.instanceSpec = instanceSpec;
        return result;
    }

    public static AbilityInstBuildResult failed(Message message) {
        AbilityInstBuildResult result = new AbilityInstBuildResult();
        result.message = message;
        return result;
    }
}

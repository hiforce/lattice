package org.hiforce.lattice.remote.container.service;

import org.apache.commons.beanutils.MethodUtils;
import org.apache.commons.lang3.StringUtils;
import org.hiforce.lattice.model.ability.IBusinessExt;
import org.hiforce.lattice.model.register.RealizationSpec;
import org.hiforce.lattice.remote.client.LatticeRemoteInvoker;
import org.hiforce.lattice.runtime.Lattice;
import org.hiforce.lattice.utils.BusinessExtUtils;

import java.io.Serializable;
import java.lang.reflect.Method;

/**
 * @author Rocky Yu
 * @since 2022/9/30
 */
public class LatticeRemoteInvokerImpl implements LatticeRemoteInvoker {

    @Override
    @SuppressWarnings("all")
    public Serializable invoke(String bizCode, String scenario, String extCode, Object... params) {
        RealizationSpec realizationSpec = Lattice.getInstance().getAllRealizations().stream()
                .filter(p -> StringUtils.equals(bizCode, p.getCode()))
                .filter(p -> isScenarioMatched(scenario, p.getScenario()))
                .filter(p -> p.getExtensionCodes().contains(extCode))
                .findFirst().orElse(null);
        if (null == realizationSpec) {
            return null;
        }
        try {
            IBusinessExt businessExt = realizationSpec.getBusinessExt().getBusinessExtByCode(extCode, scenario);
            Method method = BusinessExtUtils.getExtensionMethod(businessExt, extCode, scenario);
            return (Serializable) method.invoke(realizationSpec.getBusinessExt(), params);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private boolean isScenarioMatched(String targetScenario, String specScenario) {
        if (StringUtils.isEmpty(targetScenario) && StringUtils.isEmpty(specScenario)) {
            return true;
        }
        if (null == targetScenario || null == specScenario) {
            return false;
        }
        return StringUtils.equals(targetScenario, specScenario);

    }
}

package org.hiforce.lattice.remote.container.service;

import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class LatticeRemoteInvokerImpl implements LatticeRemoteInvoker {

    @Override
    @SuppressWarnings("all")
    public Serializable invoke(String bizCode, String scenario, String extCode, Object... params) {
        log.info("[Lattice]Remote Invoked, bizCode={}, scenario={}, extCode={} ", bizCode, scenario, extCode);
        RealizationSpec realizationSpec = Lattice.getInstance().getAllRealizations().stream()
                .filter(p -> StringUtils.equals(bizCode, p.getCode()))
                .filter(p -> isScenarioMatched(scenario, p.getScenario()))
                .filter(p -> p.getExtensionCodes().contains(extCode))
                .findFirst().orElse(null);
        if (null == realizationSpec) {
            log.info("[Lattice] The realization not found. bizCode={}, scenario={}, extCode={} ", bizCode, scenario, extCode);
            return null;
        }
        try {
            IBusinessExt businessExt = realizationSpec.getBusinessExt().getBusinessExtByCode(extCode, scenario);
            Method method = BusinessExtUtils.getExtensionMethod(businessExt, extCode, scenario);
            Serializable value = (Serializable) method.invoke(realizationSpec.getBusinessExt(), params);
            log.debug("[Lattice] Remote invoke result={}", null == value ? null : value.toString());
            return value;
        } catch (Exception e) {
            log.info("[Lattice] Remote invoke runtime exception occurred. ex=bizCode={}, scenario={}, extCode={}, ex={}",
                    bizCode, scenario, extCode, e.getMessage());
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

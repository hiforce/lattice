package org.hiforce.lattice.remote.container.service;

import org.apache.commons.lang3.StringUtils;
import org.hifforce.lattice.model.register.RealizationSpec;
import org.hifforce.lattice.utils.BusinessExtUtils;
import org.hiforce.lattice.remote.client.LatticeRemoteInvoker;
import org.hiforce.lattice.runtime.Lattice;

import java.io.Serializable;
import java.lang.reflect.Method;

/**
 * @author Rocky Yu
 * @since 2022/9/30
 */
public class LatticeRemoteInvokerImpl implements LatticeRemoteInvoker {

    @Override
    @SuppressWarnings("all")
    public Serializable invoke(String bizCode, String scenario, String extCode, Serializable... params) {
        RealizationSpec realizationSpec = Lattice.getInstance().getAllRealizations().stream()
                .filter(p -> StringUtils.equals(bizCode, p.getCode()))
                .filter(p -> StringUtils.equals(scenario, p.getScenario()))
                .filter(p -> p.getExtensionCodes().contains(extCode))
                .findFirst().orElse(null);
        if (null == realizationSpec) {
            return null;
        }
        Method method = BusinessExtUtils.getExtensionMethod(realizationSpec.getBusinessExt(), extCode, scenario);
        try {
            return (Serializable) method.invoke(realizationSpec.getBusinessExt(), params);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}

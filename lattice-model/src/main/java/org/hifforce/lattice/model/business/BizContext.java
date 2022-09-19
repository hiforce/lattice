package org.hifforce.lattice.model.business;

import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;

/**
 * @author Rocky Yu
 * @since 2022/9/16
 */
public abstract class BizContext implements Serializable {

    private static final long serialVersionUID = 4886210062129912701L;

    public abstract Serializable getBizId();

    public abstract String getBizCode();

    public abstract String getScenario();


    private int hashCode;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BizContext)) return false;

        BizContext that = (BizContext) o;
        if (!StringUtils.equals(getBizCode(), that.getBizCode()))
            return false;
        if (null == getBizId() || !getBizId().equals(that.getBizId()))
            return false;
        return true;
    }

    @Override
    public int hashCode() {
        int hashCode = this.hashCode;
        if (hashCode > 0) {
            return hashCode;
        }
        int result = 31;
        if (StringUtils.isNotEmpty(getBizCode())) {
            result *= getBizCode().hashCode();
        }
        if (null != getBizId()) {
            result = 31 * result + getBizId().hashCode();
        }
        this.hashCode = result;
        return result;
    }
}

package org.hifforce.lattice.model.business;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;

/**
 * @author Rocky Yu
 * @since 2022/9/16
 */
public class BizContext implements Serializable {

    private static final long serialVersionUID = 4886210062129912701L;

    @Getter
    @Setter
    private Serializable bizId;

    @Getter
    private final String bizCode;

    @Getter
    @Setter
    private String scenario;

    @Getter
    @Setter
    private Serializable extraInfo;

    private int hashCode;


    protected BizContext(String bizCode, Serializable bizId) {
        this.bizCode = bizCode;
        this.bizId = bizId;
        this.hashCode = this.hashCode();
    }

    protected BizContext(String bizCode, Serializable bizId, Serializable extraInfo) {
        this.bizCode = bizCode;
        this.bizId = bizId;
        this.hashCode = this.hashCode();
        this.extraInfo = extraInfo;
    }

    public static BizContext of(String bizCode, Serializable bizId) {
        return new BizContext(bizCode, bizId);
    }

    public static  BizContext of(String bizCode, Serializable bizId, Serializable extraInfo) {
        return new BizContext(bizCode, bizId, extraInfo);
    }

    public static BizContext ofId(Serializable bizId) {
        return new BizContext(null, bizId);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BizContext)) return false;

        BizContext that = (BizContext) o;
        if (!StringUtils.equals(bizCode, that.bizCode))
            return false;
        if (null == bizId || !bizId.equals(that.bizId))
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
        if (StringUtils.isNotEmpty(bizCode)) {
            result *= bizCode.hashCode();
        }
        if (null != bizId) {
            result = 31 * result + bizId.hashCode();
        }
        this.hashCode = result;
        return result;
    }

    public void setBizInstanceId(Serializable bizId) {
        this.bizId = bizId;
        if (hashCode > 0) {
            hashCode = 0;
        }
    }
}

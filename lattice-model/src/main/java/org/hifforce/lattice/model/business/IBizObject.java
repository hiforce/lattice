package org.hifforce.lattice.model.business;

import java.io.Serializable;
import java.util.Optional;

/**
 * @author Rocky Yu
 * @since 2022/9/16
 */
public interface IBizObject {

    IBizObject DUMMY = new IBizObject() {
        @Override
        public BizContext getBizContext() {
            return null;
        }
    };

    BizContext getBizContext();

    default String getBizCode() {
        return Optional.ofNullable(getBizContext())
                .map(BizContext::getBizCode).orElse(null);
    }

    default Serializable getBizId(){
        return Optional.ofNullable(getBizContext())
                .map(BizContext::getBizId).orElse(null);
    }
}

package org.hiforce.lattice.model.business;

import javax.annotation.Nullable;
import java.io.Serializable;
import java.util.Optional;

/**
 * @author Rocky Yu
 * @since 2022/9/16
 */
public interface IBizObject {

    BizContext getBizContext();

    default String getBizCode() {
        return Optional.ofNullable(getBizContext())
                .map(BizContext::getBizCode).orElse(null);
    }

    default Serializable getBizId() {
        return Optional.ofNullable(getBizContext())
                .map(BizContext::getBizId).orElse(null);
    }

    @SuppressWarnings("unused")
    default <T> void addExtObject(Class<? super T> klass, @Nullable T instance) {
        getBizContext().addExtObject(klass, instance);
    }

    @SuppressWarnings("unused")
    default <T> T getExtObject(Class<? extends T> klass) {
        return getBizContext().getExtObject(klass);
    }
}

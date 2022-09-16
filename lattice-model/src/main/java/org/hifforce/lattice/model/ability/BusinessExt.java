package org.hifforce.lattice.model.ability;

import lombok.NonNull;

import java.util.List;

/**
 * @author Rocky Yu
 * @since 2022/9/16
 */
public abstract class BusinessExt implements IBusinessExt{

    @Override
    public IBusinessExt getBusinessExtByCode(String extCode) {
        return null;
    }

    @Override
    public IBusinessExt getBusinessExtByCode(String extCode, String scenario) {
        return null;
    }

    @Override
    public @NonNull List<IBusinessExt> getAllSubBusinessExt() {
        return null;
    }
}

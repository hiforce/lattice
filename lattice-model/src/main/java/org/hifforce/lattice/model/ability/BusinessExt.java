package org.hifforce.lattice.model.ability;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * @author Rocky Yu
 * @since 2022/9/16
 */
@Slf4j
public abstract class BusinessExt implements IBusinessExt {

    @Override
    public IBusinessExt getBusinessExtByCode(String extCode, String scenario) {
        return null;
    }

    @Override
    public @NonNull List<IBusinessExt> getAllSubBusinessExt() {
        return null;
    }
}

package org.hiforce.lattice.runtime.cache;

import lombok.NonNull;
import org.hiforce.lattice.model.ability.IBusinessExt;

import java.util.List;

/**
 * @author Rocky Yu
 * @since 2022/9/19
 */
public class NotExistedExtensionPointRealization implements IBusinessExt {


    @Override
    public IBusinessExt getBusinessExtByCode(String extCode, String scenario) {
        return null;
    }

    @Override
    public @NonNull List<IBusinessExt> getAllSubBusinessExt() {
        return null;
    }

}

package org.hiforce.lattice.runtime.template;

import com.google.common.collect.Lists;
import lombok.Getter;
import org.hiforce.lattice.model.ability.IBusinessExt;

import java.util.List;

/**
 * @author Rocky Yu
 * @since 2022/9/18
 */
public class LatticeTemplateManager {

    @Getter
    private final List<IBusinessExt> realizations = Lists.newArrayList();

}

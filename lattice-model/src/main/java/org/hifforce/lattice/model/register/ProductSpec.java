package org.hifforce.lattice.model.register;

import com.google.common.collect.Lists;
import lombok.Getter;
import lombok.Setter;
import org.hifforce.lattice.model.ability.IBusinessExt;
import org.hifforce.lattice.model.business.TemplateType;

import java.util.List;

/**
 * @author Rocky Yu
 * @since 2022/9/20
 */
public class ProductSpec extends BaseSpec {

    @Getter
    @Setter
    private Class<?> productClass;

    @Getter
    @Setter
    private TemplateType type;

    @Getter
    @Setter
    private Class<? extends IBusinessExt> businessExt;

    @Getter
    @Setter
    private int priority;

    @Getter
    private final List<RealizationSpec> realizations = Lists.newArrayList();
}

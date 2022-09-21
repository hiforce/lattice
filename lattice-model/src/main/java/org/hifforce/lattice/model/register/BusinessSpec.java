package org.hifforce.lattice.model.register;

import com.google.common.collect.Lists;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * @author Rocky Yu
 * @since 2022/9/21
 */
public class BusinessSpec extends BaseSpec {

    @Getter
    @Setter
    private Class<?> businessClass;

    @Getter
    @Setter
    private int priority = 1000;

    @Getter
    private final List<RealizationSpec> realizations = Lists.newArrayList();
}

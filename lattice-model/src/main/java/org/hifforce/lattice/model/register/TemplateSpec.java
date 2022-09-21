package org.hifforce.lattice.model.register;

import com.google.common.collect.Lists;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * @author Rocky Yu
 * @since 2022/9/21
 */
public class TemplateSpec extends BaseSpec {

    @Getter
    @Setter
    private int priority;

    @Getter
    private final List<RealizationSpec> realizations = Lists.newArrayList();
}

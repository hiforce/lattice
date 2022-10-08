package org.hiforce.lattice.maven.model;

import com.google.common.collect.Lists;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * @author Rocky Yu
 * @since 2022/10/8
 */
public class AbilityInstInfo extends BaseInfo {

    private static final long serialVersionUID = 6918296707529974734L;

    @Getter
    @Setter
    private String abilityCode;

    @Getter
    @Setter
    private int priority;

    @Getter
    private final List<ExtensionInfo> extensions = Lists.newArrayList();
}

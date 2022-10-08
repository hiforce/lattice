package org.hiforce.lattice.maven.model;

import com.google.common.collect.Lists;
import lombok.Getter;

import java.io.Serializable;
import java.util.List;

/**
 * @author Rocky Yu
 * @since 2022/10/8
 */
public class AbilitySet implements Serializable {

    private static final long serialVersionUID = -7444380275868696642L;

    @Getter
    private final List<AbilityInfo> providing = Lists.newArrayList();

    @Getter
    private final List<AbilityInfo> using = Lists.newArrayList();
}

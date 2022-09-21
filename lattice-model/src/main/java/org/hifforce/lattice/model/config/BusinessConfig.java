package org.hifforce.lattice.model.config;

import com.google.common.collect.Lists;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

/**
 * @author Rocky Yu
 * @since 2022/9/19
 */
@Builder
@SuppressWarnings("all")
public class BusinessConfig implements Serializable {

    private static final long serialVersionUID = 2955186375325583813L;

    @Getter
    @Setter
    private String bizCode;

    @Getter
    private List<ProductConfig> installedProducts = Lists.newArrayList();

    @Getter
    private List<PriorityConfig> priorityConfigs = Lists.newArrayList();
}

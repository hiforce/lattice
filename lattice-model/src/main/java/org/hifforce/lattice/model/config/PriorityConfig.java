package org.hifforce.lattice.model.config;

import com.google.common.collect.Lists;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

/**
 * @author Rocky Yu
 * @since 2022/9/21
 */
@SuppressWarnings("all")
@NoArgsConstructor
public class PriorityConfig implements Serializable {

    private static final long serialVersionUID = -1973462143706510775L;

    @Getter
    @Setter
    private String extCode;

    @Getter
    @Setter
    private List<ExtPriority> priorities = Lists.newArrayList();
}

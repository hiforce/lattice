package org.hifforce.lattice.model.config.builder;

import com.google.common.collect.Lists;
import lombok.Getter;
import org.apache.commons.collections4.CollectionUtils;
import org.hifforce.lattice.model.config.ExtPriority;
import org.hifforce.lattice.model.config.ExtPriorityConfig;

import java.util.List;

/**
 * @author Rocky Yu
 * @since 2022/9/24
 */
@SuppressWarnings("unused")
public class PriorityConfigBuilder {

    @Getter
    private String extCode;


    private List<ExtPriority> priorities;

    private PriorityConfigBuilder() {

    }

    public static PriorityConfigBuilder builder() {
        return new PriorityConfigBuilder();
    }

    public ExtPriorityConfig build() {
        ExtPriorityConfig config = new ExtPriorityConfig();
        config.setExtCode(extCode);
        config.getPriorities().addAll(priorities);
        return config;
    }

    public PriorityConfigBuilder extCode(String extCode) {
        this.extCode = extCode;
        return this;
    }

    public PriorityConfigBuilder priority(ExtPriority priority) {
        if (null == priority)
            return this;
        if (this.priorities == null) {
            this.priorities = Lists.newArrayList();
        }
        this.priorities.add(priority);
        return this;
    }

    public PriorityConfigBuilder priorities(List<ExtPriority> priorities) {
        if (CollectionUtils.isEmpty(priorities)) {
            this.priorities = Lists.newArrayList();
            return this;
        }
        this.priorities = priorities;
        return this;
    }
}

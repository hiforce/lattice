package org.hiforce.lattice.model.config;

import lombok.Getter;
import lombok.Setter;
import org.hiforce.lattice.model.business.TemplateType;

import java.io.Serializable;

/**
 * @author Rocky Yu
 * @since 2022/9/21
 */
public class ExtPriority implements Serializable {

    private static final long serialVersionUID = -5187745224595471480L;

    @Getter
    @Setter
    private String code;

    @Getter
    @Setter
    private TemplateType type;

    public static ExtPriority of(String code, TemplateType type) {
        ExtPriority priority = new ExtPriority();
        priority.code = code;
        priority.type = type;
        return priority;
    }
}

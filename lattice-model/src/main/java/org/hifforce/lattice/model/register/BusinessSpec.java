package org.hifforce.lattice.model.register;

import lombok.Getter;
import lombok.Setter;
import org.hifforce.lattice.model.business.TemplateType;

/**
 * @author Rocky Yu
 * @since 2022/9/21
 */
public class BusinessSpec extends TemplateSpec {

    @Getter
    @Setter
    private Class<?> businessClass;

    public BusinessSpec() {
        this.setPriority(1000);
        this.setType(TemplateType.BUSINESS);
    }
}

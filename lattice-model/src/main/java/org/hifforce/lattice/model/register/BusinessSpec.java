package org.hifforce.lattice.model.register;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.hifforce.lattice.exception.LatticeRuntimeException;
import org.hifforce.lattice.model.business.BusinessTemplate;
import org.hifforce.lattice.model.business.TemplateType;

/**
 * @author Rocky Yu
 * @since 2022/9/21
 */
@Slf4j
public class BusinessSpec extends TemplateSpec<BusinessTemplate> {

    @Getter
    @Setter
    private Class<?> businessClass;

    public BusinessSpec() {
        this.setPriority(1000);
        this.setType(TemplateType.BUSINESS);
    }

    @Override
    public BusinessTemplate newInstance() {
        if (null == businessClass) {
            return null;
        }
        try {
            BusinessTemplate template = (BusinessTemplate) businessClass.newInstance();
            template.setCode(getCode());
            template.setInternalId(getInternalId());
            template.setPriority(getPriority());
            return template;
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            throw new LatticeRuntimeException("LATTICE-CORE-004", ex.getMessage());
        }
    }
}

package org.hifforce.lattice.model.register;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.hifforce.lattice.exception.LatticeRuntimeException;
import org.hifforce.lattice.model.business.ProductTemplate;
import org.hifforce.lattice.model.business.TemplateType;

/**
 * @author Rocky Yu
 * @since 2022/9/20
 */
@Slf4j
public class ProductSpec extends TemplateSpec<ProductTemplate> {

    @Getter
    @Setter
    private Class<?> productClass;


    public ProductSpec() {
        this.setPriority(500);
        this.setType(TemplateType.PRODUCT);
    }

    public ProductTemplate newInstance() {
        if (null == productClass) {
            return null;
        }
        try {
            return (ProductTemplate) productClass.newInstance();
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            throw new LatticeRuntimeException("LATTICE-CORE-002", ex.getMessage());
        }
    }
}

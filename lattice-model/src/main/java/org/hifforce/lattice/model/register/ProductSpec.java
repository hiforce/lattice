package org.hifforce.lattice.model.register;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.hifforce.lattice.exception.LatticeRuntimeException;
import org.hifforce.lattice.model.ability.IBusinessExt;
import org.hifforce.lattice.model.business.ProductTemplate;
import org.hifforce.lattice.model.business.TemplateType;

/**
 * @author Rocky Yu
 * @since 2022/9/20
 */
@Slf4j
public class ProductSpec extends TemplateSpec {

    @Getter
    @Setter
    private Class<?> productClass;

    @Getter
    @Setter
    private TemplateType type;

    @Getter
    @Setter
    private Class<? extends IBusinessExt> businessExt;

    public ProductSpec() {
        this.setPriority(500);
        this.setType(TemplateType.PRODUCT);
    }

    public ProductTemplate createProductInstance() {
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

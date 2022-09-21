package org.hifforce.lattice.model.business;

/**
 * @author Rocky Yu
 * @since 2022/9/21
 */
public abstract class BusinessTemplate extends BaseTemplate implements IBusiness {
    @Override
    public TemplateType getType() {
        return TemplateType.BUSINESS;
    }
}

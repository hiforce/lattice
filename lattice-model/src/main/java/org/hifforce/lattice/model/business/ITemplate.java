package org.hifforce.lattice.model.business;

/**
 * @author Rocky Yu
 * @since 2022/9/18
 */
public interface ITemplate {

    String getCode();

    TemplateType getType();


    Long getInternalId();


    boolean isPatternTemplateCode();

}

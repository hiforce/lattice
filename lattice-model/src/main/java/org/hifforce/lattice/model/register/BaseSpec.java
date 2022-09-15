package org.hifforce.lattice.model.register;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

import java.util.Collection;

/**
 * @author Rocky Yu
 * @since 2022/9/15
 */
public abstract class BaseSpec {

    /**
     * Name.
     */
    @Getter
    @Setter
    private String name;

    /**
     * code.
     */
    @Getter
    private String code;

    /**
     * description.
     */
    @Getter
    @Setter
    private String description;

    /**
     * chech the current spec element whether in the specific Collection..
     *
     * @param elements the Collection create the spec elements.
     * @return true:in the collection, else for false.
     */
    public boolean inList(Collection<? extends BaseSpec> elements) {
        for (BaseSpec baseSpec : elements) {
            if (!this.getClass().getName().equals(baseSpec.getClass().getName())) {
                continue;
            }
            if (StringUtils.equals(this.getCode(), baseSpec.getCode())) {
                return true;
            }
        }
        return false;
    }

    public void setCode(String code) {
        this.code = code.intern(); //in order reduce the stack mem size.
    }
}

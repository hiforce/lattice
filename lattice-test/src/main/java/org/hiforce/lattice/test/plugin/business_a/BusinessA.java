package org.hiforce.lattice.test.plugin.business_a;

import org.hifforce.lattice.annotation.Business;
import org.hifforce.lattice.model.business.BusinessTemplate;

/**
 * @author Rocky Yu
 * @since 2022/9/21
 */
@Business(code = BusinessA.BUSINESS_A_CODE, name = "Business A")
public class BusinessA extends BusinessTemplate {
    public static final String BUSINESS_A_CODE = "business.a";
}

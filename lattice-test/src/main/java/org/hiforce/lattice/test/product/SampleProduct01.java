package org.hiforce.lattice.test.product;

import org.hifforce.lattice.annotation.Product;
import org.hifforce.lattice.model.business.ProductTemplate;
import org.hifforce.lattice.model.scenario.ScenarioRequest;

/**
 * @author Rocky Yu
 * @since 2022/9/20
 */
@Product(code = SampleProduct01.CODE, name = "SampleProduct01")
public class SampleProduct01 extends ProductTemplate {

    public static final String CODE = "org.hiforce.lattice.test.product.SampleProduct01";

    @Override
    public boolean isEffect(ScenarioRequest request) {
        return true;
    }
}

package org.hiforce.lattice.test;

import org.hifforce.lattice.model.register.*;
import org.hiforce.lattice.runtime.Lattice;
import org.hiforce.lattice.test.ability.SampleAbility;
import org.hiforce.lattice.test.model.OrderLine;
import org.hiforce.lattice.test.product.SampleProduct01;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Collection;
import java.util.Set;

import static org.hiforce.lattice.test.ability.ext.SampleBusinessExt.SAMPLE_GET_SAMPLE_EXTENSION_POINT_01;

/**
 * @author Rocky Yu
 * @since 2022/9/16
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = LatticeTestStarter.class)
@AutoConfigureMockMvc
public class LatticeGeneralTest {

    @Test
    public void test_ability_register_01() {
        Collection<AbilitySpec> abilitySpecList = Lattice.getInstance().getAllRegisteredAbilities();
        Assert.assertFalse(abilitySpecList.isEmpty());

        AbilitySpec abilitySpec = abilitySpecList.stream().findFirst().orElse(null);
        Assert.assertNotNull(abilitySpec);
        Set<AbilityInstSpec> abilityInstSpecSet = abilitySpec.getAbilityInstances();
        Assert.assertFalse(abilityInstSpecSet.isEmpty());

        AbilityInstSpec instSpec = abilityInstSpecSet.stream().findFirst().orElse(null);
        Assert.assertNotNull(instSpec);

        Set<ExtensionPointSpec> extensionPointSpecSet = instSpec.getExtensions();
        Assert.assertFalse(extensionPointSpecSet.isEmpty());

        ExtensionPointSpec extensionPointSpec = extensionPointSpecSet.stream().findFirst().orElse(null);
        Assert.assertNotNull(extensionPointSpec);

        Assert.assertEquals(extensionPointSpec.getCode(), SAMPLE_GET_SAMPLE_EXTENSION_POINT_01);
    }

    @Test
    public void test_realization_register_01() {
        Assert.assertFalse(Lattice.getInstance().getAllRegisteredRealizations().isEmpty());

        RealizationSpec spec = Lattice.getInstance().getRealizationSpecByCode("business.b");
        Assert.assertNotNull(spec);
        Assert.assertNotNull(spec.getBusinessExt());
        Assert.assertFalse(spec.getExtensionCodes().isEmpty());

    }

    @Test
    public void test_extension_invoke_01() {
        OrderLine orderLine = new OrderLine();
        orderLine.setOrderLineId(1L);
        orderLine.setBizCode("business.a");
        SampleAbility ability = new SampleAbility(orderLine);
        String value = ability.invokeTheSampleSampleExtensionPoint_01();
        System.out.println(">>>Lattice extension invoke result: " + value);
        Assert.assertNotNull(value);
    }

    @Test
    public void test_product_register_01() {
        Assert.assertFalse(Lattice.getInstance().getAllRegisteredProducts().isEmpty());
        ProductSpec productSpec = Lattice.getInstance().getRegisteredProductByCode(SampleProduct01.CODE);
        Assert.assertNotNull(productSpec);
        Assert.assertFalse(productSpec.getRealizations().isEmpty());
    }
}

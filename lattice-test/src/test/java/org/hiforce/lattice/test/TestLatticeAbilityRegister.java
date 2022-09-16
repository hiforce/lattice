package org.hiforce.lattice.test;

import org.hiforce.lattice.runtime.Lattice;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * @author Rocky Yu
 * @since 2022/9/16
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = LatticeTestStarter.class)
@AutoConfigureMockMvc
public class TestLatticeAbilityRegister {

    @Test
    public void testAbilityRegister() {
        Assert.assertFalse(Lattice.getInstance().getAllRegisteredAbilities().isEmpty());
    }
}

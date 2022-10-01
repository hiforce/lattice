package org.hiforce.lattice.test;

import org.hifforce.lattice.model.business.BizContext;
import org.hiforce.lattice.runtime.Lattice;
import org.hiforce.lattice.test.ability.SampleAbility;

import java.io.Serializable;

public class LatticeTest {

    public static void main(String[] args) {
        Lattice.getInstance().setSimpleMode(true);
        Lattice.getInstance().start();

        SampleAbility ability = new SampleAbility(() -> new BizContext() {

            @Override
            public Serializable getBizId() {
                return 1;
            }

            @Override
            public String getBizCode() {
                return "business.a";
            }

            @Override
            public String getScenario() {
                return null;
            }
        });

        ability.invokeExtension();
    }
}

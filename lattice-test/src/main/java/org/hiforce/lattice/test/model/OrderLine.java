package org.hiforce.lattice.test.model;

import lombok.Getter;
import lombok.Setter;
import org.hifforce.lattice.model.business.BizContext;
import org.hifforce.lattice.model.business.IBizObject;

import java.io.Serializable;

/**
 * @author Rocky Yu
 * @since 2022/9/19
 */
public class OrderLine implements IBizObject {

    @Getter
    @Setter
    private long orderLineId;

    @Getter
    @Setter
    private String bizCode;

    @Getter
    @Setter
    private String scenario;


    @Override
    public BizContext getBizContext() {

        return new BizContext() {

            @Override
            public Serializable getBizId() {
                return orderLineId;
            }

            @Override
            public String getBizCode() {
                return bizCode;
            }

            @Override
            public String getScenario() {
                return scenario;
            }
        };
    }
}

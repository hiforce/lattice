package org.hiforce.lattice.test.model;

import lombok.Getter;
import lombok.Setter;
import org.hifforce.lattice.model.business.BizContext;
import org.hifforce.lattice.model.business.IBizObject;
import org.hifforce.lattice.sequence.SequenceGenerator;

import java.io.Serializable;

/**
 * @author Rocky Yu
 * @since 2022/9/19
 */
public class OrderLine implements IBizObject {

    @Getter
    @Setter
    private long orderLineId = SequenceGenerator.next(OrderLine.class.getName());

    @Getter
    @Setter
    private long itemId;

    @Getter
    @Setter
    private String buyerId;

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

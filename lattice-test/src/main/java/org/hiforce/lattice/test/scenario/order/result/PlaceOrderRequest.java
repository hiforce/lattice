package org.hiforce.lattice.test.scenario.order.result;

import lombok.Getter;
import org.hifforce.lattice.model.scenario.ScenarioRequest;
import org.hiforce.lattice.test.model.OrderLine;

/**
 * @author Rocky Yu
 * @since 2022/9/21
 */
public class PlaceOrderRequest implements ScenarioRequest {

    private static final long serialVersionUID = -6668268516185898852L;

    @Getter
    private final OrderLine orderLine;

    public PlaceOrderRequest(OrderLine orderLine) {
        this.orderLine = orderLine;
    }

    @Override
    public OrderLine getBizObject() {
        return orderLine;
    }
}

package org.hiforce.lattice.test.scenario.order;

import org.hiforce.lattice.test.scenario.order.param.PlaceOrderReqDTO;
import org.hiforce.lattice.test.scenario.order.param.PlaceOrderRespDTO;

/**
 * @author Rocky Yu
 * @since 2022/9/21
 */
public interface PlaceOrderService {

    PlaceOrderRespDTO createOrder(PlaceOrderReqDTO reqDTO);
}

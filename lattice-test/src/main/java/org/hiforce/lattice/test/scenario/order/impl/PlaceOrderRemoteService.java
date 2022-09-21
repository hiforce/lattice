package org.hiforce.lattice.test.scenario.order.impl;

import org.hifforce.lattice.exception.LatticeRuntimeException;
import org.hiforce.lattice.runtime.session.BizSessionScope;
import org.hiforce.lattice.test.ability.SampleAbility;
import org.hiforce.lattice.test.model.OrderLine;
import org.hiforce.lattice.test.scenario.order.PlaceOrderService;
import org.hiforce.lattice.test.scenario.order.param.PlaceOrderReqDTO;
import org.hiforce.lattice.test.scenario.order.param.PlaceOrderRespDTO;
import org.hiforce.lattice.test.scenario.order.result.PlaceOrderRequest;
import org.hiforce.lattice.test.scenario.order.result.ShoppingResult;

/**
 * @author Rocky Yu
 * @since 2022/9/21
 */
public class PlaceOrderRemoteService implements PlaceOrderService {

    @Override
    public PlaceOrderRespDTO createOrder(PlaceOrderReqDTO reqDTO) {
        //1. build OrderLine business object.
        OrderLine orderLine = new OrderLine();
        orderLine.setBizCode("business.a");
        orderLine.setScenario(reqDTO.getScenario());
        orderLine.setItemId(reqDTO.getItemId());
        orderLine.setBuyerId(reqDTO.getBuyerId());
        orderLine.setBuyQuantity(reqDTO.getBuyQuantity());

        try {
            ShoppingResult result = new BizSessionScope<ShoppingResult, OrderLine>(orderLine) {
                @Override
                @SuppressWarnings("all")
                protected ShoppingResult execute() throws LatticeRuntimeException {
                    ShoppingResult shoppingResult = new ShoppingResult();
                    //bla.bla.bla

                    //invoke a extension point.
                    SampleAbility ability = new SampleAbility(orderLine);
                    String value = ability.invokeTheSampleSampleExtensionPoint_01();
                    return shoppingResult;
                }

                @Override
                @SuppressWarnings("all")
                public PlaceOrderRequest buildScenarioRequest(OrderLine bizObject) {
                    PlaceOrderRequest request = new PlaceOrderRequest(bizObject);
                    //add some other info.
                    return request;
                }
            }.invoke();
            //build the resp with result...bla bla bla..
            return PlaceOrderRespDTO.success();
        } catch (Throwable ex) {
            ex.printStackTrace();
            return PlaceOrderRespDTO.failed("ERR_SOME_CODE", "Create Order Failed.");
        }
    }
}

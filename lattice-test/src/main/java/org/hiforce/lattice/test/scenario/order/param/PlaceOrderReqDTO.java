package org.hiforce.lattice.test.scenario.order.param;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * @author Rocky Yu
 * @since 2022/9/21
 */
public class PlaceOrderReqDTO implements Serializable {

    private static final long serialVersionUID = 4639100161707989256L;

    @Getter
    @Setter
    private long buyerId;

    @Getter
    @Setter
    private long itemId;

    @Getter
    @Setter
    private int buyQuantity = 1;
}

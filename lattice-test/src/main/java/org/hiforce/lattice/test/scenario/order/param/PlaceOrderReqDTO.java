package org.hiforce.lattice.test.scenario.order.param;

import com.google.common.collect.Maps;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Map;

/**
 * @author Rocky Yu
 * @since 2022/9/21
 */
public class PlaceOrderReqDTO implements Serializable {

    private static final long serialVersionUID = 4639100161707989256L;

    @Getter
    @Setter
    private String buyerId;

    @Getter
    @Setter
    private long itemId;

    @Getter
    @Setter
    private int buyQuantity = 1;

    @Getter
    @Setter
    private String scenario;

    @Getter
    private final Map<String, String> extraParams = Maps.newHashMap();
}

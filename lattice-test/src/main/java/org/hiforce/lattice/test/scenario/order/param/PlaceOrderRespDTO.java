package org.hiforce.lattice.test.scenario.order.param;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * @author Rocky Yu
 * @since 2022/9/21
 */
public class PlaceOrderRespDTO implements Serializable {

    private static final long serialVersionUID = 6283089467213431087L;

    @Getter
    @Setter
    private boolean success;

    @Getter
    @Setter
    private String errCode;

    @Getter
    @Setter
    private String errText;

    @Getter @Setter
    private String result;

    public static PlaceOrderRespDTO success() {
        PlaceOrderRespDTO respDTO = new PlaceOrderRespDTO();
        respDTO.setSuccess(true);
        return respDTO;
    }

    public static PlaceOrderRespDTO failed(String errCode, String errText) {
        PlaceOrderRespDTO respDTO = new PlaceOrderRespDTO();
        respDTO.setSuccess(false);
        respDTO.setErrCode(errCode);
        respDTO.setErrText(errText);
        return respDTO;
    }
}

package org.hiforce.lattice.remote.client;

import java.io.Serializable;

/**
 * @author Rocky Yu
 * @since 2022/9/30
 */
public interface LatticeRemoteInvoker {

    Serializable invoke(String bizCode, String scenario, String extCode, Serializable... params);
}

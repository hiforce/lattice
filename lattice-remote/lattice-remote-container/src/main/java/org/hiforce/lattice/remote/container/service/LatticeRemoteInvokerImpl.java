package org.hiforce.lattice.remote.container.service;

import org.hiforce.lattice.remote.client.LatticeRemoteInvoker;

import java.io.Serializable;

/**
 * @author Rocky Yu
 * @since 2022/9/30
 */
public class LatticeRemoteInvokerImpl implements LatticeRemoteInvoker {

    @Override
    public Serializable invoke(String bizCode, String scenario, String extCode, Serializable... params) {
        return null;
    }
}

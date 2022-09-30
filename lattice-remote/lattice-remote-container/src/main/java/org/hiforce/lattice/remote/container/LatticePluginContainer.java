package org.hiforce.lattice.remote.container;

import org.hiforce.lattice.runtime.Lattice;

/**
 * @author Rocky Yu
 * @since 2022/9/30
 */
public class LatticePluginContainer {

    private static LatticePluginContainer instance;

    private LatticePluginContainer() {

    }

    public static LatticePluginContainer getInstance() {
        if (null == instance) {
            instance = new LatticePluginContainer();
        }
        return instance;
    }

    public void start() {
        Lattice.getInstance().start();
        //register the dubbo service

    }
}

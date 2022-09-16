package org.hifforce.lattice.exception;


import org.hifforce.lattice.message.Message;

/**
 * @author Rocky Yu
 * @since 2022/9/16
 */
public interface ILatticeException {

    Message getErrorMessage();
}

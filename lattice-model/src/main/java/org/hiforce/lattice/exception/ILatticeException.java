package org.hiforce.lattice.exception;


import org.hiforce.lattice.message.Message;

/**
 * @author Rocky Yu
 * @since 2022/9/16
 */
public interface ILatticeException {

    Message getErrorMessage();
}

package org.hiforce.lattice.exception;

import org.apache.commons.lang3.StringUtils;
import org.hiforce.lattice.message.Message;
import org.hiforce.lattice.message.MessageCode;
import org.jetbrains.annotations.PropertyKey;

/**
 * @author Rocky Yu
 * @since 2022/9/16
 */
public class LatticeRuntimeException extends RuntimeException implements ILatticeException {

    private static final long serialVersionUID = -813481135380936090L;

    private Message realMessage = null;

    /**
     * LatticeRuntimeException.
     *
     * @param th throwable exception.
     */
    public LatticeRuntimeException(Throwable th) {
        super(th);
        if (th instanceof ILatticeException) {
            Message errorMessage = ((ILatticeException) th).getErrorMessage();
            if (null != errorMessage) {
                realMessage = Message.of(errorMessage.getCode(), errorMessage.getText());
            }
        }
        if (null == realMessage) {
            realMessage = Message.defaultError();
        }
    }

    public LatticeRuntimeException(@PropertyKey(resourceBundle = MessageCode.BUNDLE) String key,
                                   Object... params) {
        this.realMessage = Message.code(key, params);
    }

    public LatticeRuntimeException(Message message) {
        this.realMessage = message;
    }

    public LatticeRuntimeException(Throwable th, Message message) {
        super(th);
        this.realMessage = message;
    }

    public Message getErrorMessage() {
        if (null != realMessage)
            return realMessage;
        return Message.defaultError();
    }

    @Override
    public String getMessage() {
        Message message = this.getErrorMessage();
        if (null != message) {
            String error = message.toString();
            if (null != error)
                return error;
        }
        String error = super.getMessage();
        if (StringUtils.isNotEmpty(error)) {
            return error;
        }
        return Message.defaultError().toString();
    }
}

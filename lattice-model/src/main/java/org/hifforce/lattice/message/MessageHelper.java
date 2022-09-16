package org.hifforce.lattice.message;

/**
 * @author Rocky Yu
 * @since 2022/9/16
 */
public class MessageHelper {

    private static final Message message = Message.code("LATTICE-CORE-000");

    static Message defaultOne() {
        return message;
    }

    public static void setI18n(String i18nCode) {
        MessageCode.setI18n(i18nCode);
        Message.cachedMessages.clear();
    }

}

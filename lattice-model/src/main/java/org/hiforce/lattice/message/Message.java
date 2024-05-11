package org.hiforce.lattice.message;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.PropertyKey;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Rocky Yu
 * @since 2022/9/16
 */
@NoArgsConstructor
public class Message implements Serializable {


    /**
     * 对于没有参数的错误码 cache 起来
     */
    public final static Map<String, Message> cachedMessages = new ConcurrentHashMap<String, Message>(500);

    private static final long serialVersionUID = 19894637804946296L;
    /**
     * the error code.
     */
    @Getter
    private String code = null;

    /**
     * display error code
     */
    @Getter
    private String readableCode = null;

    /**
     * the error message.
     */
    @Getter
    private String text = null;

    /**
     * the error message which will be displayed.
     */
    @Getter
    private String displayText = null;

    /**
     * 错误所属的Group
     */
    @Getter
    @Setter
    private String group;
    /**
     * the error content create the error.
     */
    @Getter
    @Setter
    private Map<String, Object> contents = new HashMap<String, Object>();

    private Message(String code,
                    String message,
                    String displayMessage,
                    String readableCode) {
        this.code = code;
        this.text = message;
        this.displayText = displayMessage;
        this.readableCode = readableCode;
    }

    public static Message defaultError() {
        return MessageHelper.defaultOne();
    }

    /**
     * @param errorCode the code.
     * @param message   the message.
     * @return
     */
    public static Message of(String errorCode, String message) {
        return of(errorCode, message, message);
    }

    /**
     * 这个构造函数会从当前模块的resources/i18n/infos.properties文件
     * 中读取相关的错误码和错误文案，并且可以通过intellij的代码完成功能展示出相关的错误码。
     *
     * @param key
     * @param params
     * @return
     */
    public static Message code(@PropertyKey(resourceBundle = MessageCode.BUNDLE) String key,
                               Object... params) {

        return MessageCode.toErrorMessage(key, params);
    }

    /**
     * @param code           the code.
     * @param message        the message.
     * @param displayMessage the front display message.
     * @return
     */
    public static Message of(String code,
                             String message,
                             String displayMessage) {
        return of(code, message, displayMessage, "");
    }


    /**
     * @param code        the code.
     * @param text        the message.
     * @param displayText the front display message.
     * @return
     */
    public static Message of(String code,
                             String text,
                             String displayText,
                             String readableCode) {
        Message m = new Message();
        m.code = code;
        m.text = text;
        m.displayText = displayText;
        m.readableCode = readableCode;
        return m;
    }


    @SuppressWarnings("unused")
    public Message addErrorContents(Map<String, Object> errorContents) {
        if (MapUtils.isNotEmpty(errorContents)) {
            contents.putAll(errorContents);
        }
        return this;
    }

    /**
     * toSting 值显示核心内容
     *
     * @return
     */
    @Override
    public String toString() {
        String message = "ErrorMessage{" +
                "c='" + code + '\'' +
                ", rC='" + readableCode + '\'' +
                ", m='" + text + '\'';
        String content = contentToString();
        if (StringUtils.isNotBlank(content)) {
            message = message +
                    " ct=' " + content + '\'';
        }

        return message + "}";

    }


    public String getFullText() {
        return "Message{" +
                "code='" + code + '\'' +
                ", text='" + text + '\'' +
                ", displayText='" + displayText + '\'' +
                ", readableCode='" + readableCode + '\'' +
                ", group='" + group + '\'' +
                ", contents=" + contents +
                '}';
    }

    public String contentToString() {
        if (MapUtils.isEmpty(contents))
            return StringUtils.EMPTY;

        StringBuilder buffer = new StringBuilder(128);
        for (Map.Entry<String, Object> p : contents.entrySet()) {
            if (buffer.length() > 0) {
                buffer.append(", ");
            }
            buffer.append(p.getKey()).append(" => ").append(p.getValue());
        }
        return "{" + buffer.toString() + "}";
    }

    public static void clean() {
        cachedMessages.clear();
    }

}

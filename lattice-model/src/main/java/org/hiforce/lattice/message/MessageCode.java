package org.hiforce.lattice.message;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.hiforce.lattice.exception.LatticeRuntimeException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.PropertyKey;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Rocky Yu
 * @since 2022/9/16
 */
@Slf4j
public class MessageCode {

    public static final String DEFAULT_DISPLAY_ERROR_MESSAGE = "对不起，系统繁忙，请稍候再试";

    public static final String DEFAULT_DISPLAY_ERROR_MESSAGE_EN = "Sorry, the system is busy, please try again later";

    @NotNull
    public static final String BUNDLE = "i18n.infos";

    private static final String DEFAULT_LOG_ERROR_MESSAGE = "ERROR MESSAGE IS MISSING";

    private final static String internalErrorMessageFilePath = "i18n/infos_en_AA.properties";
    private final static String readableErrorCodeFilePath = "i18n/infos_en_XA.properties";

    private static String defaultDisplayLocale = "en";

    private static final Map<String, Object> allDisplayErrorCodes = new ConcurrentHashMap<>();
    private static Map<Object, Object> displayErrorCodes = new ConcurrentHashMap<>();
    private static Map<Object, Object> internalErrorMessage = new ConcurrentHashMap<>();
    private static Map<Object, Object> readableErrorCode = new ConcurrentHashMap<>();
    private static final Map<Object, Object> nonStandardReadableErrorCode = new ConcurrentHashMap<>();

    private static final Map<String, String> cachedLogMessage = new ConcurrentHashMap<>();

    private static final ThreadLocal<String> dynamicI18n = new ThreadLocal<>();

    static {
        init();
    }

    public static void init() {
        internalErrorMessage = extractErrorCodes(internalErrorMessageFilePath);
        readableErrorCode = extractErrorCodes(readableErrorCodeFilePath);
        displayErrorCodes = extractErrorCodes("i18n/infos_" + defaultDisplayLocale + ".properties");
        processReadableErrorCode();
    }

    private static void processReadableErrorCode() {
        nonStandardReadableErrorCode.clear();
        for (Map.Entry<Object, Object> entry : readableErrorCode.entrySet()) {
            String raw = entry.getValue().toString();
            try {
                String encoded = URLEncoder.encode(raw, "UTF-8");
                if (!raw.equals(encoded)) {
                    nonStandardReadableErrorCode.put(entry.getKey(), raw);
                }
            } catch (UnsupportedEncodingException e) {
                throw new LatticeRuntimeException(e);
            }
        }
        for (Map.Entry<Object, Object> entry : nonStandardReadableErrorCode.entrySet()) {
            String raw = entry.getValue().toString();
            try {
                readableErrorCode.put(entry.getKey(), URLEncoder.encode(raw, "UTF-8"));
            } catch (UnsupportedEncodingException e) {
                throw new LatticeRuntimeException(e);
            }
        }
    }

    private static String logMessageWithoutCode(@PropertyKey(resourceBundle = BUNDLE)
                                                @NotNull String key, Object... params) {
        boolean canNotCache = params != null && params.length > 0;
        if (!canNotCache) {
            String result = cachedLogMessage.get(key);
            if (result != null) {
                return result;
            }
        }
        String logMessage = "log message cannot be retrieved properly";
        try {
            logMessage = searchKeyInAllResourceFile(internalErrorMessage, key, DEFAULT_LOG_ERROR_MESSAGE, params);
        } catch (Exception e) {
            log.error(logMessage, e);
        }
        String result = logMessage;
        if (!canNotCache && StringUtils.isNotEmpty(key) && StringUtils.isNotEmpty(result)) {
            cachedLogMessage.put(key, result);
        }
        return result;
    }

    @SuppressWarnings("all")
    public static String displayMessage(Locale locale, @PropertyKey(resourceBundle = BUNDLE) String key, Object... params) {
        Map<Object, Object> props = new HashMap<>();

        String i18nCode = null == locale ? Locale.CHINA.toString() : locale.toString();
        if (allDisplayErrorCodes.containsKey(i18nCode)) {
            props = (Map<Object, Object>) allDisplayErrorCodes.get(i18nCode);
        } else {
            props = extractContextErrorCodes("i18n/infos_" + i18nCode + ".properties", false, DEFAULT_DISPLAY_ERROR_MESSAGE);
            allDisplayErrorCodes.put(i18nCode, props);
        }
        String value = searchKeyInAllResourceFile(props, key, params);
        if (StringUtils.isEmpty(value)) {
            value = displayMessage(Locale.ENGLISH, key, params);
        }
        if (StringUtils.isNotEmpty(value)) {
            return value;
        }
        return displayMessage(key, params);
    }

    @SuppressWarnings("unchecked")
    public static String displayMessage(@PropertyKey(resourceBundle = BUNDLE) String key, Object... params) {
        Map<Object, Object> props = new HashMap<>();
        String i18nCode = dynamicI18n.get();
        if (StringUtils.isEmpty(i18nCode)) {
            props = displayErrorCodes;
        } else {
            if (allDisplayErrorCodes.containsKey(i18nCode)) {
                props = (Map<Object, Object>) allDisplayErrorCodes.get(i18nCode);
            } else {
                props = extractContextErrorCodes("i18n/infos_" + i18nCode + ".properties", false, DEFAULT_DISPLAY_ERROR_MESSAGE);
                allDisplayErrorCodes.put(i18nCode, props);
            }
        }
        return searchKeyInAllResourceFile(props, key, DEFAULT_DISPLAY_ERROR_MESSAGE, params);
    }


    private static String searchKeyInAllResourceFile(Map<Object, Object> props,
                                                     String key,
                                                     Object... params) {
        if (!props.containsKey(key)) return null;

        Object obj = props.get(key);
        String message = buildMessage(obj, params);
        return StringUtils.isNotBlank(message) ? message : null;
    }


    private static String buildMessage(Object obj, Object[] params) {
        String message = String.valueOf(obj);

        if (params != null && params.length > 0 && message != null && message.indexOf('{') >= 0) {
            message = MessageFormat.format(message, params);
        }
        if (params != null && params.length > 0 && message != null && message.contains("{0}")) {
            message = MessageFormat.format(message, params);
        }

        return message;
    }

    private static Map<Object, Object> extractErrorCodes(String resourceFilePath) {
        return extractErrorCodes(resourceFilePath, false, null);
    }

    private static Map<Object, Object> extractErrorCodes(String resourceFilePath,
                                                         boolean replaceEnglishWords,
                                                         String replaceText) {

        return new HashMap<>(extractErrorCodes(Thread.currentThread().getContextClassLoader(),
                resourceFilePath,
                replaceEnglishWords,
                replaceText));
    }

    public static Map<Object, Object> extractErrorCodes(ClassLoader classLoader,
                                                        String resourceFilePath,
                                                        boolean replaceEnglishWords,
                                                        String replaceText) {
        Map<Object, Object> props = new HashMap<Object, Object>();
        try {
            Enumeration<URL> resources = classLoader.getResources(resourceFilePath);
            while (resources.hasMoreElements()) {
                URL url = resources.nextElement();
                InputStream in = url.openStream();
                Properties prop = new Properties();
                prop.load(new InputStreamReader(in, StandardCharsets.UTF_8));
                if (replaceEnglishWords) {
                    for (Map.Entry<Object, Object> entry : prop.entrySet()) {
                        //如果全是英文,则替换为默认文案
                        if (!hasChineseCharacter(String.valueOf(entry.getValue()))) {
                            entry.setValue(replaceText);
                        }
                    }
                }
                props.putAll(prop);
            }
        } catch (IOException e) {
            log.error("The target resource [{}] is not available ... ", resourceFilePath);
            return Collections.emptyMap();
        }
        return props;
    }

    public static boolean hasChineseCharacter(String chineseStr) {
        char[] charArray = chineseStr.toCharArray();
        for (int i = 0; i < charArray.length; i++) {
            if ((charArray[i] >= 0x4e00) && (charArray[i] <= 0x9fbb)) {
                return true;
            }
        }
        return false;
    }

    private static Map<Object, Object> extractContextErrorCodes(String resourceFilePath,
                                                                boolean replaceEnglishWords,
                                                                String replaceText) {
        return new HashMap<>(extractErrorCodes(Thread.currentThread().getContextClassLoader(),
                resourceFilePath,
                replaceEnglishWords,
                replaceText));
    }

    static Message toErrorMessage(@PropertyKey(resourceBundle = BUNDLE) String key, Object... params) {
        String logMessage = logMessageWithoutCode(key, params);
        String displayMessage = displayMessage(key, params);
        String readableCode = searchKeyInAllResourceFile(readableErrorCode, key, key);
        Message message = Message.of(key, logMessage, displayMessage, readableCode);
        message.setMessageKey(key);
        message.setMessageParams(params != null ? params.clone() : null);
        return message;
    }

    public static void setI18n(String i18nCode) {
        if (allDisplayErrorCodes.containsKey(i18nCode)) {
            displayErrorCodes = (Map<Object, Object>) allDisplayErrorCodes.get(i18nCode);
        } else {
            displayErrorCodes = extractContextErrorCodes("i18n/infos_" + i18nCode + ".properties", false, DEFAULT_DISPLAY_ERROR_MESSAGE);
            allDisplayErrorCodes.put(i18nCode, displayErrorCodes);
        }
    }

    /**
     * Set the default display locale used by getDisplayText() (no-arg version).
     * Must be called before the first Message.code() invocation, or call init() afterwards.
     *
     * @param localeCode the locale code matching the resource file suffix, e.g. "en", "zh_CN", "ja"
     */
    public static void setDefaultDisplayLocale(String localeCode) {
        defaultDisplayLocale = localeCode;
        displayErrorCodes = extractContextErrorCodes(
                "i18n/infos_" + localeCode + ".properties", false, DEFAULT_DISPLAY_ERROR_MESSAGE);
        allDisplayErrorCodes.put(localeCode, displayErrorCodes);
    }

    public static String getDefaultDisplayLocale() {
        return defaultDisplayLocale;
    }

    public static void setDynamicI18n(String i18nCode) {
        dynamicI18n.set(i18nCode);
    }
}

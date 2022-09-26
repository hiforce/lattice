package org.hiforce.lattice.tool;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * @author Rocky Yu
 * @since 2022/9/26
 */
public class JacksonUtils {
    private static final Logger logger = LoggerFactory.getLogger(JacksonUtils.class);

    /**
     * 精简输出Mapper
     */
    public static final ObjectMapper objectMapper = new ObjectMapper();

    static {
        objectMapper.registerModule(new JavaTimeModule());

    }

    private static void attachSimplify(ObjectMapper attached) {
        attached.setSerializationInclusion(JsonInclude.Include.NON_NULL);

        attached.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        attached.configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true);
        attached.configure(SerializationFeature.WRITE_EMPTY_JSON_ARRAYS, false);
    }

    static {
        objectMapper.setVisibility(
                objectMapper.getSerializationConfig().
                        getDefaultVisibilityChecker().
                        withFieldVisibility(JsonAutoDetect.Visibility.ANY).
                        withGetterVisibility(JsonAutoDetect.Visibility.NONE).
                        withIsGetterVisibility(JsonAutoDetect.Visibility.NONE)
        );
        /**
         * 精简输出！
         */
        attachSimplify(objectMapper);
    }


    /**
     * 异常必须抛给上层处理，否则会出现很多坑爹问题
     */
    public static String serialize(Object serialized) throws Exception {
        if (serialized == null) {
            return null;
        }
        return objectMapper.writeValueAsString(serialized);

    }

    public static String serializeWithoutException(Object serialized) {
        if (serialized == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(serialized);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static <T> T deserialize(String jsonStr, Class<T> transferClass) throws Exception {
        if (StringUtils.isBlank(jsonStr) || transferClass == null) {
            return null;
        }
        return objectMapper.readValue(jsonStr, transferClass);
    }

    public static <T> T deserialize(String json, Class<?> collectionClass, Class<?>... elementClasses) {
        ObjectMapper objectMapper = new ObjectMapper();
        attachSimplify(objectMapper);

        JavaType javaType = objectMapper.getTypeFactory().constructParametricType(collectionClass, elementClasses);
        try {
            return objectMapper.readValue(json, javaType);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            return null;
        }
    }


    public static <T> T deserializeIgnoreException(String jsonStr, Class<T> transferClass) {
        try {
            if (StringUtils.isBlank(jsonStr) || transferClass == null) {
                return null;
            }
            return objectMapper.readValue(jsonStr, transferClass);
        } catch (Exception e) {
            logger.error(" deserializeIgnoreException use JacksonUtils error , jsonStr is : " + jsonStr, e);
        }
        return null;
    }

}

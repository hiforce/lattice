package org.hiforce.lattice.utils;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Sets;
import com.google.common.collect.Table;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.hiforce.lattice.annotation.model.ExtensionAnnotation;
import org.hiforce.lattice.model.ability.IBusinessExt;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Rocky Yu
 * @since 2022/9/18
 */
@Slf4j
public class BusinessExtUtils {

    private static final Map<Class<?>, Set<String>> CODE_MAP = new ConcurrentHashMap<>();

    private static final Table<Class<?>, String, Method> EXT_METHOD_MAP = HashBasedTable.create();

    public static Method getExtensionMethod(IBusinessExt businessExt, String extCode, String scenario) {
        if (null == businessExt) {
            return null;
        }
        if (!supportedExtCodes(businessExt).contains(extCode)) {
            return null;
        }

        IBusinessExt subExt = businessExt.getBusinessExtByCode(extCode, scenario);
        return EXT_METHOD_MAP.get(subExt.getClass(), extCode);
    }


    public static Set<String> supportedExtCodes(IBusinessExt businessExt) {
        if (null == businessExt) {
            return Sets.newHashSet();
        }
        Class<?> key = businessExt.getClass();
        Set<String> result = CODE_MAP.get(key);
        if (CollectionUtils.isNotEmpty(result)) {
            return result;
        }

        Set<String> supportedCodes = Sets.newConcurrentHashSet();
        supportedCodes.addAll(distinctSupportCodes(businessExt));
        try {
            for (Method method : businessExt.getClass().getMethods()) {
                ExtensionAnnotation annotation = LatticeAnnotationUtils.getExtensionAnnotation(method);
                if (null == annotation) {
                    continue;
                }
                if (StringUtils.isNotEmpty(annotation.getCode())) {
                    EXT_METHOD_MAP.put(key, annotation.getCode(), method);
                    supportedCodes.add(annotation.getCode());
                }

            }
        } catch (Throwable th) {
            log.warn(th.getMessage(), th);
        }
        if (null == CODE_MAP.get(key)) {
            CODE_MAP.put(key, supportedCodes);
        } else {
            CODE_MAP.get(key).addAll(supportedCodes);
        }

        return supportedCodes;
    }

    private static Set<String> distinctSupportCodes(IBusinessExt businessExt) {
        Set<String> codes = Sets.newConcurrentHashSet();
        if (null == businessExt) {
            return Sets.newHashSet();
        }
        List<? extends IBusinessExt> subBusinessLists = businessExt.getAllSubBusinessExt();
        for (IBusinessExt subBusinessExt : subBusinessLists) {
            if (null == subBusinessExt)
                continue;
            codes.addAll(supportedExtCodes(subBusinessExt));
        }
        return codes;
    }
}

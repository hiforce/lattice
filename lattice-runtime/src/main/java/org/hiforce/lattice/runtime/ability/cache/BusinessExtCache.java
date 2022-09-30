package org.hiforce.lattice.runtime.ability.cache;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Lists;
import com.google.common.collect.Table;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.StringUtils;
import org.hifforce.lattice.annotation.model.ScanSkipAnnotation;
import org.hifforce.lattice.model.ability.IBusinessExt;
import org.hifforce.lattice.model.ability.cache.IBusinessExtCache;
import org.hifforce.lattice.utils.BusinessExtUtils;
import org.hiforce.lattice.runtime.utils.LatticeBeanUtils;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Objects;

import static org.hifforce.lattice.utils.LatticeAnnotationUtils.getScanSkipAnnotation;


/**
 * @author Rocky Yu
 * @since 2022/9/16
 */
@Slf4j
public class BusinessExtCache implements IBusinessExtCache {

    private static BusinessExtCache instance;

    private static final Object lock = new Object();

    private static final Table<Class<?>, ExtKey, IBusinessExt> BIZ_EXT_TABLE = HashBasedTable.create();

    private BusinessExtCache() {

    }

    public static BusinessExtCache getInstance() {
        if (null == instance) {
            synchronized (lock) {
                if (null == instance) {
                    instance = new BusinessExtCache();
                }
            }
        }
        return instance;
    }

    @SuppressWarnings("SynchronizationOnGetClass")
    public IBusinessExt getCachedBusinessExt(IBusinessExt businessExt, String extCode, String scenario) {
        scenario = StringUtils.isEmpty(scenario) ? "None#" : scenario;
        ExtKey extKey = new ExtKey(scenario, extCode);
        IBusinessExt found = BIZ_EXT_TABLE.get(businessExt.getClass(), extKey);
        if (null != found) {
            return found;
        }
        synchronized (businessExt.getClass()) {
            IBusinessExt point = BIZ_EXT_TABLE.get(businessExt.getClass(), extKey);
            if (point != null) {
                return point;
            }
            point = findSubBusinessExtViaExtCode(businessExt, extCode);
            if (point != null) {
                LatticeBeanUtils.autowireBean(point);
                BIZ_EXT_TABLE.put(businessExt.getClass(), extKey, point);
            }
            return point;
        }
    }

    @Override
    public List<IBusinessExt> getAllSubBusinessExt(IBusinessExt businessExt) {
        List<IBusinessExt> children = Lists.newArrayList();
        try {
            for (Method method : businessExt.getClass().getMethods()) {
                if (ClassUtils.isAssignable(method.getReturnType(), IBusinessExt.class)) {
                    ScanSkipAnnotation scanSkip = getScanSkipAnnotation(method);
                    if (null != scanSkip) {
                        continue;
                    }
                    try {
                        method.setAccessible(true);
                        IBusinessExt subBusinessExt = (IBusinessExt) method.invoke(businessExt);
                        if (null == subBusinessExt)
                            continue;

                        boolean b = true;
                        for (IBusinessExt p : children) {
                            if (isSubBusinessClassMatched(p, subBusinessExt)) {
                                b = false;
                                break;
                            }
                        }
                        if (b) {
                            children.add(subBusinessExt);
                        }
                    } catch (Throwable th) {
                        log.warn(th.getMessage(), th);
                    }
                }
            }
        } catch (Throwable th) {
            log.warn(th.getMessage(), th);
        }
        return children;
    }

    private boolean isSubBusinessClassMatched(IBusinessExt child, IBusinessExt businessExt) {
        if (null == child || null == businessExt) {
            return false;
        }
        return child.getClass().equals(businessExt.getClass());
    }

    private IBusinessExt findSubBusinessExtViaExtCode(IBusinessExt businessExt, String extCode) {
        if (StringUtils.isEmpty(extCode)) {
            return null;
        }
        boolean isMatch = BusinessExtUtils.supportedExtCodes(businessExt).contains(extCode);
        if (!isMatch) {
            return null;
        }

        //递归看是否有子Facade
        for (IBusinessExt facade : businessExt.getAllSubBusinessExt()) {
            IBusinessExt extension = findSubBusinessExtViaExtCode(facade, extCode);
            if (null == extension) {
                continue;
            }
            return extension;
        }
        return businessExt;
    }


    static class ExtKey {
        private final String scenario;
        private final String extCode;

        public ExtKey(String scenario, String extCode) {
            this.scenario = scenario;
            this.extCode = extCode;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            ExtKey key = (ExtKey) o;

            if (StringUtils.isNotEmpty(scenario) ?
                    !StringUtils.equals(scenario, key.scenario) : StringUtils.isNotEmpty(key.scenario)) {
                return false;
            }
            return Objects.equals(extCode, key.extCode);
        }

        @Override
        public int hashCode() {
            int result = scenario != null ? scenario.hashCode() : 0;
            result = 31 * result + (extCode != null ? extCode.hashCode() : 0);
            return result;
        }
    }
}

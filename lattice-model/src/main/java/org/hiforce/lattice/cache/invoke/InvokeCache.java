package org.hiforce.lattice.cache.invoke;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.hiforce.lattice.exception.LatticeRuntimeException;
import org.hiforce.lattice.message.Message;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Collections.emptyList;

/**
 * @author Rocky Yu
 * @since 2022/9/20
 */
@Slf4j
public final class InvokeCache {
    private static final Null NULL = new Null();

    private static final InvokeCacheThreadLocal INSTANCE = new InvokeCacheThreadLocal();

    private static final ThreadLocal<Boolean> INIT = new ThreadLocal<>();

    private final Map<Class<?>, Map<Object, Object>> cache = Maps.newHashMap();
    private final boolean needHoldRemoteCache = false;


    public static void main(String[] args) {
        System.out.println(InvokeCache.isThreadLocalInit());
        InvokeCache.instance().put(String.class, "rocky", "yu");
        InvokeCache.instance();
        System.out.println(InvokeCache.isThreadLocalInit());
        InvokeCache.initInvokeCache();
        System.out.println(InvokeCache.isThreadLocalInit());
    }

    InvokeCache() {
    }

    public static void initInvokeCache() {
        INIT.set(true);
        InvokeCache.INSTANCE.get();
    }

    public static boolean isThreadLocalInit() {
        Boolean initialized = INIT.get();
        return (null != initialized && initialized);
    }

    public static InvokeCache instance() {
        if (InvokeCache.isThreadLocalInit()) {
            return INSTANCE.get();
        }
        return new InvokeCache();
    }

    public static void forceClear() {
        INSTANCE.remove();
        INIT.remove();
    }

    /**
     * 将要缓存的对象放入缓存中。
     *
     * @param klass    要缓存对象的类型
     * @param id       要缓存对象的 ID
     * @param instance 要缓存对象的实例
     * @param <T>      要缓存对象的类型
     */
    public <T> void put(Class<? super T> klass, Object id, @Nullable T instance) {
        if (!isThreadLocalInit()) {
            return;
        }
        if (instance != null)
            checkArgument(klass.isInstance(instance), "incompatible class and instance");

        @SuppressWarnings({"MismatchedQueryAndUpdateOfCollection"})
        Map<Object, Object> idToInstanceCache = getIdToInstanceCache(klass);

        if (instance != null) {
            idToInstanceCache.put(id, instance);
        } else {
            idToInstanceCache.put(id, NULL);
        }
        if (idToInstanceCache.size() > 1000) {
            log.warn("RemoteCache: idToInstanceCache too large! size = " + idToInstanceCache.size() + ", class = " + klass);
        }
    }

    /**
     * 将对象批量放入缓存。批量放入的这些实例后续可以通过 ID 单独获取出来。所以，
     * {@code ids} 与 {@code instances} 的值必须是一一对应的。
     *
     * @param klass     要缓存对象的类型
     * @param ids       要缓存对象的 ID 列表
     * @param instances 与 ID 列表对应的实例列表，注意要保序
     * @param <T>       要缓存对象的类型
     */
    public <T> void batchPut(Class<? super T> klass, List<?> ids, List<? extends T> instances) {
        if (!isThreadLocalInit())
            return;

        checkArgument(ids.size() == instances.size(), "incompatible ids and instances");

        Iterator<?> idsItr = ids.iterator();
        Iterator<? extends T> instancesItr = instances.iterator();

        while (idsItr.hasNext()) {
            Object id = idsItr.next();
            T instance = instancesItr.next();
            if (instance != null)
                checkArgument(klass.isInstance(instance), "incompatible class and instance");

            put(klass, id, instance);
        }
    }

    /**
     * 以指定的类型和 ID 获取缓存中的对象。如果没有，则返回 {@code null}。
     *
     * @param klass 被缓存对象的类型（必须与 put 时一致，否则拿不到）
     * @param id    被缓存对象的 ID
     * @param <T>   被缓存对象的类型
     */
    @Nullable
    public <T> T get(Class<T> klass, Object id) {
        return get(klass, id, null);
    }

    /**
     * 以指定的类型和 ID 获取缓存中的对象。如果没有，则返回 {@code callbackOnMiss}
     * 的调用结果。此调用结果会被自动缓存起来。后续的 {@code get/batchGet} 调用
     * 都会从缓存中取结果。
     *
     * @param klass          被缓存对象的类型（必须与 put 时一致，否则拿不到）
     * @param id             被缓存对象的 ID
     * @param callbackOnMiss 如果缓存没有命中时的回调
     * @param <T>            被缓存对象的类型
     */
    @Nullable
    public <T> T get(Class<T> klass, Object id, @Nullable Callable<? extends T> callbackOnMiss) {
        Object ret = get0(klass, id, callbackOnMiss);
        if (ret == NULL) {
            return null;
        }
        //noinspection unchecked
        return (T) ret;
    }

    @Nullable
    private <T> Object get0(Class<T> klass, Object id, @Nullable Callable<? extends T> callbackOnMiss) {
        if (!isThreadLocalInit()) {
            if (callbackOnMiss != null) {
                try {
                    T callbackRet = callbackOnMiss.call();
                    put(klass, id, callbackRet);
                    return callbackRet;
                } catch (Exception ex) {
                    handleCallException(klass, Lists.newArrayList(id), ex);
                }
            }
            return null;
        }
        Map<Object, Object> idToInstanceCache = cache.get(klass);
        Object ret = idToInstanceCache == null ? null : idToInstanceCache.get(id);

        if (ret == NULL) {
            return NULL;
        }
        if (ret != null) {
            return ret;
        }

        if (callbackOnMiss != null) {
            try {
                T callbackRet = callbackOnMiss.call();
                put(klass, id, callbackRet);
                return callbackRet;
            } catch (Exception ex) {
                handleCallException(klass, Lists.newArrayList(id), ex);
            }
        }

        return null;
    }

    /**
     * 以指定的类型和 ID 列表批量获取被缓存对象。如果缓存中不存在则返回 {@code null}。
     * 返回的被缓存对象列表与 {@code ids} 保持顺序一致。
     *
     * @param klass 被缓存对象的类型（必须与 put 时一致，否则拿不到）
     * @param ids   被缓存对象的 ID 列表
     * @param <T>   被缓存对象的类型
     */
    @Nonnull
    @SuppressWarnings("unused")
    public <T> List<T> batchGet(Class<T> klass, List<?> ids) {
        return batchGet(klass, ids, null);
    }

    /**
     * 以指定的类型和 ID 列表批量获取被缓存对象。返回的被缓存对象列表与 {@code ids}
     * 保持顺序一致。如果缓存中不存在则调用 {@code callbackOnMiss}，并且返回此回
     * 调的返回值。
     *
     * @param klass          被缓存对象的类型
     * @param ids            被缓存对象的 ID 列表
     * @param callbackOnMiss 缓存没有命中时的回调
     * @param <T>            被缓存对象的类型
     */
    @Nonnull
    public <T> List<T> batchGet(Class<T> klass,
                                List<?> ids,
                                @Nullable Callable<List<T>> callbackOnMiss) {
        if (!isThreadLocalInit()) {
            try {
                if (null != callbackOnMiss) {
                    return callbackOnMiss.call();
                }
                return Collections.emptyList();
            } catch (Exception ex) {
                handleCallException(klass, ids, ex);
            }
        }

        List<T> cachedInstances = batchGet0(klass, ids);
        if (cachedInstances.size() > ids.size()) {
            throw new IllegalStateException(format("incorrect cache size. expected %s, but got %s",
                    ids.size(),
                    cachedInstances.size()));
        }
        if (cachedInstances.size() == ids.size()) {
            return cachedInstances;
        }

        if (callbackOnMiss != null) {
            try {
                List<T> ret = callbackOnMiss.call();
                batchPut(klass, ids, ret);
                return ret;
            } catch (Exception ex) {
                handleCallException(klass, ids, ex);
            }
        }

        return Collections.emptyList();
    }

    private void handleCallException(Class<?> klass, List<?> ids, Exception ex) {
        if (ex instanceof LatticeRuntimeException) {
            Message errorMessage = ((LatticeRuntimeException) ex).getErrorMessage();
            if (null != errorMessage) {
                throw new LatticeRuntimeException(Message.of(errorMessage.getCode(), errorMessage.getText()));
            }
        }
        throw new LatticeRuntimeException("LATTICE-CORE-001", klass.getName(),
                ids.stream().map(Object::toString).collect(Collectors.joining(",")), ex.getMessage());
    }

    @Nonnull
    <T> List<T> batchGet0(@Nonnull Class<T> klass, @Nonnull List<?> ids) {
        if (!isThreadLocalInit()) {
            return Collections.emptyList();
        }
        Iterator<?> idsItr = ids.iterator();

        List<T> ret = Lists.newArrayListWithCapacity(ids.size());

        while (idsItr.hasNext()) {
            Object id = idsItr.next();

            Object cachedInstance = get0(klass, id, null);
            if (cachedInstance == NULL) {
                ret.add(null);
            } else if (cachedInstance != null) {
                //noinspection unchecked
                ret.add((T) cachedInstance);
            } else {
                return emptyList();
            }
        }

        return ret;
    }

    @Nonnull
    private Map<Object, Object> getIdToInstanceCache(@Nonnull Class<?> klass) {
        return cache.computeIfAbsent(klass, k -> new HashMap<>());
    }

    public void clear() {
        if (!INSTANCE.get().needHoldRemoteCache) {
            INSTANCE.remove();
        }
    }

    /**
     * 清空当前线程内以指定类型缓存的对象。
     */
    public void clear(@Nonnull Class<?> klass) {
        @SuppressWarnings({"MismatchedQueryAndUpdateOfCollection"})
        Map<Object, Object> cacheOfClass = cache.get(klass);
        if (cacheOfClass != null) {
            cache.remove(klass);
        }
    }

    private static class Null {
    }

    public static String format(String template, Object... args) {
        StringBuilder builder = new StringBuilder(template.length() + 16 * args.length);
        int templateStart = 0;

        int i;
        int placeholderStart;
        for (i = 0; i < args.length; templateStart = placeholderStart + 2) {
            placeholderStart = template.indexOf("%s", templateStart);
            if (placeholderStart == -1) {
                break;
            }

            builder.append(template, templateStart, placeholderStart);
            builder.append(args[i++]);
        }

        builder.append(template.substring(templateStart));
        if (i < args.length) {
            builder.append(" [");
            builder.append(args[i++]);

            while (i < args.length) {
                builder.append(", ");
                builder.append(args[i++]);
            }

            builder.append("]");
        }

        return builder.toString();
    }
}

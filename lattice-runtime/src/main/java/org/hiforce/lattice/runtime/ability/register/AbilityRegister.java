package org.hiforce.lattice.runtime.ability.register;

import com.google.common.collect.Sets;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.hifforce.lattice.annotation.model.AbilityAnnotation;
import org.hifforce.lattice.annotation.model.ExtensionAnnotation;
import org.hifforce.lattice.annotation.model.ReduceType;
import org.hifforce.lattice.annotation.parser.AbilityAnnotationParser;
import org.hifforce.lattice.exception.LatticeRuntimeException;
import org.hifforce.lattice.message.Message;
import org.hifforce.lattice.model.ability.IAbility;
import org.hifforce.lattice.model.ability.IBusinessExt;
import org.hifforce.lattice.model.business.IBizObject;
import org.hifforce.lattice.model.register.AbilityInstSpec;
import org.hifforce.lattice.model.register.AbilitySpec;
import org.hifforce.lattice.model.register.BaseSpec;
import org.hifforce.lattice.model.register.ExtensionPointSpec;
import org.hiforce.lattice.runtime.Lattice;
import org.hiforce.lattice.runtime.cache.LatticeRuntimeCache;
import org.hiforce.lattice.runtime.spi.LatticeSpiFactory;
import org.hiforce.lattice.runtime.utils.LatticeClassUtils;
import org.springframework.aop.support.AopUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;
import java.util.stream.Collectors;

import static org.hiforce.lattice.runtime.utils.ExtensionUtils.getExtensionAnnotation;
import static org.hiforce.lattice.runtime.utils.LatticeBeanUtils.getAndCreateSpringBeanViaClass;

/**
 * @author Rocky Yu
 * @since 2022/9/15
 */
@Slf4j
public class AbilityRegister {

    private static final ThreadLocal<ClassLoader> CLASS_LOADER_THREAD_LOCAL = new ThreadLocal<>();

    private static AbilityRegister instance;

    private AbilityRegister() {

    }

    public static AbilityRegister getInstance() {
        if (null == instance) {
            instance = new AbilityRegister();
        }
        return instance;
    }

    public LatticeRuntimeCache getRuntimeCache() {
        return Lattice.getInstance().getLatticeRuntimeCache();
    }

    public List<AbilitySpec> register(AbilityBuildRequest regDTO) {

        List<AbilitySpec> abilitySpecList = new ArrayList<>();
        for (Class<?> currentClass : regDTO.getClassSet()) {
            Pair<AbilityAnnotation, Class<?>> pair = findAbilityAnnotationAndAbilityClass(currentClass);
            if (null == pair) {
                continue;
            }
            AbilityAnnotation ability = pair.getLeft();
            Class<?> targetClass = pair.getRight();
            if (null == ability) {
                continue;
            }
            if (null != regDTO.getParent()) {
                if (!ability.getParent().equals(regDTO.getParent().getCode())) {
                    continue;
                }
            }
            AbilitySpec abilitySpec = getRuntimeCache().doCacheAbilitySpec(ability, targetClass);
            abilitySpecList.add(abilitySpec);
            abilitySpec.addAbilityInstance(scanAbilityInstance(abilitySpec, regDTO.getClassSet()));

        }
        return abilitySpecList;
    }

    @SuppressWarnings("all")
    private Pair<AbilityAnnotation, Class<?>> findAbilityAnnotationAndAbilityClass(Class<?> currentClass) {

        List<AbilityAnnotationParser> parsers = LatticeSpiFactory.getInstance().getAbilityAnnotationParsers();
        for (AbilityAnnotationParser parser : parsers) {
            Annotation annotation = currentClass.getDeclaredAnnotation(parser.getAnnotationClass());
            if (null == annotation) {
                continue;
            }
            AbilityAnnotation annotationInfo = parser.buildAnnotationInfo(annotation, currentClass);
            return Pair.of(annotationInfo, currentClass);
        }
        return null;
    }

    public synchronized List<AbilityInstSpec> scanAbilityInstance(AbilitySpec abilitySpec, Collection<Class> classSet) {

        List<AbilityInstSpec> abilityInstanceSpecList = new ArrayList<>(registerAbilityInstances(abilitySpec, classSet));
        abilityInstanceSpecList.sort(Comparator.comparingInt(AbilityInstSpec::getPriority));
        return abilityInstanceSpecList;
    }

    private List<AbilityInstSpec> registerAbilityInstances(AbilitySpec abilitySpec, Collection<Class> classSet) {
        List<AbilityInstSpec> instanceSpecs = new ArrayList<>();
        for (Class<?> targetClass : classSet) {
            if (Modifier.isAbstract(targetClass.getModifiers())
                    || Modifier.isInterface(targetClass.getModifiers())) {
                continue;
            }
            if (!LatticeClassUtils.isSubClassOf(targetClass, abilitySpec.getAbilityClass())) {
                continue;//不能是自己，同时是ability的子类
            }
            AbilityInstBuildResult result = innerRegisterAbilityInstance(abilitySpec, targetClass);
            if (!result.isSuccess() && !result.isRegistered()) {
                Message message = null == result.getMessage() ? Message.code("LATTICE-CORE-RT-0001", targetClass.getName(), "not clear")
                        : result.getMessage();
                throw new LatticeRuntimeException(message);
            }
            AbilityInstSpec abilityInstanceSpec = result.getInstanceSpec();
            if (null != abilityInstanceSpec)
                instanceSpecs.add(abilityInstanceSpec);
        }

        return instanceSpecs;
    }

    private AbilityInstBuildResult innerRegisterAbilityInstance(AbilitySpec abilitySpec, Class<?> instanceClass) {
        IAbility ability;
        IAbility originAbility;
        Object beanViaClass = getAndCreateSpringBeanViaClass(instanceClass, IBizObject.DUMMY);
        if (beanViaClass instanceof IAbility) {
            ability = (IAbility) beanViaClass;
            if (AopUtils.isAopProxy(ability)) {
                Class<?> originCls = AopUtils.getTargetClass(ability);
                try {
                    Object curObject = getAndCreateSpringBeanViaClass(originCls, IBizObject.DUMMY);
                    originAbility = (IAbility) curObject;
                } catch (Exception e) {
                    return AbilityInstBuildResult.failed(Message.code("LATTICE-CORE-RT-0002", originCls.getName()));
                }
            } else {
                originAbility = ability;
            }
        } else {
            return AbilityInstBuildResult.failed(Message.code("LATTICE-CORE-RT-0002", instanceClass.getName()));
        }
        if (null != Lattice.getInstance().getAbilityProvider().getRealization(originAbility.getInstanceCode())) {
            if (isAbilityInstanceRegistered(abilitySpec, originAbility)) {
                return AbilityInstBuildResult.registered();  //已经注册了，就不重复注册
            }
            return buildAbilityInstanceSpec(abilitySpec, originAbility, instanceClass);
        }
        AbilityInstBuildResult result = buildAbilityInstanceSpec(abilitySpec, originAbility, instanceClass);
        if (result.isSuccess()) {
            AbilityInstSpec abilityInstanceSpec = result.getInstanceSpec();
            if (null != abilityInstanceSpec) {
                Lattice.getInstance().getAbilityProvider().registerRealization(ability, originAbility.getInstanceCode());
            }
        }
        return result;
    }

    private boolean isAbilityInstanceRegistered(AbilitySpec abilitySpec, IAbility instance) {
        return abilitySpec.getAbilityInstSpecMap().values()
                .stream()
                .anyMatch(p -> p.getCode().equals(instance.getInstanceCode()));
    }

    private AbilityInstBuildResult buildAbilityInstanceSpec(AbilitySpec abilitySpec, IAbility instance,
                                                            Class<?> instanceClass) {
        try {
            AbilityInstSpec instanceDesc = new AbilityInstSpec();
            instanceDesc.setInstanceClass(instanceClass.getName());
            instanceDesc.setCode(instance.getInstanceCode());
            instanceDesc.setName(instanceClass.getSimpleName());
            instanceDesc.getExtensions().addAll(scanAbilityExtensions(instance, abilitySpec));
            abilitySpec.addAbilityInstance(instanceDesc);
            return AbilityInstBuildResult.success(instanceDesc);
        } catch (Exception e) {
            return AbilityInstBuildResult.failed(Message.code("LATTICE-CORE-RT-0003", instanceClass, e.getMessage()));
        }
    }

    private Set<ExtensionPointSpec> scanAbilityExtensions(IAbility domainAbility, AbilitySpec abilitySpec) {
        try {
            Set<ExtensionPointSpec> extensionPointSpecList = new HashSet<ExtensionPointSpec>();

            Class<?> returnType = findAbilityExtensionDefinition(domainAbility.getClass());
            if (null != returnType) {
                //是可扩展点的接口
                extensionPointSpecList.addAll(scanAbilityExtensions(Sets.newHashSet(), returnType, abilitySpec));
            }

            Method[] methods = domainAbility.getClass().getMethods();
            for (Method method : methods) {
                returnType = method.getReturnType();
                if (!ClassUtils.isAssignable(returnType, IBusinessExt.class)) {
                    continue;
                }

                //是可扩展点的接口
                extensionPointSpecList.addAll(
                        scanAbilityExtensions(extensionPointSpecList.stream()
                                        .map(BaseSpec::getCode).collect(Collectors.toSet()),
                                returnType, abilitySpec));
            }
            return extensionPointSpecList;
        } catch (Throwable th) {
            Message message = Message.code("LATTICE-CORE-RT-0004", domainAbility.getClass().getName(),
                    th.getMessage());
            log.error(message.getText(), th);
            throw th;
        }
    }

    private Class<?> findAbilityExtensionDefinition(Class abilityClass) {
        Object genericSuperclass = abilityClass.getGenericSuperclass();
        if (genericSuperclass instanceof ParameterizedType) {
            ParameterizedType type = (ParameterizedType) genericSuperclass;
            for (Type actualType : type.getActualTypeArguments()) {
                try {
                    Class<?> returnType;
                    if (CLASS_LOADER_THREAD_LOCAL.get() != null) {
                        returnType = Class.forName(actualType.getTypeName(), true, CLASS_LOADER_THREAD_LOCAL.get());
                    } else {
                        returnType = Class.forName(actualType.getTypeName());
                    }
                    if (ClassUtils.isAssignable(returnType, IBusinessExt.class)) {
                        return returnType;
                    }
                } catch (ClassNotFoundException e) {
                    log.warn(e.getMessage(), e);
                    continue;
                }
            }
        }
        if (ClassUtils.isAssignable(abilityClass.getSuperclass(), IAbility.class)) {
            return findAbilityExtensionDefinition(abilityClass.getSuperclass());
        }
        return null;
    }

    private Set<ExtensionPointSpec> scanAbilityExtensions(Set<String> existedSet, Class<?> itfClass, AbilitySpec abilitySpec) {
        Set<ExtensionPointSpec> extensionPointSpecList = new HashSet<>();
        Method[] methods = itfClass.getMethods();
        for (Method method : methods) {
            ExtensionAnnotation annotation = getExtensionAnnotation(method);
            if (null == annotation) {
                continue;
            }

            if (existedSet.contains(annotation.getCode()))
                continue;
            ExtensionPointSpec extensionPointSpec = buildExtensionPointSpec(annotation, abilitySpec, itfClass, method);
            if (null != extensionPointSpec) {
                extensionPointSpecList.add(extensionPointSpec);
            }
        }

        return extensionPointSpecList;
    }

    public ExtensionPointSpec buildExtensionPointSpec(
            ExtensionAnnotation annotation,
            AbilitySpec abilitySpec, Class<?> itfClass, Method method) {

        if (null == annotation)
            return null;

        return buildExtensionPointSpec(abilitySpec, annotation.getCode(),
                annotation.getName(), annotation.getDesc(), itfClass, method,
                annotation.getReduceType());
    }

    private ExtensionPointSpec buildExtensionPointSpec(AbilitySpec abilitySpec, String extensionCode,
                                                       String extensionName,
                                                       String extensionDesc,
                                                       Class<?> itfClass, Method method,
                                                       ReduceType reduceType) {

        ExtensionPointSpec extensionPointSpec =
                ExtensionPointSpec.of(method, abilitySpec.getCode(),
                        extensionCode, extensionName, extensionDesc);
        extensionPointSpec.setReduceType(reduceType);
        extensionPointSpec.setItfClass(itfClass);
        return extensionPointSpec;
    }
}

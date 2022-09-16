package org.hiforce.lattice.runtime.ability.register;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.hifforce.lattice.annotation.model.AbilityAnnotation;
import org.hifforce.lattice.annotation.parser.AbilityAnnotationParser;
import org.hifforce.lattice.exception.LatticeRuntimeException;
import org.hifforce.lattice.message.Message;
import org.hifforce.lattice.model.ability.IAbility;
import org.hifforce.lattice.model.register.AbilityInstSpec;
import org.hifforce.lattice.model.register.AbilitySpec;
import org.hiforce.lattice.runtime.Lattice;
import org.hiforce.lattice.runtime.ability.dto.AbilityRegDTO;
import org.hiforce.lattice.runtime.cache.LatticeRuntimeCache;
import org.hiforce.lattice.runtime.spi.LatticeSpiFactory;
import org.hiforce.lattice.runtime.utils.LatticeClassUtils;
import org.springframework.aop.support.AopUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

import static org.hiforce.lattice.runtime.utils.LatticeBeanUtils.getAndCreateSpringBeanViaClass;

/**
 * @author Rocky Yu
 * @since 2022/9/15
 */
@Slf4j
public class AbilityRegister {

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

    public List<AbilitySpec> register(AbilityRegDTO regDTO) {
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
            AbilityAnnotation annotationInfo = parser.buildAnnotationInfo(annotation);
            return Pair.of(annotationInfo, currentClass);
        }
        return null;
    }

    public synchronized List<AbilityInstSpec> scanAbilityInstance(AbilitySpec abilitySpec, Collection<Class> classSet) {

        List<AbilityInstSpec> abilityInstanceSpecList = new ArrayList<>();
        abilityInstanceSpecList.addAll(registerAbilityInstances(abilitySpec, classSet));
        abilityInstanceSpecList.sort(Comparator.comparingInt(AbilityInstSpec::getPriority));
        return abilityInstanceSpecList;
    }

    public Collection<AbilitySpec> getAllRegisteredAbilities() {
        return getRuntimeCache().getAllCachedAbilities();
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
        Object beanViaClass = getAndCreateSpringBeanViaClass(instanceClass);
        if (beanViaClass instanceof IAbility) {
            ability = (IAbility) beanViaClass;
            if (AopUtils.isAopProxy(ability)) {
                Class<?> originCls = AopUtils.getTargetClass(ability);
                try {
                    Object curObject = getAndCreateSpringBeanViaClass(originCls);
                    originAbility = (IAbility) curObject;
                } catch (Exception e) {
                    log.error("createPluginConfig origin ability error,class:" + originCls.getName());
                    return AbilityInstBuildResult.failed(Message.code("LATTICE-CORE-RT-0002", originCls.getName()));
                }
            } else {
                originAbility = ability;
            }
        } else {
            log.warn(instanceClass.getName() + " is not IAbility Type");
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
            instance.setAbilityCode(abilitySpec.getCode());

            //TODO: 注册能力实力下的扩展点信息

            abilitySpec.addAbilityInstance(instanceDesc);

            return AbilityInstBuildResult.success(instanceDesc);
        } catch (Exception e) {
            return AbilityInstBuildResult.failed(Message.code("LATTICE-CORE-RT-0003", instanceClass, e.getMessage()));
        }
    }
}

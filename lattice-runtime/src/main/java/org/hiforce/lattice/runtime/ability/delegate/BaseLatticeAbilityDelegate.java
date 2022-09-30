package org.hiforce.lattice.runtime.ability.delegate;

import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.hifforce.lattice.annotation.model.ProtocolType;
import org.hifforce.lattice.cache.ITemplateCache;
import org.hifforce.lattice.cache.invoke.InvokeCache;
import org.hifforce.lattice.exception.LatticeRuntimeException;
import org.hifforce.lattice.extension.ExtensionRemoteRunner;
import org.hifforce.lattice.extension.ExtensionRunner;
import org.hifforce.lattice.extension.RemoteExtensionRunnerBuilderBean;
import org.hifforce.lattice.extension.RunnerItemEntry;
import org.hifforce.lattice.message.Message;
import org.hifforce.lattice.model.ability.IBusinessExt;
import org.hifforce.lattice.model.business.IBizObject;
import org.hifforce.lattice.model.business.ITemplate;
import org.hifforce.lattice.model.business.TemplateType;
import org.hifforce.lattice.model.config.BusinessConfig;
import org.hifforce.lattice.model.config.ExtPriority;
import org.hifforce.lattice.model.config.ExtPriorityConfig;
import org.hifforce.lattice.model.context.BizSessionContext;
import org.hifforce.lattice.model.register.BusinessSpec;
import org.hifforce.lattice.model.register.ExtensionPointSpec;
import org.hifforce.lattice.model.register.RealizationSpec;
import org.hifforce.lattice.model.register.TemplateSpec;
import org.hifforce.lattice.utils.BizCodeUtils;
import org.hiforce.lattice.runtime.Lattice;
import org.hiforce.lattice.runtime.ability.BaseLatticeAbility;
import org.hiforce.lattice.runtime.ability.execute.RunnerCollection;
import org.hiforce.lattice.runtime.ability.execute.filter.ExtensionFilter;
import org.hiforce.lattice.runtime.ability.execute.filter.ProductFilter;
import org.hiforce.lattice.runtime.ability.execute.runner.ExtensionJavaRunner;
import org.hiforce.lattice.runtime.cache.ExtensionInvokeCache;
import org.hiforce.lattice.runtime.cache.ExtensionRunnerCacheKey;
import org.hiforce.lattice.runtime.cache.LatticeRuntimeCache;
import org.hiforce.lattice.runtime.cache.NotExistedExtensionPointRealization;
import org.hiforce.lattice.runtime.cache.key.ExtensionInvokeCacheKey;
import org.hiforce.lattice.runtime.spi.IRunnerCollectionBuilder;
import org.hiforce.lattice.runtime.spi.LatticeRuntimeSpiFactory;
import org.hiforce.lattice.runtime.utils.SpringApplicationContextHolder;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import static org.hiforce.lattice.runtime.ability.execute.RunnerCollection.ACCEPT_ALL;

/**
 * @author Rocky Yu
 * @since 2022/9/16
 */
@SuppressWarnings("all")
@Slf4j
public class BaseLatticeAbilityDelegate {

    public final static Object NULL_OBJECT = new Object();

    @SuppressWarnings("rawtypes")
    private final BaseLatticeAbility ability;

    @SuppressWarnings("rawtypes")
    public BaseLatticeAbilityDelegate(BaseLatticeAbility ability) {
        this.ability = ability;
    }

    public <R> RunnerCollection<R> loadExtensionRunners(
            @Nonnull String extCode, ExtensionFilter filter) {
        String bizCode = ability.getContext().getBizCode();
        String scenario = ability.getContext().getScenario();
        IBizObject bizObject = ability.getContext().getBizObject();
        boolean onlyProduct = !filter.isLoadBusinessExt();

        List<RunnerItemEntry<R>> cachedRunners = null;
        LatticeRuntimeCache runtimeCache = Lattice.getInstance().getLatticeRuntimeCache();
        ExtensionPointSpec extensionPointSpec = runtimeCache.getExtensionSpecCache().getKey1Only(extCode);
        if (null == extensionPointSpec) {
            throw new LatticeRuntimeException("LATTICE-CORE-RT-0006", extCode);
        }

        BusinessConfig businessConfig = Lattice.getInstance().getBusinessConfigByBizCode(bizCode);
        if (null == businessConfig) {
            if (Lattice.getInstance().isSimpleMode()) {
                if (extensionPointSpec.getProtocolType() == ProtocolType.LOCAL) {
                    return buildDefaultRunnerCollection(extCode, onlyProduct);
                }
                cachedRunners = getCachedRemoteRunners(extCode, businessConfig);
                if (null != cachedRunners) {
                    return RunnerCollection.of(bizObject, cachedRunners, ACCEPT_ALL);
                }
                return buildDefaultRunnerCollection(extCode, onlyProduct);
            }
            throw new LatticeRuntimeException("LATTICE-CORE-RT-0012", bizCode);
        }

        if (extensionPointSpec.getProtocolType() == ProtocolType.REMOTE) {
            cachedRunners = getCachedRemoteRunners(extCode, businessConfig);
        } else {
            cachedRunners = getCachedLocalRunners(extCode, businessConfig, filter);
        }
        if (cachedRunners == null) {
            return buildDefaultRunnerCollection(extCode, onlyProduct);
        }

        BaseLatticeAbilityDelegate.SessionRelatedFilter<R> sessionRelatedFilter
                = filter == ExtensionFilter.DEFAULT_FILTER ?
                new BaseLatticeAbilityDelegate.NoProductFilterSessionRelatedFilter<>(filter, bizObject, bizCode) :
                new BaseLatticeAbilityDelegate.SessionRelatedFilter<>(filter, bizObject, bizCode);

        boolean loadBizExt = filter.isLoadBusinessExt();
        boolean loadDefaultExtension = ability.hasDefaultExtension();

        RunnerCollection<R> businessRunnerCollection = RunnerCollection.of(bizObject,
                filterEffectiveRunners(cachedRunners), sessionRelatedFilter,
                getDefaultRunnerProducer(bizCode, extCode, scenario), loadBizExt, loadDefaultExtension);

        return RunnerCollection.combine(buildCustomRunnerCollection(extCode, bizObject)
                , businessRunnerCollection);
    }

    private <R> List<RunnerItemEntry<R>> filterEffectiveRunners(
            List<RunnerItemEntry<R>> runners) {
        List<RunnerItemEntry<R>> output = Lists.newArrayList();
        for (RunnerItemEntry<R> runner : runners) {
            if (runner.getTemplate().getType().isVertical()) {
                output.add(runner);
                continue;
            }
            BizSessionContext bizSessionContext =
                    InvokeCache.instance().get(BizSessionContext.class, BizSessionContext.class);
            List<TemplateSpec<? extends ITemplate>> effective =
                    bizSessionContext.getEffectiveTemplates().get(ability.getContext().getBizCode());

            if (effective.stream().noneMatch(p -> StringUtils.equals(p.getCode(), runner.getTemplate().getCode()))) {
                continue;
            }
            output.add(runner);
        }
        return runners;
    }

    private <R> RunnerCollection<R> buildCustomRunnerCollection(
            String extensionCode, IBizObject bizInstance) {
        IRunnerCollectionBuilder runnerCollectionBuilder = LatticeRuntimeSpiFactory.getInstance().getRunnerCollectionBuilder();
        if (!runnerCollectionBuilder.isSupport(ability, extensionCode)) {
            return RunnerCollection.of(bizInstance, Lists.newArrayList(), ACCEPT_ALL);
        }
        return runnerCollectionBuilder.buildCustomRunnerCollection(ability, extensionCode);
    }

    private static boolean isTemplateEffected(String bizCode, TemplateSpec template) {
        if (template.getType() == TemplateType.BUSINESS) {
            return BizCodeUtils.isCodesMatched(bizCode, template.getCode());
        }

        BizSessionContext bizSessionContext =
                InvokeCache.instance().get(BizSessionContext.class, BizSessionContext.class);
        if (null == bizSessionContext) {
            return false;
        }

        List<TemplateSpec<? extends ITemplate>> effective = bizSessionContext.getEffectiveTemplates().get(bizCode);
        if (effective.stream().noneMatch(p -> StringUtils.equals(p.getCode(), template.getCode()))) {
            return false;
        }
        return true;
    }

    private <R> List<RunnerItemEntry<R>> getCachedRemoteRunners(String extCode, BusinessConfig businessConfig) {
        String scenario = ability.getContext().getScenario();
        String bizCode = ability.getContext().getBizCode();
        LatticeRuntimeCache runtimeCache = Lattice.getInstance().getLatticeRuntimeCache();
        ExtensionRunnerCacheKey key = new ExtensionRunnerCacheKey(
                extCode, bizCode, scenario, true, false);
        Object result = runtimeCache.getCachedExtensionRunner(ability, key);
        if (result != null) {
            if (result == NULL_OBJECT) {
                return null;
            } else {
                return (List<RunnerItemEntry<R>>) result;
            }
        }
        RemoteExtensionRunnerBuilderBean builderBean =
                SpringApplicationContextHolder.getSpringBean(RemoteExtensionRunnerBuilderBean.class);
        if (null == builderBean) {
            throw new LatticeRuntimeException("LATTICE-CORE-RT-0021", extCode);
        }

        BusinessSpec businessSpec = null;

        if (null == businessConfig) {
            businessSpec = new BusinessSpec();
            businessSpec.setType(TemplateType.BUSINESS);
            businessSpec.setCode(ability.getContext().getBizCode());
        } else {
            businessSpec = Lattice.getInstance()
                    .getRegisteredBusinessByCode(businessConfig.getBizCode());
        }

        ExtensionRemoteRunner<R> runner = builderBean.build(ability, businessSpec, extCode, scenario);
        RunnerItemEntry<R> runerItem = new RunnerItemEntry<R>(ability, businessSpec, runner);
        runtimeCache.doCacheExtensionRunner(ability, key, Lists.newArrayList(runerItem));
        return Lists.newArrayList(runerItem);
    }

    private <R> List<RunnerItemEntry<R>> getCachedLocalRunners(
            String extCode, BusinessConfig businessConfig, ExtensionFilter filter) {

        String scenario = ability.getContext().getScenario();
        String bizCode = ability.getContext().getBizCode();

        boolean supportCustomization = ability.supportCustomization();
        boolean isHorizontal = !filter.isLoadBusinessExt();
        LatticeRuntimeCache runtimeCache = Lattice.getInstance().getLatticeRuntimeCache();
        // cache
        ExtensionRunnerCacheKey key = new ExtensionRunnerCacheKey(
                extCode, bizCode, scenario, supportCustomization, isHorizontal);

        Object result = runtimeCache.getCachedExtensionRunner(ability, key);
        if (result != null) {
            if (result == NULL_OBJECT) {
                return null;
            } else {
                return (List<RunnerItemEntry<R>>) result;
            }
        }

        ExtPriorityConfig priorityConfig = businessConfig.getExtensions().stream()
                .filter(p -> StringUtils.equals(p.getExtCode(), extCode))
                .findFirst().orElse(null);
        if (null == priorityConfig) {
            runtimeCache.doCacheExtensionRunner(ability, key, NULL_OBJECT);
            return null;
        }

        List<RunnerItemEntry<R>> extensionRunners = new ArrayList<>();
        for (ExtPriority config : businessConfig.getExtPriorityByCode(extCode, isHorizontal)) {
            if (null == config)
                continue;
            BizSessionContext bizSessionContext =
                    InvokeCache.instance().get(BizSessionContext.class, BizSessionContext.class);
            if (null == bizSessionContext) {
                continue;
            }
            if (config.getType().isHorizontal() && config.getType().needInstall()) {
                if (!businessConfig.productInstalled(config.getCode())) {
                    continue;
                }
            }

            RunnerItemEntry<R> runnerItemEntry =
                    buildExtensionJavaRunnerItemEntry(extCode, config, bizCode, scenario);
            if (null != runnerItemEntry) {
                extensionRunners.add(runnerItemEntry);
            }
        }
        runtimeCache.doCacheExtensionRunner(ability, key, extensionRunners);
        return extensionRunners;
    }

    private <R> RunnerItemEntry<R> buildExtensionJavaRunnerItemEntry(
            String extensionCode, ExtPriority config, String bizCode, String scenario) {

        boolean supportCustomization = ability.supportCustomization();

        ExtensionRunner extensionJavaRunner = null;
        if (null == config) {
            if (log.isInfoEnabled()) {
                log.info(Message.code("LATTICE-CORE-RT-0013", extensionCode, bizCode).getText());
            }
            return null;
        }
        TemplateSpec template = config.getType().isVertical() ?
                getBusinessSpec(config.getCode()) : getHorizontalTemplateSpec(config.getCode());

        if (supportCustomization) {
            IBusinessExt extImpl = loadExtensionRealization(bizCode, scenario, template, extensionCode);
            if (null == extImpl) {
                if (log.isInfoEnabled()) {
                    log.info("[Lattice]The ExtensionFacade or ExtensionImplement is null. bizCode: [{}], extCode: [{}]",
                            bizCode, extensionCode);
                }
                extensionJavaRunner = null;
            } else {
                extensionJavaRunner = new ExtensionJavaRunner(extensionCode, extImpl);
            }
        } else {
            extensionJavaRunner = new ExtensionJavaRunner(extensionCode, ability.getDefaultRealization());
        }

        if (extensionJavaRunner != null) {
            return new RunnerItemEntry<>(ability, template, extensionJavaRunner);
        }
        return null;
    }

    @SuppressWarnings("all")
    public <R> RunnerCollection<R> buildDefaultRunnerCollection(
            String extCode, boolean onlyProduct) {
        String bizCode = ability.getContext().getBizObject().getBizCode();
        String scenario = ability.getContext().getScenario();
        boolean loadBizExt = !onlyProduct;
        boolean loadDefaultExt = ability.hasDefaultExtension();//Whether load the default ext realization.


        RunnerCollection<R> runnerCollection = LatticeRuntimeSpiFactory.getInstance()
                .getRunnerCollectionBuilder().buildCustomRunnerCollection(
                        ability, extCode);

        if (loadBizExt) {
            RunnerCollection.Producer producer = getDefaultRunnerProducer(bizCode, extCode, scenario);
            return RunnerCollection.combine(runnerCollection, producer, loadBizExt, loadDefaultExt);
        } else {
            return runnerCollection;
        }
    }


    @SuppressWarnings("all")
    private <R> RunnerCollection.Producer<R> getDefaultRunnerProducer(
            String bizCode,
            String extensionCode,
            String scenario) {

        return () -> {
            ExtensionRunner javaRunner = null;
            BusinessSpec template = getBusinessSpec(bizCode);

            if (ability.supportCustomization()) {
                IBusinessExt extImpl = loadExtensionRealization(bizCode, scenario, template, extensionCode);
                if (null == extImpl) {
                    javaRunner = null;
                } else {
                    javaRunner = new ExtensionJavaRunner(extensionCode, extImpl);
                }
            } else {
                if (ability.hasDefaultExtension()) {
                    javaRunner = new ExtensionJavaRunner(extensionCode, ability.getDefaultRealization());
                }
            }
            if (null == javaRunner) {
                return null;
            }
            return new RunnerItemEntry<>(ability, template, javaRunner);
        };
    }

    private BusinessSpec getBusinessSpec(String bizCode) {
        return Lattice.getInstance().getAllRegisteredBusinesses().stream()
                .filter(p -> StringUtils.equals(bizCode, p.getCode()))
                .findFirst().orElse(null);
    }

    private TemplateSpec getHorizontalTemplateSpec(String code) {
        TemplateSpec spec = Lattice.getInstance().getAllRegisteredProducts().stream()
                .filter(p -> StringUtils.equals(code, p.getCode()))
                .findFirst().orElse(null);
        if (null != spec) {
            return spec;
        }
        return Lattice.getInstance().getAllRegisteredUseCases().stream()
                .filter(p -> StringUtils.equals(code, p.getCode()))
                .findFirst().orElse(null);
    }

    private IBusinessExt loadExtensionRealization(
            String bizCode, String scenario, TemplateSpec template, String extPointCode) {

        ExtensionInvokeCacheKey cacheKey = null == template ? null :
                new ExtensionInvokeCacheKey(scenario, template, extPointCode);

        return internalLoadExtensionRealization(cacheKey, bizCode, scenario, template, extPointCode);
    }

    private IBusinessExt internalLoadExtensionRealization(
            ExtensionInvokeCacheKey cacheKey, String bizCode, String scenario, TemplateSpec template, String extPointCode) {

        IBusinessExt extImpl = ExtensionInvokeCache.getInstance().getCachedExtensionRealization(cacheKey);
        if (extImpl instanceof NotExistedExtensionPointRealization)
            return null;
        if (extImpl != null) {
            return extImpl;
        }

        if (null == template) {
            if (ability.hasDefaultExtension()) {
                return ability.getDefaultRealization();
            }
            return null;
        }

        IBusinessExt extFacade = null;

        extFacade = findIExtensionPointsFacadeViaScenario(scenario, template, extPointCode);
        if (extFacade != null) {
            extImpl = extFacade.getBusinessExtByCode(extPointCode, scenario);
        } else {
            if (ability.hasDefaultExtension()) {
                extImpl = ability.getDefaultRealization();
            }
        }

        if (null == extImpl) {
            ExtensionInvokeCache.getInstance().doCacheExtensionRealization(cacheKey, new NotExistedExtensionPointRealization());
            return null;
        }
        return ExtensionInvokeCache.getInstance().doCacheExtensionRealization(cacheKey, extImpl);
    }

    public IBusinessExt findIExtensionPointsFacadeViaScenario(String scenario, TemplateSpec template, String extPointCode) {
        IBusinessExt extFacade = null;

        ITemplateCache templateCache = Lattice.getInstance().getLatticeRuntimeCache().getTemplateCache();

        List<RealizationSpec> realizationSpecs = Lattice.getInstance().getAllRegisteredRealizations();

        if (StringUtils.isEmpty(scenario)) {
            for (RealizationSpec realization : realizationSpecs) {
                if (StringUtils.isNotEmpty(realization.getScenario())) {
                    continue;
                }
                IBusinessExt facade = realization.getBusinessExt();
                if (facade != null
                        && templateCache.templateCodeMatched(realization.getCode(), template.getCode())
                        && null != facade.getBusinessExtByCode(extPointCode, scenario)) {
                    extFacade = facade;
                    break;
                }
            }
        } else {
            for (RealizationSpec realization : realizationSpecs) {
                if (StringUtils.equals(scenario, realization.getScenario())) {
                    IBusinessExt facade = realization.getBusinessExt();
                    if (facade != null
                            && templateCache.templateCodeMatched(realization.getCode(), template.getCode())
                            && null != facade.getBusinessExtByCode(extPointCode, scenario)) {
                        extFacade = facade;
                        break;
                    }
                }
            }
            if (null == extFacade) {
                for (RealizationSpec realization : realizationSpecs) {
                    if (StringUtils.isNotEmpty(realization.getScenario())) {
                        continue;
                    }
                    IBusinessExt facade = realization.getBusinessExt();
                    if (facade != null
                            && templateCache.templateCodeMatched(realization.getCode(), template.getCode())
                            && null != facade.getBusinessExtByCode(extPointCode, scenario)) {
                        extFacade = facade;
                        break;
                    }
                }
            }
        }
        return extFacade;
    }


    private static class SessionRelatedFilter<R> implements Predicate<RunnerItemEntry<R>> {

        private ProductFilter productFilter;
        private ExtensionFilter extensionFilter;
        protected IBizObject bizObject;
        protected String bizCode;

        public SessionRelatedFilter(ExtensionFilter extensionFilter, IBizObject bizObject, String bizCode) {
            this.productFilter = extensionFilter.getProductFilter();
            this.extensionFilter = extensionFilter;
            this.bizObject = bizObject;
            this.bizCode = bizCode;
        }

        @Override
        public boolean test(RunnerItemEntry<R> entry) {
            String templateCode = entry.getTemplate().getCode();
            if (null != productFilter) {
                if (!productFilter.getAllowedCodes().contains(templateCode)) {
                    return false;
                }
            }
            IBizObject bizInstance = this.bizObject;
            return isTemplateEffected(bizInstance.getBizCode(), entry.getTemplate());
        }
    }

    private static class NoProductFilterSessionRelatedFilter<R> extends SessionRelatedFilter<R> {
        public NoProductFilterSessionRelatedFilter(ExtensionFilter extensionRunnerFilter, IBizObject bizInstance, String bizCode) {
            super(extensionRunnerFilter, bizInstance, bizCode);
        }

        @Override
        public boolean test(RunnerItemEntry<R> entry) {
            return isTemplateEffected(bizObject.getBizCode(), entry.getTemplate());
        }
    }
}

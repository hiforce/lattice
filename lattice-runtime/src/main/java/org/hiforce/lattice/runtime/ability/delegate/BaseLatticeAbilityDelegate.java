package org.hiforce.lattice.runtime.ability.delegate;

import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.hifforce.lattice.cache.ITemplateCache;
import org.hifforce.lattice.cache.invoke.InvokeCache;
import org.hifforce.lattice.exception.LatticeRuntimeException;
import org.hifforce.lattice.message.Message;
import org.hifforce.lattice.model.ability.IBusinessExt;
import org.hifforce.lattice.model.business.IBizObject;
import org.hifforce.lattice.model.business.TemplateType;
import org.hifforce.lattice.model.config.BusinessConfig;
import org.hifforce.lattice.model.config.ExtPriority;
import org.hifforce.lattice.model.config.PriorityConfig;
import org.hifforce.lattice.model.context.BizSessionContext;
import org.hifforce.lattice.model.register.BusinessSpec;
import org.hifforce.lattice.model.register.ProductSpec;
import org.hifforce.lattice.model.register.RealizationSpec;
import org.hifforce.lattice.model.register.TemplateSpec;
import org.hiforce.lattice.runtime.Lattice;
import org.hiforce.lattice.runtime.ability.BaseLatticeAbility;
import org.hiforce.lattice.runtime.ability.execute.IRunnerCollectionBuilder;
import org.hiforce.lattice.runtime.ability.execute.RunnerCollection;
import org.hiforce.lattice.runtime.ability.execute.filter.ExtensionFilter;
import org.hiforce.lattice.runtime.ability.execute.filter.ProductFilter;
import org.hiforce.lattice.runtime.ability.execute.runner.ExtensionJavaRunner;
import org.hiforce.lattice.runtime.ability.execute.runner.ExtensionRunner;
import org.hiforce.lattice.runtime.cache.ExtensionInvokeCache;
import org.hiforce.lattice.runtime.cache.ExtensionRunnerCacheKey;
import org.hiforce.lattice.runtime.cache.LatticeRuntimeCache;
import org.hiforce.lattice.runtime.cache.NotExistedExtensionPointRealization;
import org.hiforce.lattice.runtime.cache.key.ExtensionInvokeCacheKey;
import org.hiforce.lattice.runtime.spi.LatticeSpiFactory;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

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

    public <BusinessExt extends IBusinessExt, R> RunnerCollection<BusinessExt, R> loadExtensionRunners(
            @Nonnull String extCode, ExtensionFilter filter) {
        String bizCode = ability.getContext().getBizCode();
        String scenario = ability.getContext().getScenario();
        IBizObject bizObject = ability.getContext().getBizObject();
        boolean onlyProduct = !filter.isLoadBusinessExt();

        BusinessConfig businessConfig = Lattice.getInstance().getBusinessConfigByBizCode(bizCode);
        if (null == businessConfig) {
            if (Lattice.getInstance().isSimpleMode()) {
                return buildDefaultRunnerCollection(extCode, onlyProduct);
            }
            throw new LatticeRuntimeException("LATTICE-CORE-RT-0012", bizCode);
        }


        List<RunnerCollection.RunnerItemEntry<BusinessExt, R>> cachedRunners =
                getCachedRunners(extCode, businessConfig, filter);
        if (cachedRunners == null) {
            return buildDefaultRunnerCollection(extCode, onlyProduct);
        }

        BaseLatticeAbilityDelegate.SessionRelatedFilter<BusinessExt, R> sessionRelatedFilter
                = filter == ExtensionFilter.DEFAULT_FILTER ?
                new BaseLatticeAbilityDelegate.NoProductFilterSessionRelatedFilter<>(filter, bizObject, bizCode) :
                new BaseLatticeAbilityDelegate.SessionRelatedFilter<>(filter, bizObject, bizCode);

        boolean loadBizExt = filter.isLoadBusinessExt();
        boolean loadDefaultExtension = ability.hasDefaultExtension();

        RunnerCollection<BusinessExt, R> businessRunnerCollection = RunnerCollection.of(bizObject,
                cachedRunners, sessionRelatedFilter,
                getDefaultRunnerProducer(bizCode, extCode, scenario), loadBizExt, loadDefaultExtension);

        return RunnerCollection.combine(buildCustomRunnerCollection(extCode, bizObject)
                , businessRunnerCollection);
    }

    private <BusinessExt extends IBusinessExt, R> RunnerCollection<BusinessExt, R> buildCustomRunnerCollection(
            String extensionCode, IBizObject bizInstance) {
        IRunnerCollectionBuilder runnerCollectionBuilder = LatticeSpiFactory.getInstance().getRunnerCollectionBuilder();
        if (!runnerCollectionBuilder.isSupport(ability, extensionCode)) {
            return RunnerCollection.of(bizInstance, Lists.newArrayList(), RunnerCollection.ACCEPT_ALL);
        }
        return runnerCollectionBuilder.buildCustomRunnerCollection(ability, extensionCode);
    }

    private static boolean isTemplateEffected(String bizCode, String templateCode) {
        BizSessionContext bizSessionContext =
                InvokeCache.instance().get(BizSessionContext.class, BizSessionContext.class);
        if (null == bizSessionContext) {
            return false;
        }

        List<ProductSpec> effective = bizSessionContext.getEffectiveProducts().get(bizCode);
        if (effective.stream().noneMatch(p -> StringUtils.equals(p.getCode(), templateCode))) {
            return false;
        }
        return true;
    }

    private <ExtensionPoints extends IBusinessExt, R> List<RunnerCollection.RunnerItemEntry<ExtensionPoints, R>> getCachedRunners(
            String extensionCode, BusinessConfig businessConfig, ExtensionFilter filter) {

        String scenario = ability.getContext().getScenario();
        String bizCode = ability.getContext().getBizCode();

        boolean supportCustomization = ability.supportCustomization();
        boolean isOnlyProduct = !filter.isLoadBusinessExt();
        LatticeRuntimeCache runtimeCache = Lattice.getInstance().getLatticeRuntimeCache();
        // cache
        ExtensionRunnerCacheKey key = new ExtensionRunnerCacheKey(
                extensionCode, bizCode, scenario, supportCustomization, isOnlyProduct);

        Object result = runtimeCache.getCachedExtensionRunner(ability, key);
        if (result != null) {
            if (result == NULL_OBJECT) {
                return null;
            } else {
                return (List<RunnerCollection.RunnerItemEntry<ExtensionPoints, R>>) result;
            }
        }

        PriorityConfig priorityConfig = businessConfig.getPriorityConfigs().stream()
                .filter(p -> StringUtils.equals(p.getExtCode(), extensionCode))
                .findFirst().orElse(null);
        if (null == priorityConfig) {
            runtimeCache.doCacheExtensionRunner(ability, key, NULL_OBJECT);
            return null;
        }

        List<RunnerCollection.RunnerItemEntry<ExtensionPoints, R>> extensionRunners = new ArrayList<>();
        for (ExtPriority config : businessConfig.getProductConfigByExtCode(extensionCode, isOnlyProduct)) {
            //接下来再根据运营平台的配置，看当前的模板是否有效并构建出Runner
            if (null == config)
                continue;
            if (config.getType() == TemplateType.BUSINESS
                    || !businessConfig.productInstalled(config.getCode())) {
                continue;
            }
            BizSessionContext bizSessionContext =
                    InvokeCache.instance().get(BizSessionContext.class, BizSessionContext.class);
            if (null == bizSessionContext) {
                continue;
            }

            List<ProductSpec> effective = bizSessionContext.getEffectiveProducts().get(bizCode);
            if (effective.stream().noneMatch(p -> StringUtils.equals(p.getCode(), config.getCode()))) {
                continue;
            }

            RunnerCollection.RunnerItemEntry<ExtensionPoints, R> runnerItemEntry =
                    buildExtensionJavaRunnerItemEntry(extensionCode, config, bizCode, scenario);
            if (null != runnerItemEntry) {
                extensionRunners.add(runnerItemEntry);
            }
        }
        runtimeCache.doCacheExtensionRunner(ability, key, extensionRunners);
        return extensionRunners;
    }

    private <ExtensionPoints extends IBusinessExt, R> RunnerCollection.RunnerItemEntry<ExtensionPoints, R> buildExtensionJavaRunnerItemEntry(
            String extensionCode, ExtPriority config, String bizCode, String scenario) {

        boolean supportCustomization = ability.supportCustomization();

        ExtensionRunner extensionJavaRunner = null;
        if (null == config) {
            if (log.isInfoEnabled()) {
                log.info(Message.code("LATTICE-CORE-RT-0013", extensionCode, bizCode).getText());
            }
            return null;
        }
        TemplateSpec template = config.getType() == TemplateType.BUSINESS ?
                getBusinessSpec(config.getCode()) : getProductSpec(config.getCode());

        if (supportCustomization) {
            IBusinessExt extImpl = loadExtensionRealization(bizCode, scenario, template, extensionCode);
            if (null == extImpl) {
                if (log.isInfoEnabled()) {
                    log.info("[Lattice]The ExtensionFacade or ExtensionImplement is null. bizCode: [{}], extCode: [{}]",
                            bizCode, extensionCode);
                }
                extensionJavaRunner = null;
            } else {
                int priority = config.getPriority();
                extensionJavaRunner = new ExtensionJavaRunner(extensionCode, priority, extImpl);
            }
        } else {
            extensionJavaRunner = new ExtensionJavaRunner(extensionCode, config.getPriority(),
                    ability.getDefaultRealization());
        }

        if (extensionJavaRunner != null) {
            return new RunnerCollection.RunnerItemEntry<>(template, extensionJavaRunner, ability);
        }
        return null;
    }

    @SuppressWarnings("all")
    public <BusinessExt extends IBusinessExt, R> RunnerCollection<BusinessExt, R> buildDefaultRunnerCollection(
            String extCode, boolean onlyProduct) {
        String bizCode = ability.getContext().getBizObject().getBizCode();
        String scenario = ability.getContext().getScenario();
        boolean loadBizExt = !onlyProduct;
        boolean loadDefaultExt = ability.hasDefaultExtension();//Whether load the default ext realization.


        RunnerCollection<BusinessExt, R> runnerCollection = LatticeSpiFactory.getInstance()
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
    private <ExtensionPoints extends IBusinessExt, R> RunnerCollection.Producer<ExtensionPoints, R> getDefaultRunnerProducer(
            String bizCode,
            String extensionCode,
            String scenario) {

        return () -> {
            ExtensionRunner extensionJavaRunner = null;
            BusinessSpec template = getBusinessSpec(bizCode);

            if (ability.supportCustomization()) {
                IBusinessExt extImpl = loadExtensionRealization(bizCode, scenario, template, extensionCode);
                if (null == extImpl) {
                    extensionJavaRunner = null;
                } else {
                    int priority = -1;
                    extensionJavaRunner = new ExtensionJavaRunner(extensionCode, priority, extImpl);
                }
            } else {
                if (ability.hasDefaultExtension()) {
                    extensionJavaRunner = new ExtensionJavaRunner(extensionCode, -1, ability.getDefaultRealization());
                }
            }
            if (null == extensionJavaRunner) {
                return null;
            }
            return new RunnerCollection.RunnerItemEntry<>(template, extensionJavaRunner, ability);
        };
    }

    private BusinessSpec getBusinessSpec(String bizCode) {
        return Lattice.getInstance().getAllRegisteredBusinesses().stream()
                .filter(p -> StringUtils.equals(bizCode, p.getCode()))
                .findFirst().orElse(null);
    }

    private ProductSpec getProductSpec(String code) {
        return Lattice.getInstance().getAllRegisteredProducts().stream()
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


    private static class SessionRelatedFilter<ExtensionPoints, R> implements Predicate<RunnerCollection.RunnerItemEntry<ExtensionPoints, R>> {

        private ProductFilter productFilter;
        private ExtensionFilter extensionRunnerFilter;
        protected IBizObject bizInstance;
        protected String bizCode;

        public SessionRelatedFilter(ExtensionFilter extensionRunnerFilter, IBizObject bizInstance, String bizCode) {
            this.productFilter = extensionRunnerFilter.getProductFilter();
            this.extensionRunnerFilter = extensionRunnerFilter;
            this.bizInstance = bizInstance;
            this.bizCode = bizCode;
        }

        @Override
        public boolean test(RunnerCollection.RunnerItemEntry<ExtensionPoints, R> entry) {
            String templateCode = entry.getTemplate().getCode();
            if (null != productFilter) {
                if (!productFilter.getAllowedProductCodes().contains(templateCode)) {
                    return false;
                }
            }
            IBizObject bizInstance = this.bizInstance;
            return isTemplateEffected(bizInstance.getBizCode(), entry.getTemplate().getCode());
        }
    }

    private static class NoProductFilterSessionRelatedFilter<ExtensionPoints, R> extends SessionRelatedFilter<ExtensionPoints, R> {
        public NoProductFilterSessionRelatedFilter(ExtensionFilter extensionRunnerFilter, IBizObject bizInstance, String bizCode) {
            super(extensionRunnerFilter, bizInstance, bizCode);
        }

        @Override
        public boolean test(RunnerCollection.RunnerItemEntry<ExtensionPoints, R> entry) {
            IBizObject bizInstance = this.bizInstance;
            //从模板的物理归属上看，当前业务（BizCode）是否可以使用该模板。。比如，五道口APP里定义的产品模板不应该能被其他业务所使用
            return isTemplateEffected(bizInstance.getBizCode(), entry.getTemplate().getCode());
        }
    }
}

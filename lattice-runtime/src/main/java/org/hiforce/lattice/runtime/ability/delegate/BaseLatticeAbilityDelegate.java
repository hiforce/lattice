package org.hiforce.lattice.runtime.ability.delegate;

import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.hiforce.lattice.annotation.model.ProtocolType;
import org.hiforce.lattice.cache.ITemplateCache;
import org.hiforce.lattice.cache.invoke.InvokeCache;
import org.hiforce.lattice.exception.LatticeRuntimeException;
import org.hiforce.lattice.extension.ExtensionRunner;
import org.hiforce.lattice.extension.RemoteExtensionRunnerBuilder;
import org.hiforce.lattice.extension.RunnerItemEntry;
import org.hiforce.lattice.message.Message;
import org.hiforce.lattice.model.ability.IBusinessExt;
import org.hiforce.lattice.model.business.IBizObject;
import org.hiforce.lattice.model.business.ITemplate;
import org.hiforce.lattice.model.business.TemplateType;
import org.hiforce.lattice.model.config.BusinessConfig;
import org.hiforce.lattice.model.config.ExtPriority;
import org.hiforce.lattice.model.config.ExtPriorityConfig;
import org.hiforce.lattice.model.context.BizSessionContext;
import org.hiforce.lattice.model.register.BusinessSpec;
import org.hiforce.lattice.model.register.ExtensionSpec;
import org.hiforce.lattice.model.register.RealizationSpec;
import org.hiforce.lattice.model.register.TemplateSpec;
import org.hiforce.lattice.runtime.Lattice;
import org.hiforce.lattice.runtime.ability.BaseLatticeAbility;
import org.hiforce.lattice.runtime.ability.execute.RunnerCollection;
import org.hiforce.lattice.runtime.ability.execute.filter.ExtensionFilter;
import org.hiforce.lattice.runtime.ability.execute.filter.ProductFilter;
import org.hiforce.lattice.runtime.ability.execute.runner.ExtensionJavaRunner;
import org.hiforce.lattice.runtime.ability.register.TemplateRegister;
import org.hiforce.lattice.runtime.cache.LatticeRuntimeCache;
import org.hiforce.lattice.runtime.cache.ability.AbilityCache;
import org.hiforce.lattice.runtime.cache.exension.ExtensionInvokeCache;
import org.hiforce.lattice.runtime.cache.exension.NotExistedRealization;
import org.hiforce.lattice.runtime.cache.index.TemplateIndex;
import org.hiforce.lattice.runtime.cache.key.ExtensionInvokeCacheKey;
import org.hiforce.lattice.runtime.cache.key.ExtensionRunnerCacheKey;
import org.hiforce.lattice.runtime.spi.IRunnerCollectionBuilder;
import org.hiforce.lattice.runtime.spi.LatticeRuntimeSpiFactory;
import org.hiforce.lattice.runtime.utils.SpringApplicationContextHolder;
import org.hiforce.lattice.utils.BizCodeUtils;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static org.hiforce.lattice.runtime.ability.execute.RunnerCollection.ACCEPT_ALL;
import static org.hiforce.lattice.utils.BizCodeUtils.isCodeMatched;

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

    public static volatile boolean remoteExtensionInitFlag;

    private BusinessConfig loadBusinessConfig(String bizCode, ExtensionSpec extension) {
        BusinessConfig businessConfig = Lattice.getInstance().getBusinessConfigByBizCode(bizCode);
        if (extension.getProtocolType() == ProtocolType.LOCAL) {
            return businessConfig;
        } else if (extension.getProtocolType() == ProtocolType.REMOTE) {
            if (!Lattice.getInstance().isSimpleMode() || remoteExtensionInitFlag) {
                return businessConfig;
            }
            BusinessSpec businessSpec = TemplateRegister.getInstance().getBusinesses().stream()
                    .filter(p -> StringUtils.equals(p.getCode(), bizCode))
                    .findFirst().orElseGet(() -> {
                        BusinessSpec spec = new BusinessSpec();
                        spec.setCode(bizCode);
                        spec.setName("Remote Product [" + bizCode + "]");
                        TemplateIndex.getInstance().addTemplateIndex(spec);
                        TemplateRegister.getInstance().getBusinesses().add(spec);
                        return spec;
                    });
            Set<ExtensionSpec> remoteExtensionSet = Lattice.getInstance().getAllRegisteredAbilities().stream()
                    .flatMap(p -> p.getAbilityInstances().stream())
                    .flatMap(p -> p.getExtensions().stream())
                    .filter(p -> p.getProtocolType() == ProtocolType.REMOTE)
                    .collect(Collectors.toSet());

            RealizationSpec realization = new RealizationSpec();
            realization.setCode(bizCode);
            realization.setRemote(true);
            businessSpec.getRealizations().add(realization);

            TemplateRegister.getInstance().getProducts()
                    .forEach(p -> p.getRealizations().add(realization));

            remoteExtensionSet.forEach(p -> realization.getExtensionCodes().add(p.getCode()));
            businessConfig = Lattice.getInstance().autoAddAndBuildBusinessConfig(businessSpec, ProtocolType.REMOTE);
            remoteExtensionInitFlag = true;
        }
        return businessConfig;
    }

    public <R> RunnerCollection<R> loadExtensionRunners(
            @Nonnull String extCode, ExtensionFilter filter) {
        String bizCode = ability.getContext().getBizCode();
        String scenario = ability.getContext().getScenario();
        IBizObject bizObject = ability.getContext().getBizObject();
        boolean onlyProduct = !filter.isLoadBusinessExt();

        List<RunnerItemEntry<R>> cachedRunners = null;
        LatticeRuntimeCache runtimeCache = Lattice.getInstance().getRuntimeCache();
        ExtensionSpec extensionSpec = runtimeCache.getExtensionCache().getExtensionIndex().getKey1Only(extCode);
        if (null == extensionSpec) {
            throw new LatticeRuntimeException("LATTICE-CORE-RT-0006", extCode);
        }

        BusinessConfig businessConfig = loadBusinessConfig(bizCode, extensionSpec);
        if (null == businessConfig) {
            if (Lattice.getInstance().isSimpleMode()) {
                return buildDefaultRunnerCollection(extCode, onlyProduct);
            }
            throw new LatticeRuntimeException("LATTICE-CORE-RT-0012", bizCode);
        }

        cachedRunners = getCachedExtensionRunners(extensionSpec, businessConfig, filter);
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
        List<RunnerItemEntry<R>> effectiveRunners = Lists.newArrayList();
        for (RunnerItemEntry<R> runner : runners) {
            if (runner.getTemplate().getType().isVertical()) {
                effectiveRunners.add(runner);
                continue;
            }
            BizSessionContext bizSessionContext =
                    InvokeCache.instance().get(BizSessionContext.class, BizSessionContext.class);
            if (!Lattice.getInstance().isSimpleMode() && null == bizSessionContext) {
                throw new LatticeRuntimeException("LATTICE-CORE-RT-0024", ability.getContext().getExtCode());
            }
            List<TemplateSpec<? extends ITemplate>> effective = (null == bizSessionContext) ? Lists.newArrayList()
                    : bizSessionContext.getEffectiveTemplates()
                    .get(ability.getContext().getBizCode());

            if (effective.stream().noneMatch(p -> StringUtils.equals(p.getCode(), runner.getTemplate().getCode()))) {
                continue;
            }
            effectiveRunners.add(runner);
        }
        return effectiveRunners;
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

    private <R> List<RunnerItemEntry<R>> getCachedExtensionRunners(
            ExtensionSpec extension, BusinessConfig businessConfig, ExtensionFilter filter) {

        String scenario = ability.getContext().getScenario();
        String bizCode = ability.getContext().getBizCode();

        boolean supportCustomization = ability.supportCustomization();
        boolean isHorizontal = !filter.isLoadBusinessExt();
        LatticeRuntimeCache runtimeCache = Lattice.getInstance().getRuntimeCache();
        // cache
        ExtensionRunnerCacheKey key = new ExtensionRunnerCacheKey(
                extension.getCode(), bizCode, scenario, supportCustomization, isHorizontal);

        Object result = AbilityCache.getInstance().getCachedExtensionRunner(ability.getClass(), key);
        if (result != null) {
            if (result == NULL_OBJECT) {
                return null;
            } else {
                List<RunnerItemEntry<R>>  runnerItemEntryList = (List<RunnerItemEntry<R>>) result;
                runnerItemEntryList.forEach(r->{
                    r.setAbility(ability);
                    r.getRunner().setAbility(ability);
                });
            }
        }

        ExtPriorityConfig priorityConfig = businessConfig.getExtensions().stream()
                .filter(p -> StringUtils.equals(p.getExtCode(), extension.getCode()))
                .findFirst().orElse(null);
        if (null == priorityConfig) {
            AbilityCache.getInstance().doCacheExtensionRunner(ability.getClass(), key, NULL_OBJECT);
            return null;
        }

        List<RunnerItemEntry<R>> extensionRunners = new ArrayList<>();
        for (ExtPriority config : businessConfig.getExtPriorityByCode(extension.getCode(), isHorizontal)) {
            if (null == config)
                continue;
            BizSessionContext bizSessionContext =
                    InvokeCache.instance().get(BizSessionContext.class, BizSessionContext.class);
            if (null == bizSessionContext) {
                RunnerItemEntry<R> runnerItemEntry =
                        buildExtensionRunnerItemEntry(extension, config, bizCode, scenario);
                if (null != runnerItemEntry) {
                    extensionRunners.add(runnerItemEntry);
                }
                continue;
            }

            if (config.getType().isHorizontal() && config.getType().needInstall()) {
                if (!businessConfig.productInstalled(config.getCode())) {
                    continue;
                }
            }
            RunnerItemEntry<R> runnerItemEntry =
                    buildExtensionRunnerItemEntry(extension, config, bizCode, scenario);
            if (null != runnerItemEntry) {
                extensionRunners.add(runnerItemEntry);
            }
        }
        AbilityCache.getInstance().doCacheExtensionRunner(ability.getClass(), key, extensionRunners);
        return extensionRunners;
    }

    private <R> ExtensionRunner<R> buildRemoteExtensionRunner(
            TemplateSpec template, ExtensionSpec extension, String bizCode, String scenario) {
        List<RealizationSpec> realizations = template.getRealizations();
        RealizationSpec realization = realizations.stream().filter(r -> r.getExtensionCodes().contains(extension.getCode()))
                .findFirst().orElseThrow(() -> new LatticeRuntimeException("LATTICE-CORE-RT-0026", extension.getCode()));
        if (!realization.isRemote()) {
            return buildLocalExtensionRunner(template, extension, bizCode, scenario);
        }

        RemoteExtensionRunnerBuilder builderBean =
                SpringApplicationContextHolder.getSpringBean(RemoteExtensionRunnerBuilder.class);
        if (null == builderBean) {
            throw new LatticeRuntimeException("LATTICE-CORE-RT-0021", extension.getCode());
        }
        return builderBean.build(ability, template, extension.getCode(), scenario);
    }

    private <R> ExtensionRunner<R> buildLocalExtensionRunner(
            TemplateSpec template, ExtensionSpec extension, String bizCode, String scenario) {

        ExtensionRunner extensionJavaRunner = null;

        IBusinessExt extImpl = loadExtensionRealization(bizCode, scenario, template, extension.getCode());
        if (null == extImpl) {
            if (log.isInfoEnabled()) {
                log.info("[Lattice]The ExtensionFacade or ExtensionImplement is null. bizCode: [{}], extCode: [{}]",
                        bizCode, extension.getCode());
            }
            extensionJavaRunner = null;
        } else {
            extensionJavaRunner = new ExtensionJavaRunner(extension.getCode(), extImpl);
        }
        return extensionJavaRunner;
    }

    private <R> RunnerItemEntry<R> buildExtensionRunnerItemEntry(
            ExtensionSpec extension, ExtPriority config, String bizCode, String scenario) {

        boolean supportCustomization = ability.supportCustomization();

        ExtensionRunner runner = null;
        if (null == config) {
            if (log.isInfoEnabled()) {
                log.info(Message.code("LATTICE-CORE-RT-0013", extension.getCode(), bizCode).getText());
            }
            return null;
        }
        TemplateSpec template = config.getType().isVertical() ?
                getBusinessSpec(config.getCode()) : getHorizontalTemplateSpec(config.getCode());
        if (null == template) {
            return null;
        }

        if (supportCustomization) {
            if (extension.getProtocolType() == ProtocolType.REMOTE) {
                runner = buildRemoteExtensionRunner(template, extension, bizCode, scenario);
            }
            if (null == runner || extension.getProtocolType() == ProtocolType.LOCAL) {
                runner = buildLocalExtensionRunner(template, extension, bizCode, scenario);
            }
        } else {
            runner = new ExtensionJavaRunner(extension.getCode(), ability.getDefaultRealization());
        }

        if (runner != null) {
            return new RunnerItemEntry<>(ability, template, runner);
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
        if (extImpl instanceof NotExistedRealization)
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
            ExtensionInvokeCache.getInstance().doCacheExtensionRealization(cacheKey, new NotExistedRealization());
            return null;
        }
        return ExtensionInvokeCache.getInstance().doCacheExtensionRealization(cacheKey, extImpl);
    }

    public IBusinessExt findIExtensionPointsFacadeViaScenario(String scenario, TemplateSpec template, String extPointCode) {
        IBusinessExt extFacade = null;

        ITemplateCache templateCache = Lattice.getInstance().getRuntimeCache().getTemplateIndex();

        List<RealizationSpec> realizationSpecs = Lattice.getInstance().getAllRegisteredRealizations();

        if (StringUtils.isEmpty(scenario)) {
            for (RealizationSpec realization : realizationSpecs) {
                if (StringUtils.isNotEmpty(realization.getScenario())) {
                    continue;
                }
                IBusinessExt facade = realization.getBusinessExt();
                if (facade != null
                        && isCodeMatched(realization.getCode(), template.getCode())
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
                            && isCodeMatched(realization.getCode(), template.getCode())
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
                            && isCodeMatched(realization.getCode(), template.getCode())
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

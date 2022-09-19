package org.hiforce.lattice.runtime.ability;

import org.apache.commons.lang3.StringUtils;
import org.hifforce.lattice.cache.ITemplateCache;
import org.hifforce.lattice.model.ability.IBusinessExt;
import org.hifforce.lattice.model.config.BusinessConfig;
import org.hifforce.lattice.model.register.RealizationSpec;
import org.hifforce.lattice.model.template.ITemplate;
import org.hifforce.lattice.model.template.TemplateType;
import org.hiforce.lattice.runtime.Lattice;
import org.hiforce.lattice.runtime.ability.execute.ExtensionJavaRunner;
import org.hiforce.lattice.runtime.ability.execute.ExtensionRunner;
import org.hiforce.lattice.runtime.ability.execute.RunnerCollection;
import org.hiforce.lattice.runtime.cache.ExtensionInvokeCache;
import org.hiforce.lattice.runtime.cache.NotExistedExtensionPointRealization;
import org.hiforce.lattice.runtime.cache.key.ExtensionInvokeCacheKey;
import org.hiforce.lattice.runtime.session.SessionConfig;
import org.hiforce.lattice.runtime.spi.LatticeSpiFactory;
import org.hiforce.lattice.runtime.template.Template;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * @author Rocky Yu
 * @since 2022/9/16
 */
public class BaseLatticeAbilityDelegate {

    @SuppressWarnings("rawtypes")
    private final BaseLatticeAbility ability;

    @SuppressWarnings("rawtypes")
    public BaseLatticeAbilityDelegate(BaseLatticeAbility ability) {
        this.ability = ability;
    }

    public <BusinessExt extends IBusinessExt, R> RunnerCollection<BusinessExt, R> loadExtensionRunners(@Nonnull String extCode) {
        String scenario = ability.getContext().getScenario();
        String bizCode = ability.getContext().getBizObject().getBizCode();
        boolean onlyProduct = ability.getContext().isOnlyProductExt();
        BusinessConfig businessConfig = null; //TODO: 要从当前会话中裁剪出来
        if (null == businessConfig) {
            return buildDefaultRunnerCollection(extCode, null);
        }

        return null;//TODO
    }

    @SuppressWarnings("all")
    public <BusinessExt extends IBusinessExt, R> RunnerCollection<BusinessExt, R> buildDefaultRunnerCollection(
            String extCode, SessionConfig sessionConfig) {
        String bizCode = ability.getContext().getBizObject().getBizCode();
        String scenario = ability.getContext().getScenario();
        boolean loadBizExt = !ability.getContext().isOnlyProductExt();//Whether load the business's ext realization
        boolean loadDefaultExt = ability.hasDefaultExtension();//Whether load the default ext realization.


        RunnerCollection<BusinessExt, R> runnerCollection = LatticeSpiFactory.getInstance()
                .getRunnerCollectionBuilder().buildCustomRunnerCollection(
                        ability, extCode, sessionConfig);

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
            Template template = getBusinessTemplate(bizCode);

            if (ability.supportCustomization()) {
                IBusinessExt extImpl = loadExtensionRealization(bizCode, scenario, template, extensionCode);
                if (null == extImpl) {
                    extensionJavaRunner = null;
                } else {
                    int priority = -1;
                    extensionJavaRunner = new ExtensionJavaRunner(template, extensionCode, priority, extImpl);
                }
            } else {
                if (ability.hasDefaultExtension()) {
                    extensionJavaRunner = new ExtensionJavaRunner(template, extensionCode, -1, ability.getDefaultRealization());
                }
            }
            if (null == extensionJavaRunner) {
                return null;
            }
            return new RunnerCollection.RunnerItemEntry<>(template, extensionJavaRunner, ability);
        };
    }

    //TODO:
    private Template getBusinessTemplate(String bizCode) {

        Template template = new Template();
        template.setCode(bizCode);
        template.setType(TemplateType.BUSINESS);
        return template;
    }

    private IBusinessExt loadExtensionRealization(
            String bizCode, String scenario, Template template, String extPointCode) {

        //get the extension point's realization from cache. if not found, then search from the template's realization array.
        ExtensionInvokeCacheKey cacheKey = null == template ? null : new ExtensionInvokeCacheKey(scenario, template, extPointCode);

        return internalLoadExtensionRealization(cacheKey, bizCode, scenario, template, extPointCode);
    }

    private IBusinessExt internalLoadExtensionRealization(
            ExtensionInvokeCacheKey cacheKey, String bizCode, String scenario, Template template, String extPointCode) {

        IBusinessExt extImpl = ExtensionInvokeCache.getInstance().getCachedExtensionRealization(cacheKey);
        if (extImpl instanceof NotExistedExtensionPointRealization)
            return null;
        if (extImpl != null) {
            return extImpl;
        }

        if (null == template) {//无业务定制包的情况下，获取能力的默认实现
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
        //do cache the template's extension realization.
        return ExtensionInvokeCache.getInstance().doCacheExtensionRealization(cacheKey, extImpl);
    }

    public IBusinessExt findIExtensionPointsFacadeViaScenario(String scenario, ITemplate template, String extPointCode) {
        IBusinessExt extFacade = null;

        ITemplateCache templateCache = Lattice.getInstance().getLatticeRuntimeCache().getTemplateCache();

        List<RealizationSpec> realizationSpecs = Lattice.getInstance().getRegisteredRealizations();

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
}

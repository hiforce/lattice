package org.hiforce.lattice.runtime;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.hiforce.lattice.annotation.model.ProtocolType;
import org.hiforce.lattice.cache.LatticeCacheFactory;
import org.hiforce.lattice.exception.LatticeRuntimeException;
import org.hiforce.lattice.message.Message;
import org.hiforce.lattice.message.MessageCode;
import org.hiforce.lattice.model.ability.IAbility;
import org.hiforce.lattice.model.ability.IBusinessExt;
import org.hiforce.lattice.model.business.*;
import org.hiforce.lattice.model.config.*;
import org.hiforce.lattice.model.config.builder.BusinessConfigBuilder;
import org.hiforce.lattice.model.register.*;
import org.hiforce.lattice.model.scenario.ScenarioRequest;
import org.hiforce.lattice.runtime.ability.creator.DefaultAbilityCreator;
import org.hiforce.lattice.runtime.ability.delegate.BaseLatticeAbilityDelegate;
import org.hiforce.lattice.runtime.ability.register.AbilityBuildRequest;
import org.hiforce.lattice.runtime.ability.register.AbilityRegister;
import org.hiforce.lattice.runtime.ability.register.TemplateRegister;
import org.hiforce.lattice.runtime.cache.LatticeRuntimeCache;
import org.hiforce.lattice.runtime.cache.ability.AbilityCache;
import org.hiforce.lattice.runtime.cache.config.BusinessConfigCache;
import org.hiforce.lattice.runtime.spi.LatticeRuntimeSpiFactory;
import org.hiforce.lattice.runtime.utils.ClassLoaderUtil;
import org.hiforce.lattice.runtime.utils.ClassPathScanHandler;
import org.hiforce.lattice.spi.classloader.CustomClassLoaderSpi;
import org.hiforce.lattice.spi.classloader.LatticeClassLoader;
import org.hiforce.lattice.utils.BizCodeUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Rocky Yu
 * @since 2022/9/16
 */
@Slf4j
@SuppressWarnings("unused")
public class Lattice {


    private static Lattice instance;

    @Getter
    @Setter
    private String uuid = UUID.randomUUID().toString();

    @Getter
    private boolean initialized = false;

    /**
     * The business config not necessary.
     */
    @Getter
    @Setter
    private boolean simpleMode = false;

    @Getter
    @Setter
    private LatticeClassLoader latticeClassLoader;

    @Getter
    private final List<AbilitySpec> registeredAbilities = Lists.newArrayList();


    @Getter
    private final LatticeRuntimeCache runtimeCache = (LatticeRuntimeCache) LatticeCacheFactory.getInstance()
            .getRuntimeCache(new LatticeRuntimeCache());

    private Lattice() {

    }

    public AbilitySpec getAbilitySpecByCode(String code) {
        return registeredAbilities.stream()
                .filter(p -> StringUtils.equals(code, p.getCode()))
                .findFirst().orElse(null);
    }

    public static Lattice getInstance() {
        if (null == instance) {
            instance = new Lattice();
        }
        return instance;
    }

    public final void start() {
        initLatticeClassLoader();
        registerAbilities();//Register the Ability Instances during runtime.
        registerRealizations();//Register the business extension realization during runtime.
        registerBusinesses();
        registerUseCases();
        registerProducts();
        buildBusinessConfig();
        initLatticeCache();
        initialized = true;
    }

    public void initLatticeClassLoader() {
        latticeClassLoader = new LatticeClassLoader(Lattice.class.getClassLoader());
        List<CustomClassLoaderSpi> customClassLoaders =
                LatticeRuntimeSpiFactory.getInstance().getCustomClassLoaders();
        latticeClassLoader.getCustomLoaders().addAll(
                customClassLoaders.stream()
                        .map(CustomClassLoaderSpi::getCustomClassLoader)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList()));
        Thread.currentThread().setContextClassLoader(latticeClassLoader);
    }

    public final void clear() {
        runtimeCache.clear();
        registeredAbilities.clear();
        TemplateRegister.getInstance().clear();
        AbilityCache.getInstance().clear();
        initialized = false;
    }

    public void reload() {
        clear();
        start();
    }

    private void initLatticeCache() {
        getRuntimeCache().init();
        initErrorMessageCode();
        ClassPathScanHandler.clearCache();
    }

    private void initErrorMessageCode() {
        Message.clean();
        MessageCode.init();
    }

    public BusinessConfig getBusinessConfigByBizCode(String bizCode) {
        BusinessConfig config = BusinessConfigCache.getInstance().getBusinessConfigByBizCode(bizCode);
        if (null == config) {
            return null;
        }
        return new ReadonlyBusinessConfig(config.getBizCode(), config.getPriority(),
                config.getProducts(), config.getExtensions());
    }


    private void buildBusinessConfig() {
        List<String> bizCodes = Lattice.getInstance().getAllRegisteredBusinesses().stream()
                .map(BaseSpec::getCode).collect(Collectors.toList());
        List<BusinessConfig> configs = LatticeRuntimeSpiFactory.getInstance().getBusinessConfigLoads().stream()
                .flatMap(p -> p.loadBusinessConfigs(bizCodes).stream())
                .collect(Collectors.toList());
        BusinessConfigCache.getInstance().addBusinessConfigs(configs);

        if (isSimpleMode()) {
            //auto-config business and products.
            autoBuildBusinessConfig();
        }

        //非simple模式下，因为业务不会对间接依赖的扩展点做定义，所以这里要注入一下
        injectIndirectDependencyExtensions();
    }

    private void injectIndirectDependencyExtensions(){
        BusinessConfigCache.getInstance().getBusinessConfigs().forEach(p -> autoBuildUseCaseExtPriorityConfig(p, buildUseCaseExtPriorityConfigMap()));
        BusinessConfigCache.getInstance().getBusinessConfigs().sort(Comparator.comparingInt(BusinessConfig::getPriority));
    }

    private void autoBuildUseCaseExtPriorityConfig(BusinessConfig businessConfig, Map<String, ExtPriorityConfig> priorityMap) {
        priorityMap.forEach((key, value) -> {
            ExtPriorityConfig priorityConfig = businessConfig.getExtPriorityConfigByExtCode(key);
            if (null == priorityConfig) {
                businessConfig.getExtensions().add(value);
            } else {
                List<ExtPriority> newList = Lists.newArrayList();
                newList.addAll(value.getPriorities());
                newList.addAll(priorityConfig.getPriorities());
                priorityConfig.setPriorities(newList);
            }

        });
    }

    private Map<String, ExtPriorityConfig> buildUseCaseExtPriorityConfigMap() {
        Map<String, ExtPriorityConfig> extPriorityConfigMap = Maps.newHashMap();
        getAllRegisteredUseCases().forEach(p -> {
            for (RealizationSpec realizationSpec : p.getRealizations()) {
                for (String extCode : realizationSpec.getExtensionCodes()) {
                    ExtPriorityConfig config = extPriorityConfigMap.get(extCode);
                    if (null == config) {
                        config = new ExtPriorityConfig(extCode);
                        extPriorityConfigMap.put(extCode, config);
                    }
                    config.getPriorities().add(ExtPriority.of(p.getCode(), p.getType()));
                }
            }
        });
        return extPriorityConfigMap;
    }

    public void addBusinessConfig(BusinessConfig config) {
        if (null == config) {
            throw new LatticeRuntimeException("LATTICE-CORE-RT-0015");
        }

        Message message = checkBusinessConfig(config);
        if (null != message) {
            throw new LatticeRuntimeException(message);
        }

        BusinessConfigCache.getInstance().getBusinessConfigs().stream()
                .filter(p -> StringUtils.equals(config.getBizCode(), p.getBizCode()))
                .findFirst().ifPresent(BusinessConfigCache.getInstance().getBusinessConfigs()::remove);
        BusinessConfigCache.getInstance().getBusinessConfigs().add(config);
    }

    private Message checkBusinessConfig(BusinessConfig config) {
        if (StringUtils.isEmpty(config.getBizCode())) {
            return Message.code("LATTICE-CORE-RT-0014");
        }
        return null;
    }

    public BusinessConfig autoAddAndBuildBusinessConfig(BusinessSpec businessSpec, ProtocolType protocolType) {
        List<ProductConfig> productConfigs = getAllRegisteredProducts().stream()
                .map(this::buildProductConfig)
                .collect(Collectors.toList());

        BusinessConfig businessConfig = BusinessConfigCache.getInstance().getBusinessConfigs().stream()
                .filter(p -> StringUtils.equals(p.getBizCode(), businessSpec.getCode()))
                .findFirst().orElse(null);
        if (null != businessConfig && protocolType == ProtocolType.LOCAL) {
            return businessConfig;
        }

        // In remote mode, since ExtPriorityConfig is calculated at runtime, the cache is not read for the first time
        if (null != businessConfig && BaseLatticeAbilityDelegate.remoteExtensionInitFlag) {
            return businessConfig;
        }

        List<ExtPriorityConfig> priorityConfigs = businessSpec.getRealizations().stream()
                .flatMap(p -> autoBuildPriorityConfig(businessSpec, p).stream())
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        businessConfig = BusinessConfigBuilder.builder()
                .priority(businessSpec.getPriority())
                .bizCode(businessSpec.getCode())
                .install(productConfigs)
                .extension(priorityConfigs)
                .build();
        businessConfig.setAutoBuild(true);

        // Remote mode, clear the cache information during engine startup, replace it at runtime
        if (protocolType == ProtocolType.REMOTE) {
            BusinessConfigCache.getInstance().removeBusinessConfig(businessConfig.getBizCode());
        }
        BusinessConfigCache.getInstance().getBusinessConfigs().add(businessConfig);
        
        return businessConfig;
    }

    public List<BusinessConfig> getBusinessConfigs() {
        return BusinessConfigCache.getInstance().getBusinessConfigs();
    }

    private void autoBuildBusinessConfig() {
        for (BusinessSpec businessSpec : getAllRegisteredBusinesses()) {
            autoAddAndBuildBusinessConfig(businessSpec, ProtocolType.LOCAL);
        }
    }

    private void autoMakeupPriorityConfig(BusinessConfig businessConfig, List<ProductSpec> products) {
        Map<String, ExtPriorityConfig> priorityConfigHashMap = Maps.newHashMap();
        for (ProductSpec spec : products) {
            List<String> extCodes = spec.getRealizations().stream()
                    .flatMap(p -> p.getExtensionCodes().stream())
                    .filter(businessConfig::notContainExtCode)
                    .collect(Collectors.toList());
            if (CollectionUtils.isEmpty(extCodes)) {
                continue;
            }
            for (String extCode : extCodes) {
                ExtPriorityConfig priorityConfig = priorityConfigHashMap.get(extCode);
                if (null == priorityConfig) {
                    priorityConfig = new ExtPriorityConfig();
                    priorityConfig.setExtCode(extCode);
                    priorityConfigHashMap.put(extCode, priorityConfig);
                }
                priorityConfig.getPriorities().add(ExtPriority.of(spec.getCode(), TemplateType.PRODUCT));
            }
        }
        priorityConfigHashMap.values().forEach(p -> businessConfig.getExtensions().add(p));
    }

    private List<ExtPriorityConfig> autoBuildPriorityConfig(BusinessSpec business, RealizationSpec realization) {
        List<ExtPriorityConfig> configs = Lists.newArrayList();


        for (String extCode : realization.getExtensionCodes()) {
            List<ProductSpec> products = getAllRegisteredProducts().stream()
                    .filter(p -> p.getRealizations().stream().
                            anyMatch(real -> real.getExtensionCodes().contains(extCode)))
                    .collect(Collectors.toList());
            ExtPriorityConfig config = new ExtPriorityConfig();
            config.setExtCode(extCode);
            config.getPriorities().add(ExtPriority.of(business.getCode(), TemplateType.BUSINESS));
            configs.add(config);

            if (CollectionUtils.isEmpty(products)) {
                continue;
            }
            config.getPriorities().addAll(0, products.stream()
                    .map(this::buildExtPriority).collect(Collectors.toList()));
        }
        return configs;
    }

    private ExtPriority buildExtPriority(ProductSpec productSpec) {
        return ExtPriority.of(productSpec.getCode(), TemplateType.PRODUCT);
    }

    private ProductConfig buildProductConfig(ProductSpec productSpec) {
        return ProductConfig.of(productSpec.getCode());
    }


    public Collection<AbilitySpec> getAllRegisteredAbilities() {
        return getRuntimeCache().getAbilityCache().getAllCachedAbilities();
    }

    public List<UseCaseSpec> getAllRegisteredUseCases() {
        return TemplateRegister.getInstance().getUseCases();
    }

    public List<ProductSpec> getAllRegisteredProducts() {
        return TemplateRegister.getInstance().getProducts();
    }

    public List<BusinessSpec> getAllRegisteredBusinesses() {
        return TemplateRegister.getInstance().getBusinesses();
    }

    public List<RealizationSpec> getAllRegisteredRealizations() {
        return TemplateRegister.getInstance().getRealizations();
    }

    public BusinessSpec getRegisteredBusinessByCode(String code) {
        return TemplateRegister.getInstance().getBusinesses().stream()
                .filter(p -> StringUtils.equals(code, p.getCode()))
                .findFirst().orElse(null);
    }

    @SuppressWarnings("rawtypes")
    public TemplateSpec getTemplateSpec(String code, TemplateType type) {
        if (type == TemplateType.BUSINESS) {
            return getRegisteredBusinessByCode(code);
        } else if (type == TemplateType.PRODUCT) {
            return getRegisteredProductByCode(code);
        } else if (type == TemplateType.USE_CASE) {
            return getRegisteredUseCaseByCode(code);
        }
        return null;
    }

    public ProductSpec getRegisteredProductByCode(String code) {
        return TemplateRegister.getInstance().getProducts().stream()
                .filter(p -> StringUtils.equals(code, p.getCode()))
                .findFirst().orElse(null);
    }

    public UseCaseSpec getRegisteredUseCaseByCode(String code) {
        return TemplateRegister.getInstance().getUseCases().stream()
                .filter(p -> StringUtils.equals(code, p.getCode()))
                .findFirst().orElse(null);
    }

    public static List<String> getServiceProviderValues(String spiClassName, ClassLoader originLoader) {

        List<String> classNames = Lists.newArrayList();

        try {
            List<ClassLoader> classLoaders = Lists.newArrayList(
                    null == originLoader ? Thread.currentThread().getContextClassLoader() : originLoader);
            for (ClassLoader classLoader : classLoaders) {
                Enumeration<URL> enumeration = classLoader.getResources("META-INF/services/" + spiClassName);
                while (enumeration.hasMoreElements()) {
                    URL url = enumeration.nextElement();
                    classNames.addAll(loadSpiFileContent(url));
                }
            }
        } catch (IOException e) {
            log.warn(e.getMessage(), e);
        }
        return classNames;
    }

    private static Class<?> loadClass(String className, ClassLoader classLoader) {
        if (StringUtils.isEmpty(className)) {
            return null;
        }
        try {
            return classLoader.loadClass(className);
        } catch (ClassNotFoundException e) {
            return null;
        }
    }

    @SuppressWarnings("rawtypes")
    public static Set<Class> getServiceProviderClasses(String spiClassName, ClassLoader classLoader) {
        ClassLoader originLoader = Lattice.getInstance().getLatticeClassLoader();
        List<String> classNames = getServiceProviderValues(spiClassName, originLoader);
        return classNames.stream().filter(StringUtils::isNotEmpty)
                .map(p -> loadClass(p, classLoader))
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    @SuppressWarnings("all")
    public static Set<Class> getServiceProviderClasses(String spiClassName) {
        ClassLoader originLoader = Lattice.getInstance().getLatticeClassLoader();
        ClassLoader classLoader = null == originLoader ? Thread.currentThread().getContextClassLoader() : originLoader;
        return getServiceProviderClasses(spiClassName, classLoader);
    }

    private static List<String> loadSpiFileContent(URL url) {
        List<String> contentList = new ArrayList<>();
        try (BufferedReader bufferedReader =
                     new BufferedReader(new InputStreamReader(url.openStream(), StandardCharsets.UTF_8))) {
            String eachLine;
            while (StringUtils.isNotBlank(eachLine = bufferedReader.readLine())) {
                eachLine = eachLine.trim();
                contentList.add(eachLine);
            }
        } catch (Exception e) {
            log.warn(e.getMessage(), e);
        }
        return contentList;
    }

    @SuppressWarnings("rawtypes")
    private void registerRealizations() {
        Set<Class> classSet = getServiceProviderClasses(IBusinessExt.class.getName());
        TemplateRegister.getInstance().registerRealizations(classSet);
    }

    @SuppressWarnings("rawtypes")
    private void registerAbilities() {
        Set<Class> abilityClasses = getServiceProviderClasses(IAbility.class.getName());
        registeredAbilities.addAll(AbilityRegister.getInstance()
                .register(new AbilityBuildRequest(null, mergeAbilityInstancePackage(abilityClasses))));
    }

    @SuppressWarnings("rawtypes")
    private void registerBusinesses() {
        Set<Class> classSet = getServiceProviderClasses(IBusiness.class.getName());
        TemplateRegister.getInstance().registerBusinesses(classSet);
    }

    @SuppressWarnings("rawtypes")
    private void registerProducts() {
        Set<Class> classSet = getServiceProviderClasses(IProduct.class.getName());
        TemplateRegister.getInstance().registerProducts(classSet);
    }

    @SuppressWarnings("rawtypes")
    private void registerUseCases() {
        Set<Class> classSet = getServiceProviderClasses(IUseCase.class.getName());
        TemplateRegister.getInstance().registerUseCases(classSet);
    }

    @SuppressWarnings("rawtypes")
    private Set<Class> mergeAbilityInstancePackage(Set<Class> abilityClasses) {
        Set<Class> classesSet = Sets.newHashSet(abilityClasses);
        Set<String> packageSet = abilityClasses.stream().map(p -> p.getPackage().getName()).collect(Collectors.toSet());
        for (String pkg : packageSet) {
            classesSet.addAll(ClassLoaderUtil.scanLatticeClasses(pkg));
        }
        return classesSet;
    }

    public RealizationSpec getRealizationSpecByCode(String code) {
        return TemplateRegister.getInstance().getRealizations()
                .stream().filter(p -> BizCodeUtils.isCodesMatched(p.getCode(), code))
                .findFirst().orElse(null);
    }

    public List<RealizationSpec> getAllRealizations() {
        return TemplateRegister.getInstance().getRealizations();
    }


    public BusinessTemplate getFirstMatchedBusiness(ScenarioRequest request) {
        return TemplateRegister.getInstance().getFirstMatchedBusiness(request);
    }

    @SuppressWarnings("all")
    public static <Ability extends IAbility> List<Ability> getAllAbilities(Class<?> abilityClass, IBizObject target) {
        if( null == abilityClass ){
            throw new LatticeRuntimeException("LATTICE-CORE-RT-0025");
        }
        return getAllAbilities(abilityClass.getName(), target);
    }

    @SuppressWarnings("all")
    public static <Ability extends IAbility> List<Ability> getAllAbilities(String abilityCode, IBizObject target) {
        DefaultAbilityCreator creator = new DefaultAbilityCreator(abilityCode, target);
        List<Ability> abilityList = creator.getAllAbilityInstancesWithCache();

        if (abilityList.isEmpty()) {
            return Collections.emptyList();
        }
        List<Ability> abilities = new ArrayList<>(abilityList.size());
        for (Ability ability : abilityList) {
            boolean supportChecking = ability.supportChecking();
            if (supportChecking) {
                abilities.add(ability);
            }
        }
        return abilities;
    }

    @SuppressWarnings("all")
    public static <Ability extends IAbility> Ability getFirstMatchedAbility(Class<?> abilityClass, IBizObject target) {
        if( null == abilityClass ){
            throw new LatticeRuntimeException("LATTICE-CORE-RT-0025");
        }
        return getFirstMatchedAbility(abilityClass.getName(), target);
    }
    @SuppressWarnings("all")
    public static <Ability extends IAbility> Ability getFirstMatchedAbility(String abilityCode, IBizObject target) {
        DefaultAbilityCreator creator = new DefaultAbilityCreator(abilityCode, target);
        List<Ability> domainAbilitySet = creator.getAllAbilityInstancesWithCache();
        if (domainAbilitySet.isEmpty()) {
            return null;
        }

        for (Ability ability : domainAbilitySet) {
            boolean supportChecking = ability.supportChecking();
            if (supportChecking) {
                return ability;
            }
        }
        return null;
    }


}

package org.hiforce.lattice.runtime;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.hifforce.lattice.cache.LatticeCacheFactory;
import org.hifforce.lattice.message.Message;
import org.hifforce.lattice.message.MessageCode;
import org.hifforce.lattice.model.ability.IAbility;
import org.hifforce.lattice.model.ability.IBusinessExt;
import org.hifforce.lattice.model.ability.provider.IAbilityProvider;
import org.hifforce.lattice.model.business.IBusiness;
import org.hifforce.lattice.model.business.IProduct;
import org.hifforce.lattice.model.register.AbilitySpec;
import org.hifforce.lattice.model.register.BusinessSpec;
import org.hifforce.lattice.model.register.ProductSpec;
import org.hifforce.lattice.model.register.RealizationSpec;
import org.hifforce.lattice.utils.BizCodeUtils;
import org.hiforce.lattice.runtime.ability.register.AbilityBuildRequest;
import org.hiforce.lattice.runtime.ability.register.AbilityRegister;
import org.hiforce.lattice.runtime.ability.register.TemplateRegister;
import org.hiforce.lattice.runtime.cache.LatticeRuntimeCache;
import org.hiforce.lattice.runtime.spi.LatticeSpiFactory;
import org.hiforce.lattice.runtime.template.LatticeTemplateManager;
import org.hiforce.lattice.runtime.utils.ClassLoaderUtil;
import org.hiforce.lattice.runtime.utils.ClassPathScanHandler;

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
    private final LatticeTemplateManager templateManager = new LatticeTemplateManager();

    @Getter
    private final List<AbilitySpec> registeredAbilities = Lists.newArrayList();


    @Getter
    private final LatticeRuntimeCache latticeRuntimeCache = (LatticeRuntimeCache) LatticeCacheFactory.getInstance()
            .getRuntimeCache(new LatticeRuntimeCache());

    @Getter
    @SuppressWarnings("all")
    private final IAbilityProvider abilityProvider = LatticeSpiFactory.getInstance()
            .getAbilityProviderCreator().createAbilityProvider();

    private Lattice() {

    }

    public static Lattice getInstance() {
        if (null == instance) {
            instance = new Lattice();
        }
        return instance;
    }

    public final void start() {
        registerAbilities();//Register the Ability Instances during runtime.
        registerRealizations();//Register the business extension realization during runtime.
        registerBusinesses();
        registerProducts();
        buildBusinessConfig();


        MessageCode.init();
        Message.clean();
        ClassPathScanHandler.clearCache();
        initialized = true;
    }


    private void buildBusinessConfig() {
        if (isSimpleMode()) {

        }
    }


    public Collection<AbilitySpec> getAllRegisteredAbilities() {
        return Lattice.getInstance().getLatticeRuntimeCache().getAllCachedAbilities();
    }

    public List<ProductSpec> getAllRegisteredProducts() {
        return TemplateRegister.getInstance().getProducts();
    }

    public List<BusinessSpec> getAllRegisteredBusinesses(){
        return TemplateRegister.getInstance().getBusinesses();
    }

    public List<RealizationSpec> getAllRegisteredRealizations() {
        return TemplateRegister.getInstance().getRealizations();
    }

    public BusinessSpec getRegisteredBusinessByCode(String code){
        return TemplateRegister.getInstance().getBusinesses().stream()
                .filter(p -> StringUtils.equals(code, p.getCode()))
                .findFirst().orElse(null);
    }

    public ProductSpec getRegisteredProductByCode(String code) {
        return TemplateRegister.getInstance().getProducts().stream()
                .filter(p -> StringUtils.equals(code, p.getCode()))
                .findFirst().orElse(null);
    }


    @SuppressWarnings("rawtypes")
    public static Set<Class> getServiceProviderClasses(String spiClassName) {
        Set<Class> classList = Sets.newHashSet();
        try {
            List<ClassLoader> classLoaders = Lists.newArrayList(Thread.currentThread().getContextClassLoader());
            for (ClassLoader classLoader : classLoaders) {
                Enumeration<URL> enumeration = classLoader.getResources("META-INF/services/" + spiClassName);
                while (enumeration.hasMoreElements()) {
                    URL url = enumeration.nextElement();
                    List<String> classNames = loadSpiFileContent(url);
                    for (String className : classNames) {
                        try {
                            classList.add(Class.forName(className));
                        } catch (Exception e) {
                            log.warn(e.getMessage(), e);
                        }
                    }
                }
            }
        } catch (IOException e) {
            log.warn(e.getMessage(), e);
        }
        return classList;
    }

    private static List<String> loadSpiFileContent(URL url) {
        List<String> contentList = new ArrayList<>();
        try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(url.openStream(), StandardCharsets.UTF_8))) {
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
}

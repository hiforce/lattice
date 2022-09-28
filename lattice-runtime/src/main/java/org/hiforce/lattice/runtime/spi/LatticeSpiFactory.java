package org.hiforce.lattice.runtime.spi;

import com.google.common.collect.Lists;
import org.apache.commons.collections4.CollectionUtils;
import org.hifforce.lattice.model.ability.IAbility;
import org.hifforce.lattice.spi.annotation.*;
import org.hifforce.lattice.spi.config.BusinessConfigLoadSpi;
import org.hiforce.lattice.runtime.ability.execute.RunnerCollection;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * @author Rocky Yu
 * @since 2022/9/15
 */
@SuppressWarnings("rawtypes")
public class LatticeSpiFactory {

    private static volatile LatticeSpiFactory instance;

    private static ClassLoader classLoader;

    @SuppressWarnings("rawtypes")
    private List<AbilityAnnotationParser> abilityAnnotationParsers;

    @SuppressWarnings("rawtypes")
    private List<ExtensionAnnotationParser> extensionAnnotationParsers;

    @SuppressWarnings("rawtypes")
    private List<ScanSkipAnnotationParser> scanSkipAnnotationParsers;

    @SuppressWarnings("rawtypes")
    private List<RealizationAnnotationParser> realizationAnnotationParsers;

    private List<ProductAnnotationParser> productAnnotationParsers;

    private List<BusinessAnnotationParser> businessAnnotationParsers;

    private List<UseCaseAnnotationParser> useCaseAnnotationParsers;

    private IRunnerCollectionBuilder runnerCollectionBuilder;


    private List<BusinessConfigLoadSpi> businessConfigLoads;

    private LatticeSpiFactory() {

    }

    public static LatticeSpiFactory getInstance() {
        if (null == instance) {
            synchronized (LatticeSpiFactory.class) {
                if (null == instance) {
                    instance = new LatticeSpiFactory();
                    classLoader = LatticeSpiFactory.class.getClassLoader();
                }
            }
        }
        return instance;
    }

    public List<BusinessConfigLoadSpi> getBusinessConfigLoads() {
        if (null == businessConfigLoads) {
            businessConfigLoads = getCustomServiceProviders(BusinessConfigLoadSpi.class);
        }
        if (CollectionUtils.isNotEmpty(businessConfigLoads)) {
            businessConfigLoads.forEach(p -> p.setClassLoader(getClassLoader()));
            businessConfigLoads.sort(Comparator.comparingInt(BusinessConfigLoadSpi::getPriority));
        }
        return businessConfigLoads;
    }

    /**
     * @return The Ability's Custom Annotation Parsers..
     */
    @SuppressWarnings("rawtypes")
    public List<AbilityAnnotationParser> getAbilityAnnotationParsers() {
        if (null == abilityAnnotationParsers) {
            abilityAnnotationParsers = getCustomServiceProviders(AbilityAnnotationParser.class);
        }
        return abilityAnnotationParsers;
    }

    public List<ProductAnnotationParser> getProductAnnotationParsers() {
        if (null == productAnnotationParsers) {
            productAnnotationParsers = getCustomServiceProviders(ProductAnnotationParser.class);
        }
        return productAnnotationParsers;
    }

    public List<UseCaseAnnotationParser> getUseCaseAnnotationParsers() {
        if (null == useCaseAnnotationParsers) {
            useCaseAnnotationParsers = getCustomServiceProviders(UseCaseAnnotationParser.class);
        }
        return useCaseAnnotationParsers;
    }

    public List<BusinessAnnotationParser> getBusinessAnnotationParsers() {
        if (null == businessAnnotationParsers) {
            businessAnnotationParsers = getCustomServiceProviders(BusinessAnnotationParser.class);
        }
        return businessAnnotationParsers;
    }

    @SuppressWarnings("rawtypes")
    public List<ExtensionAnnotationParser> getExtensionAnnotationParsers() {
        if (null == extensionAnnotationParsers) {
            extensionAnnotationParsers =
                    getCustomServiceProviders(ExtensionAnnotationParser.class);
        }
        return extensionAnnotationParsers;
    }

    @SuppressWarnings("rawtypes")
    public List<RealizationAnnotationParser> getRealizationAnnotationParsers() {
        if (null == realizationAnnotationParsers) {
            realizationAnnotationParsers =
                    getCustomServiceProviders(RealizationAnnotationParser.class);
        }
        return realizationAnnotationParsers;
    }

    @SuppressWarnings("rawtypes")
    public List<ScanSkipAnnotationParser> getScanSkipAnnotationParsers() {
        if (null == scanSkipAnnotationParsers) {
            scanSkipAnnotationParsers = getCustomServiceProviders(ScanSkipAnnotationParser.class);
        }
        return scanSkipAnnotationParsers;
    }

    public <T> List<T> getCustomServiceProviders(Class<T> spiClass) {

        ServiceLoader<T> serializers;
        serializers = ServiceLoader.load(spiClass, classLoader);
        return StreamSupport.stream(serializers.spliterator(), false)
                .distinct().collect(Collectors.toList());
    }

    private ClassLoader getClassLoader() {
        return Thread.currentThread().getContextClassLoader();//TODO: 未来可以增加自定义ClassLoader
    }

    public IRunnerCollectionBuilder getRunnerCollectionBuilder() {
        if (null != runnerCollectionBuilder) {
            return runnerCollectionBuilder;
        }
        synchronized (LatticeSpiFactory.class) {
            if (null == runnerCollectionBuilder) {
                ServiceLoader<IRunnerCollectionBuilder> serializers =
                        ServiceLoader.load(IRunnerCollectionBuilder.class, classLoader);
                final Optional<IRunnerCollectionBuilder> serializer =
                        StreamSupport.stream(serializers.spliterator(), false)
                                .findFirst();
                runnerCollectionBuilder = serializer.orElse(new IRunnerCollectionBuilder() {

                    @Override
                    public boolean isSupport(IAbility ability, String extensionCode) {
                        return false;
                    }

                    @Override
                    @SuppressWarnings("all")
                    public RunnerCollection buildCustomRunnerCollection(IAbility ability, String extensionCode) {
                        return RunnerCollection.of(ability.getContext().getBizObject(),
                                Lists.newArrayList(), RunnerCollection.ACCEPT_ALL);
                    }
                });
            }
        }
        return runnerCollectionBuilder;
    }
}

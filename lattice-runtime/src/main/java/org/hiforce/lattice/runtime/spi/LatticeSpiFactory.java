package org.hiforce.lattice.runtime.spi;

import com.google.common.collect.Lists;
import org.hifforce.lattice.annotation.parser.*;
import org.hifforce.lattice.model.ability.provider.IAbilityProviderCreator;
import org.hiforce.lattice.runtime.ability.BaseLatticeAbility;
import org.hiforce.lattice.runtime.ability.execute.IRunnerCollectionBuilder;
import org.hiforce.lattice.runtime.ability.execute.RunnerCollection;
import org.hiforce.lattice.runtime.ability.provider.DefaultAbilityProviderCreator;
import org.hiforce.lattice.runtime.session.SessionConfig;

import java.util.*;
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

    private IAbilityProviderCreator abilityProviderCreator;

    private IRunnerCollectionBuilder runnerCollectionBuilder;

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

    /**
     * @return The Ability's Custom Annotation Parsers..
     */
    @SuppressWarnings("rawtypes")
    public List<AbilityAnnotationParser> getAbilityAnnotationParsers() {
        if (null == abilityAnnotationParsers) {
            abilityAnnotationParsers = getCustomAnnotationParsers(AbilityAnnotationParser.class);
        }
        return abilityAnnotationParsers;
    }

    public List<ProductAnnotationParser> getProductAnnotationParsers() {
        if (null == productAnnotationParsers) {
            productAnnotationParsers = getCustomAnnotationParsers(ProductAnnotationParser.class);
        }
        return productAnnotationParsers;
    }

    @SuppressWarnings("rawtypes")
    public List<ExtensionAnnotationParser> getExtensionAnnotationParsers() {
        if (null == extensionAnnotationParsers) {
            extensionAnnotationParsers =
                    getCustomAnnotationParsers(ExtensionAnnotationParser.class);
        }
        return extensionAnnotationParsers;
    }

    @SuppressWarnings("rawtypes")
    public List<RealizationAnnotationParser> getRealizationAnnotationParsers() {
        if (null == realizationAnnotationParsers) {
            realizationAnnotationParsers =
                    getCustomAnnotationParsers(RealizationAnnotationParser.class);
        }
        return realizationAnnotationParsers;
    }

    @SuppressWarnings("rawtypes")
    public List<ScanSkipAnnotationParser> getScanSkipAnnotationParsers() {
        if (null == scanSkipAnnotationParsers) {
            scanSkipAnnotationParsers = getCustomAnnotationParsers(ScanSkipAnnotationParser.class);
        }
        return scanSkipAnnotationParsers;
    }

    public <T> List<T> getCustomAnnotationParsers(Class<T> spiClass) {

        ServiceLoader<T> serializers;
        Set<T> result = new HashSet<>();
        boolean supportContainer = false;//TODO：为未来容器化的自定义ClassLoader预留，可以自定义ClassLoader
        if (supportContainer) {
            serializers = ServiceLoader.load(spiClass, getClassLoader());
            Set<T> containerSets = StreamSupport.stream(serializers.spliterator(), false)
                    .collect(Collectors.toSet());
            result.addAll(containerSets);
        }
        serializers = ServiceLoader.load(spiClass, classLoader);
        Set<T> platformSets = StreamSupport.stream(serializers.spliterator(), false)
                .collect(Collectors.toSet());
        result.addAll(platformSets);
        return new ArrayList<T>(result);
    }

    public IAbilityProviderCreator getAbilityProviderCreator() {
        if (null != abilityProviderCreator) {
            return abilityProviderCreator;
        }
        synchronized (LatticeSpiFactory.class) {
            if (null == abilityProviderCreator) {
                ServiceLoader<IAbilityProviderCreator> serializers = ServiceLoader.load(IAbilityProviderCreator.class, classLoader);
                final Optional<IAbilityProviderCreator> serializer = StreamSupport.stream(serializers.spliterator(), false)
                        .findFirst();
                abilityProviderCreator = serializer.orElse(new DefaultAbilityProviderCreator());
            }
        }
        return abilityProviderCreator;
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
                    public boolean isSupport(BaseLatticeAbility ability, String extensionCode, SessionConfig sessionConfig) {
                        return false;
                    }

                    @Override
                    public RunnerCollection buildCustomRunnerCollection(BaseLatticeAbility ability, String extensionCode, SessionConfig sessionConfig) {
                        return RunnerCollection.of(ability.getContext().getBizObject(), Lists.newArrayList(), RunnerCollection.ACCEPT_ALL);
                    }
                });
            }
        }
        return runnerCollectionBuilder;
    }
}

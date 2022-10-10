package org.hiforce.lattice.spi;

import org.hiforce.lattice.spi.annotation.*;

import java.util.List;
import java.util.ServiceLoader;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * @author Rocky Yu
 * @since 2022/9/30
 */
@SuppressWarnings("all")
public class LatticeAnnotationSpiFactory {

    private static LatticeAnnotationSpiFactory instance;

    private static ClassLoader classLoader;

    @SuppressWarnings("rawtypes")
    private List<ExtensionAnnotationParser> extensionAnnotationParsers;

    @SuppressWarnings("rawtypes")
    private List<AbilityAnnotationParser> abilityAnnotationParsers;

    @SuppressWarnings("rawtypes")
    private List<ScanSkipAnnotationParser> scanSkipAnnotationParsers;

    @SuppressWarnings("rawtypes")
    private List<RealizationAnnotationParser> realizationAnnotationParsers;

    private List<ProductAnnotationParser> productAnnotationParsers;

    private List<BusinessAnnotationParser> businessAnnotationParsers;

    private List<UseCaseAnnotationParser> useCaseAnnotationParsers;

    private LatticeAnnotationSpiFactory() {

    }

    public static LatticeAnnotationSpiFactory getInstance() {
        if (null == instance) {
            instance = new LatticeAnnotationSpiFactory();
            classLoader = LatticeAnnotationSpiFactory.class.getClassLoader();
        }
        return instance;
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

    public List<ProductAnnotationParser> getProductAnnotationParsers() {
        if (null == productAnnotationParsers) {
            productAnnotationParsers = getCustomServiceProviders(ProductAnnotationParser.class);
        }
        return productAnnotationParsers;
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
}

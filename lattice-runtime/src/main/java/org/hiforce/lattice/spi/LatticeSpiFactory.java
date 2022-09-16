package org.hiforce.lattice.spi;

import org.hifforce.lattice.annotation.parser.AbilityAnnotationParser;
import org.hifforce.lattice.annotation.parser.ExtensionAnnotationParser;
import org.hifforce.lattice.annotation.parser.ScanSkipAnnotationParser;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * @author Rocky Yu
 * @since 2022/9/15
 */
public class LatticeSpiFactory {

    private static volatile LatticeSpiFactory instance;

    private static ClassLoader classLoader;

    private List<AbilityAnnotationParser> abilityAnnotationParsers;

    private List<ExtensionAnnotationParser> extensionAnnotationParsers;

    private List<ScanSkipAnnotationParser> scanSkipAnnotationParsers;

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
    public List<AbilityAnnotationParser> getAbilityAnnotationParsers() {
        if (null == abilityAnnotationParsers) {
            abilityAnnotationParsers = getCustomAnnotationParsers(AbilityAnnotationParser.class, false);
        }
        return abilityAnnotationParsers;
    }

    public List<ExtensionAnnotationParser> getExtensionAnnotationParsers() {
        if (null == extensionAnnotationParsers) {
            extensionAnnotationParsers =
                    getCustomAnnotationParsers(ExtensionAnnotationParser.class, false);
        }
        return extensionAnnotationParsers;
    }

    public List<ScanSkipAnnotationParser> getScanSkipAnnotationParsers() {
        if (null == scanSkipAnnotationParsers) {
            scanSkipAnnotationParsers = getCustomAnnotationParsers(ScanSkipAnnotationParser.class, true);
        }
        return scanSkipAnnotationParsers;
    }

    public <T> List<T> getCustomAnnotationParsers(Class<T> spiClass, boolean supportContainer) {

        ServiceLoader<T> serializers;
        Set<T> result = new HashSet<>();
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

    private ClassLoader getClassLoader() {
        return Thread.currentThread().getContextClassLoader();
    }
}

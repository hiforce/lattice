package org.hiforce.lattice.runtime.spi;

import com.google.common.collect.Lists;
import org.apache.commons.collections4.CollectionUtils;
import org.hiforce.lattice.model.ability.IAbility;
import org.hiforce.lattice.spi.classloader.CustomClassLoaderSpi;
import org.hiforce.lattice.spi.config.BusinessConfigLoadSpi;
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
public class LatticeRuntimeSpiFactory {

    private static volatile LatticeRuntimeSpiFactory instance;


    private IRunnerCollectionBuilder runnerCollectionBuilder;


    private List<BusinessConfigLoadSpi> businessConfigLoads;


    private List<CustomClassLoaderSpi> customClassLoaders;

    private LatticeRuntimeSpiFactory() {

    }

    public static LatticeRuntimeSpiFactory getInstance() {
        if (null == instance) {
            synchronized (LatticeRuntimeSpiFactory.class) {
                if (null == instance) {
                    instance = new LatticeRuntimeSpiFactory();
                }
            }
        }
        return instance;
    }

    public List<CustomClassLoaderSpi> getCustomClassLoaders() {
        if (null == customClassLoaders) {
            customClassLoaders = getCustomServiceProviders(CustomClassLoaderSpi.class);
        }
        return customClassLoaders;
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

    public <T> List<T> getCustomServiceProviders(Class<T> spiClass) {

        ServiceLoader<T> serializers;
        serializers = ServiceLoader.load(spiClass, getClassLoader());
        return StreamSupport.stream(serializers.spliterator(), false)
                .distinct().collect(Collectors.toList());
    }

    private ClassLoader getClassLoader() {
        return Thread.currentThread().getContextClassLoader();
    }

    public IRunnerCollectionBuilder getRunnerCollectionBuilder() {
        if (null != runnerCollectionBuilder) {
            return runnerCollectionBuilder;
        }
        synchronized (LatticeRuntimeSpiFactory.class) {
            if (null == runnerCollectionBuilder) {
                ServiceLoader<IRunnerCollectionBuilder> serializers =
                        ServiceLoader.load(IRunnerCollectionBuilder.class, getClassLoader());
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

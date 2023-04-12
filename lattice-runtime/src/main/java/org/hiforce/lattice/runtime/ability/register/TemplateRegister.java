package org.hiforce.lattice.runtime.ability.register;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.hiforce.lattice.annotation.model.*;
import org.hiforce.lattice.exception.LatticeRuntimeException;
import org.hiforce.lattice.model.ability.IBusinessExt;
import org.hiforce.lattice.model.business.BusinessTemplate;
import org.hiforce.lattice.model.register.*;
import org.hiforce.lattice.model.scenario.ScenarioRequest;
import org.hiforce.lattice.runtime.cache.index.TemplateIndex;
import org.hiforce.lattice.utils.BizCodeUtils;
import org.hiforce.lattice.utils.BusinessExtUtils;

import java.lang.reflect.Method;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.hiforce.lattice.utils.LatticeAnnotationUtils.*;

/**
 * @author Rocky Yu
 * @since 2022/9/18
 */
public class TemplateRegister {

    private static TemplateRegister instance;

    @Getter
    private final List<RealizationSpec> realizations = Lists.newArrayList();

    @Getter
    private final List<ProductSpec> products = Lists.newArrayList();

    @Getter
    private final List<UseCaseSpec> useCases = Lists.newArrayList();

    @Getter
    private final List<BusinessSpec> businesses = Lists.newArrayList();

    private TemplateRegister() {

    }

    public static TemplateRegister getInstance() {
        if (null == instance) {
            instance = new TemplateRegister();
        }
        return instance;
    }

    public BusinessTemplate getFirstMatchedBusiness(ScenarioRequest request) {
        return businesses.stream()
                .map(BusinessSpec::newInstance)
                .filter(p -> p.isEffect(request))
                .findFirst().orElse(null);
    }

    @SuppressWarnings("rawtypes")
    public synchronized List<BusinessSpec> registerBusinesses(Set<Class> classSet) {
        List<BusinessSpec> businessSpecs = Lists.newArrayList();
        synchronized (TemplateRegister.class) {
            for (Class clz : classSet) {
                BusinessAnnotation annotation = getBusinessAnnotation(clz);
                if (null == annotation) {
                    continue;
                }
                BusinessSpec businessSpec = new BusinessSpec();
                businessSpec.setBusinessClass(clz);
                businessSpec.setCode(annotation.getCode());
                businessSpec.setName(annotation.getName());
                businessSpec.setDescription(annotation.getDesc());
                businessSpec.setPriority(annotation.getPriority());
                businessSpec.getRealizations().addAll(realizations.stream()
                        .filter(p -> BizCodeUtils.isCodesMatched(p.getCode(), businessSpec.getCode()))
                        .collect(Collectors.toList()));
                TemplateIndex.getInstance().addTemplateIndex(businessSpec);
                businesses.add(businessSpec);
                businessSpecs.add(businessSpec);
            }
        }
        return businessSpecs;
    }

    @SuppressWarnings("rawtypes")
    public synchronized List<UseCaseSpec> registerUseCases(Set<Class> classSet) {
        List<UseCaseSpec> useCaseSpecs = Lists.newArrayList();
        synchronized (TemplateRegister.class) {
            for (Class clz : classSet) {
                UseCaseAnnotation annotation = getUseCaseAnnotation(clz);
                if (null == annotation) {
                    continue;
                }
                UseCaseSpec spec = new UseCaseSpec();
                spec.setUseCaseClass(clz);
                spec.setCode(annotation.getCode());
                spec.setName(annotation.getName());
                spec.setDescription(annotation.getDesc());
                spec.setPriority(annotation.getPriority());
                spec.setSdk(annotation.getSdk());

                spec.getRealizations().addAll(realizations.stream()
                        .filter(p -> StringUtils.equals(p.getCode(), spec.getCode()))
                        .collect(Collectors.toList()));
                try {
                    IBusinessExt businessExt = annotation.getSdk().newInstance();
                    spec.getExtensions().addAll(scanBusinessExtensions(businessExt));
                } catch (Exception ex) {
                    ex.printStackTrace();
                }

                useCases.add(spec);
                useCaseSpecs.add(spec);
            }
            useCases.sort(Comparator.comparingInt(UseCaseSpec::getPriority));
        }
        return useCaseSpecs;
    }

    @SuppressWarnings("all")
    private synchronized Set<ExtensionSpec> scanBusinessExtensions(IBusinessExt businessExt) {
        Set<ExtensionSpec> extensionSpecList = Sets.newHashSet();

        Method[] methods = businessExt.getClass().getMethods();
        for (Method method : methods) {
            ExtensionAnnotation annotation = getExtensionAnnotation(method);
            if (null == annotation) {
                continue;
            }
            ExtensionSpec extensionSpec = buildExtensionPointSpec(annotation, method);
            if (null != extensionSpec) {
                extensionSpecList.add(extensionSpec);
            }
        }
        for (IBusinessExt subBusinessExt : businessExt.getAllSubBusinessExt()) {
            extensionSpecList.addAll(scanBusinessExtensions(subBusinessExt));
        }
        return extensionSpecList;
    }

    private synchronized ExtensionSpec buildExtensionPointSpec(ExtensionAnnotation annotation, Method invokeMethod) {
        ExtensionSpec spec = new ExtensionSpec(invokeMethod);
        spec.setProtocolType(annotation.getProtocolType());
        spec.setCode(annotation.getCode());
        spec.setName(StringUtils.isEmpty(annotation.getName()) ? invokeMethod.getName() : annotation.getName());
        spec.setReduceType(annotation.getReduceType());
        spec.setDescription(annotation.getDesc());
        return spec;
    }

    @SuppressWarnings("rawtypes")
    public synchronized List<ProductSpec> registerProducts(Set<Class> classSet) {
        List<ProductSpec> productSpecs = Lists.newArrayList();
        synchronized (TemplateRegister.class) {
            for (Class clz : classSet) {
                ProductAnnotation annotation = getProductAnnotation(clz);
                if (null == annotation) {
                    continue;
                }
                ProductSpec productSpec = new ProductSpec();
                productSpec.setProductClass(clz);
                productSpec.setCode(annotation.getCode());
                productSpec.setName(annotation.getName());
                productSpec.setDescription(annotation.getDesc());
                productSpec.setPriority(annotation.getPriority());
                productSpec.getRealizations().addAll(realizations.stream()
                        .filter(p -> StringUtils.equals(p.getCode(), productSpec.getCode()))
                        .collect(Collectors.toList()));
                products.add(productSpec);
                productSpecs.add(productSpec);
            }
            products.sort(Comparator.comparingInt(ProductSpec::getPriority));
        }
        return productSpecs;
    }

    @SuppressWarnings("rawtypes")
    public synchronized List<RealizationSpec> registerRealizations(Set<Class> classSet) {
        List<RealizationSpec> realizationSpecs = Lists.newArrayList();
        synchronized (TemplateRegister.class) {
            for (Class clz : classSet) {
                RealizationAnnotation annotation = getRealizationAnnotation(clz);
                if (null == annotation) {
                    continue;
                }
                for (String code : annotation.getCodes()) {
                    RealizationSpec spec = new RealizationSpec();
                    spec.setCode(code);
                    spec.setScenario(annotation.getScenario());
                    spec.setBusinessExtClass(annotation.getBusinessExtClass());
                    try {
                        spec.setBusinessExt(annotation.getBusinessExtClass().newInstance());
                    } catch (Exception e) {
                        throw new LatticeRuntimeException("LATTICE-CORE-RT-0005", clz.getName());
                    }
                    spec.getExtensionCodes().addAll(BusinessExtUtils.supportedExtCodes(spec.getBusinessExt()));
                    realizations.add(spec);
                    realizationSpecs.add(spec);
                }
            }
        }
        return realizationSpecs;
    }

    public synchronized void clear() {
        synchronized (TemplateRegister.class) {
            realizations.clear();
            products.clear();
            useCases.clear();
            businesses.clear();
        }
    }
}

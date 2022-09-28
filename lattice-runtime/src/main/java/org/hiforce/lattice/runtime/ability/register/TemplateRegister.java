package org.hiforce.lattice.runtime.ability.register;

import com.google.common.collect.Lists;
import lombok.Getter;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.StringUtils;
import org.hifforce.lattice.annotation.model.BusinessAnnotation;
import org.hifforce.lattice.annotation.model.ProductAnnotation;
import org.hifforce.lattice.annotation.model.RealizationAnnotation;
import org.hifforce.lattice.annotation.model.UseCaseAnnotation;
import org.hifforce.lattice.exception.LatticeRuntimeException;
import org.hifforce.lattice.model.ability.IBusinessExt;
import org.hifforce.lattice.model.business.BusinessTemplate;
import org.hifforce.lattice.model.register.BusinessSpec;
import org.hifforce.lattice.model.register.ProductSpec;
import org.hifforce.lattice.model.register.RealizationSpec;
import org.hifforce.lattice.model.register.UseCaseSpec;
import org.hifforce.lattice.model.scenario.ScenarioRequest;
import org.hifforce.lattice.spi.annotation.BusinessAnnotationParser;
import org.hifforce.lattice.spi.annotation.ProductAnnotationParser;
import org.hifforce.lattice.spi.annotation.RealizationAnnotationParser;
import org.hifforce.lattice.spi.annotation.UseCaseAnnotationParser;
import org.hifforce.lattice.utils.BizCodeUtils;
import org.hiforce.lattice.runtime.spi.LatticeSpiFactory;
import org.hiforce.lattice.runtime.utils.BusinessExtUtils;
import org.springframework.core.annotation.AnnotationUtils;

import java.lang.annotation.Annotation;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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
    public List<BusinessSpec> registerBusinesses(Set<Class> classSet) {
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
            businesses.add(businessSpec);
        }
        return businesses;
    }

    @SuppressWarnings("rawtypes")
    public List<UseCaseSpec> registerUseCases(Set<Class> classSet) {
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
            useCases.add(spec);
        }
        useCases.sort(Comparator.comparingInt(UseCaseSpec::getPriority));
        return useCases;
    }

    @SuppressWarnings("rawtypes")
    public List<ProductSpec> registerProducts(Set<Class> classSet) {
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
        }
        products.sort(Comparator.comparingInt(ProductSpec::getPriority));
        return products;
    }

    @SuppressWarnings("rawtypes")
    public List<RealizationSpec> registerRealizations(Set<Class> classSet) {
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
                    throw new LatticeRuntimeException("LATTICE-CORE-RT-0006", clz.getName());
                }
                spec.getExtensionCodes().addAll(BusinessExtUtils.supportedExtCodes(spec.getBusinessExt()));
                realizations.add(spec);
            }
        }
        return realizations;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private BusinessAnnotation getBusinessAnnotation(Class targetClass) {
        for (BusinessAnnotationParser parser : LatticeSpiFactory.getInstance().getBusinessAnnotationParsers()) {
            Annotation annotation = AnnotationUtils.findAnnotation(targetClass, parser.getAnnotationClass());
            if (null == annotation) {
                continue;
            }
            BusinessAnnotation info = new BusinessAnnotation();
            info.setName(parser.getName(annotation));
            info.setCode(parser.getCode(annotation));
            info.setDesc(parser.getDesc(annotation));
            info.setPriority(parser.getPriority(annotation));
            return info;
        }
        return null;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private ProductAnnotation getProductAnnotation(Class targetClass) {
        for (ProductAnnotationParser parser : LatticeSpiFactory.getInstance().getProductAnnotationParsers()) {
            Annotation annotation = AnnotationUtils.findAnnotation(targetClass, parser.getAnnotationClass());
            if (null == annotation) {
                continue;
            }
            ProductAnnotation info = new ProductAnnotation();
            info.setName(parser.getName(annotation));
            info.setCode(parser.getCode(annotation));
            info.setDesc(parser.getDesc(annotation));
            info.setPriority(parser.getPriority(annotation));
            return info;
        }
        return null;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private UseCaseAnnotation getUseCaseAnnotation(Class targetClass) {
        for (UseCaseAnnotationParser parser : LatticeSpiFactory.getInstance().getUseCaseAnnotationParsers()) {
            Annotation annotation = AnnotationUtils.findAnnotation(targetClass, parser.getAnnotationClass());
            if (null == annotation) {
                continue;
            }
            UseCaseAnnotation info = new UseCaseAnnotation();
            info.setName(parser.getName(annotation));
            info.setCode(parser.getCode(annotation));
            info.setDesc(parser.getDesc(annotation));
            info.setSdk(parser.getSdk(annotation));
            info.setPriority(parser.getPriority(annotation));
            return info;
        }
        return null;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private RealizationAnnotation getRealizationAnnotation(Class targetClass) {
        for (RealizationAnnotationParser parser : LatticeSpiFactory.getInstance().getRealizationAnnotationParsers()) {
            Annotation annotation = AnnotationUtils.findAnnotation(targetClass, parser.getAnnotationClass());
            if (null == annotation) {
                continue;
            }
            RealizationAnnotation info = new RealizationAnnotation();
            info.setScenario(parser.getScenario(annotation));
            info.setCodes(parser.getCodes(annotation));
            if (!ClassUtils.isAssignable(targetClass, IBusinessExt.class)) {
                throw new LatticeRuntimeException("LATTICE-CORE-RT-0005", targetClass.getName());
            }
            info.setBusinessExtClass(targetClass);
            return info;
        }
        return null;
    }
}

package org.hiforce.lattice.maven.builder;

import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;
import org.hifforce.lattice.annotation.model.ExtensionAnnotation;
import org.hifforce.lattice.model.ability.IBusinessExt;
import org.hifforce.lattice.model.business.IBusiness;
import org.hifforce.lattice.model.register.BusinessSpec;
import org.hifforce.lattice.model.register.RealizationSpec;
import org.hiforce.lattice.maven.LatticeBuildPlugin;
import org.hiforce.lattice.maven.model.BusinessInfo;
import org.hiforce.lattice.maven.model.ExtParam;
import org.hiforce.lattice.maven.model.ExtensionInfo;
import org.hiforce.lattice.maven.model.RealizationInfo;
import org.hiforce.lattice.runtime.ability.register.TemplateRegister;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static org.hifforce.lattice.utils.LatticeAnnotationUtils.getExtensionAnnotation;

/**
 * @author Rocky Yu
 * @since 2022/10/8
 */
public class BusinessInfoBuilder extends LatticeInfoBuilder {

    public BusinessInfoBuilder(LatticeBuildPlugin plugin) {
        super(plugin);
    }

    @Override
    public String getSpiClassName() {
        return IBusiness.class.getName();
    }

    @Override
    public void build() {

        List<BusinessSpec> businessSpecs = TemplateRegister.getInstance()
                .registerBusinesses(loadTargetClassList(getProvidedInfoClassNames()));
        getPlugin().getLatticeInfo().getBusiness()
                .getProviding().addAll(businessSpecs.stream()
                        .map(this::buildBusinessInfo)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList()));

        businessSpecs = TemplateRegister.getInstance()
                .registerBusinesses(loadTargetClassList(getImportInfoClassNames()));

        getPlugin().getLatticeInfo().getBusiness()
                .getInstalled().addAll(businessSpecs.stream()
                        .map(this::buildBusinessInfo)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList()));
    }

    private BusinessInfo buildBusinessInfo(BusinessSpec spec) {
        if (null == spec) {
            return null;
        }
        BusinessInfo info = new BusinessInfo();
        info.setBizCode(spec.getCode());
        info.setName(spec.getName());
        info.setDesc(spec.getDescription());
        info.setPriority(spec.getPriority());
        info.setClassName(spec.getBusinessClass().getName());

        List<RealizationInfo> realizationInfos =
                spec.getRealizations().stream()
                        .map(this::buildRealizationInfo)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList());
        try {
            for (RealizationInfo realizationInfo : realizationInfos) {
                IBusinessExt businessExt = (IBusinessExt) getTotalClassLoader()
                        .loadClass(realizationInfo.getBusinessExtClass())
                        .newInstance();
                info.getCustomized().addAll(buildBusinessCustomizedExtensionInfos(businessExt));
            }
        } catch (Exception ex) {
            getLog().error(ex.getMessage(), ex);
        }
        return info;
    }

    private List<ExtensionInfo> buildBusinessCustomizedExtensionInfos(IBusinessExt businessExt) {
        List<ExtensionInfo> extensionInfos = Lists.newArrayList();
        for (Method method : businessExt.getClass().getDeclaredMethods()) {
            ExtensionAnnotation annotation = getExtensionAnnotation(method);
            if (null != annotation) {
                ExtensionInfo extensionInfo = buildExtensionInfo(businessExt.getClass(), annotation, method);
                extensionInfos.add(extensionInfo);
            }
        }
        for (IBusinessExt subBusinessExt : businessExt.getAllSubBusinessExt()) {
            extensionInfos.addAll(buildBusinessCustomizedExtensionInfos(subBusinessExt));
        }
        return extensionInfos;
    }

    public static ExtensionInfo buildExtensionInfo(Class<?> facadeClass, ExtensionAnnotation annotation, Method method) {
        ExtensionInfo info = new ExtensionInfo();
        info.setCode(annotation.getCode());
        info.setName(StringUtils.isEmpty(annotation.getName()) ? method.getName() : annotation.getName());
        info.setReduceType(annotation.getReduceType());
        info.setProtocolType(annotation.getProtocolType());
        if (null != facadeClass) {
            info.setClassName(facadeClass.getName());
        }
        info.setReturnTypeName(method.getReturnType().getName());
        info.setMethodName(method.getName());
        info.setParameterCount(method.getParameterCount());
        for (int i = 0; i < method.getParameterCount(); i++) {
            Parameter parameter = method.getParameters()[i];
            ExtParam param = new ExtParam();
            param.setName(parameter.getName());
            param.setTypeName(parameter.getType().getTypeName());
            info.getParams().add(param);
        }
        return info;
    }

    private RealizationInfo buildRealizationInfo(RealizationSpec spec) {
        if (null == spec) {
            return null;
        }
        RealizationInfo info = new RealizationInfo();
        info.setScenario(spec.getScenario());
        info.setBusinessExtClass(spec.getBusinessExtClass().getName());
        info.getExtensionCodes().addAll(spec.getExtensionCodes());
        return info;
    }
}

package org.hiforce.lattice.maven.builder;

import org.apache.commons.lang3.StringUtils;
import org.hiforce.lattice.annotation.Schema;
import org.hiforce.lattice.annotation.model.ExtensionAnnotation;
import org.hiforce.lattice.jar.LatticeJarUtils;
import org.hiforce.lattice.jar.model.LatticeJarInfo;
import org.hiforce.lattice.maven.LatticeBuildPlugin;
import org.hiforce.lattice.maven.model.*;
import org.hiforce.lattice.model.ability.IBusinessExt;
import org.hiforce.lattice.model.business.IBusiness;
import org.hiforce.lattice.model.register.BusinessSpec;
import org.hiforce.lattice.runtime.ability.register.TemplateRegister;

import java.io.File;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.nio.file.Files;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

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

        TemplateRegister.getInstance().getBusinesses().clear();

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
                        .map(LatticeInfoBuilder::buildRealizationInfo)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList());
        try {
            for (RealizationInfo realizationInfo : realizationInfos) {
                IBusinessExt businessExt = (IBusinessExt) getTotalClassLoader()
                        .loadClass(realizationInfo.getBusinessExtClass())
                        .newInstance();
                info.getCustomized().addAll(buildCustomizedExtensionInfos(businessExt));
            }
        } catch (Exception ex) {
            getLog().error(ex.getMessage(), ex);
        }
        return info;
    }

    public static ExtensionInfo buildExtensionInfo(Class<?> facadeClass, ExtensionAnnotation annotation, Method method) {
        ExtensionInfo info = new ExtensionInfo();
        info.setCode(annotation.getCode());
        info.setName(StringUtils.isEmpty(annotation.getName()) ? method.getName() : annotation.getName());
        info.setReduceType(annotation.getReduceType());
        info.setProtocolType(annotation.getProtocolType());
        if (null != facadeClass) {
            info.setClassName(facadeClass.getName());
            LatticeJarInfo jarInfo = getSdkLatticeJarInfo(true, facadeClass);
            if( null != jarInfo && null != jarInfo.getLatticeInfo()){
                SDKInfo sdkInfo = new SDKInfo();
                sdkInfo.setFilename(jarInfo.getFileName());
                sdkInfo.setGroupId(jarInfo.getLatticeInfo().getGroupId());
                sdkInfo.setArtifactId(jarInfo.getLatticeInfo().getArtifactId());
                sdkInfo.setVersion(jarInfo.getLatticeInfo().getVersion());
                info.setSdkInfo(sdkInfo);
            }
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

}

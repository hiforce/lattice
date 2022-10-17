package org.hiforce.lattice.dynamic.destroy;

import org.hiforce.lattice.dynamic.classloader.LatticeClassLoader;
import org.hiforce.lattice.dynamic.model.PluginFileInfo;
import org.hiforce.lattice.dynamic.model.SpringBeanInfo;
import org.hiforce.lattice.dynamic.utils.SpringUtils;
import org.hiforce.lattice.runtime.utils.SpringApplicationContextHolder;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Rocky Yu
 * @since 2022/10/17
 */
public class SpringUninstaller implements LatticeUninstaller {
    @Override
    public DestroyResult uninstall(LatticeClassLoader classLoader, PluginFileInfo fileInfo) {

        List<SpringBeanInfo> beans = fileInfo.getBeans().stream()
                .filter(SpringBeanInfo::isMvc)
                .collect(Collectors.toList());
        beans.forEach(this::removeSpringMVC);

        beans = fileInfo.getBeans().stream()
                .filter(p -> !p.isMvc())
                .collect(Collectors.toList());
        beans.forEach(p -> SpringUtils.removeBean(p.getBeanName()));

        return DestroyResult.success();
    }

    private void removeSpringMVC(SpringBeanInfo info) {
        RequestMappingHandlerMapping mapping =
                SpringApplicationContextHolder.getSpringBean(RequestMappingHandlerMapping.class);
        info.getMappingInfos().forEach(mapping::unregisterMapping);
        SpringUtils.removeBean(info.getBeanName());
    }
}

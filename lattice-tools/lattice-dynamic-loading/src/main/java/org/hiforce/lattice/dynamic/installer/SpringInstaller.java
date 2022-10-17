package org.hiforce.lattice.dynamic.installer;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.hiforce.lattice.dynamic.classloader.LatticeClassLoader;
import org.hiforce.lattice.dynamic.model.PluginFileInfo;
import org.hiforce.lattice.dynamic.model.SpringBeanInfo;
import org.hiforce.lattice.dynamic.utils.SpringUtils;
import org.hiforce.lattice.runtime.utils.SpringApplicationContextHolder;
import org.hiforce.lattice.utils.LatticeAnnotationUtils;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;

/**
 * @author Rocky Yu
 * @since 2022/10/17
 */
public class SpringInstaller implements LatticeInstaller {
    @Override
    public InstallResult install(LatticeClassLoader classLoader, PluginFileInfo fileInfo) {
        try {
            installSpringBeans(classLoader, fileInfo);
            installSpringMVC(classLoader, fileInfo);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return InstallResult.success(fileInfo);
    }

    private void installSpringBeans(LatticeClassLoader classLoader, PluginFileInfo fileInfo) {
        List<Class<?>> classList = fileInfo.getJarFile().stream()
                .map(ZipEntry::getName)
                .filter(p -> StringUtils.endsWith(p, ".class"))
                .map(p -> StringUtils.replace(p, "/", "."))
                .map(p -> StringUtils.replace(p, ".class", ""))
                .map(p -> loadClass(classLoader, p))
                .filter(this::hasSpringAnnotation)
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(classList)) {
            return;
        }
        for (Class<?> targetClass : classList) {
            Object bean = SpringUtils.registerBean(targetClass.getSimpleName(), targetClass);
            fileInfo.getBeans().add(SpringBeanInfo.of(targetClass.getSimpleName(), targetClass, bean, false));
        }
    }

    private void installSpringMVC(LatticeClassLoader classLoader, PluginFileInfo fileInfo) throws Exception {
        List<Class<?>> classList = fileInfo.getJarFile().stream()
                .map(ZipEntry::getName)
                .filter(p -> StringUtils.endsWith(p, ".class"))
                .map(p -> StringUtils.replace(p, "/", "."))
                .map(p -> StringUtils.replace(p, ".class", ""))
                .map(p -> loadClass(classLoader, p))
                .filter(this::hasSpringMVCAnnotation)
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(classList)) {
            return;
        }
        RequestMappingHandlerMapping mapping =
                SpringApplicationContextHolder.getSpringBean(RequestMappingHandlerMapping.class);
        Field field = RequestMappingHandlerMapping.class.getDeclaredField("config");
        field.setAccessible(true);
        RequestMappingInfo.BuilderConfiguration configuration = (RequestMappingInfo.BuilderConfiguration) field.get(mapping);

        for (Class<?> targetClass : classList) {
            Object bean = targetClass.newInstance();
            SpringApplicationContextHolder.getContext().getAutowireCapableBeanFactory().autowireBean(bean);

            SpringBeanInfo beanInfo = SpringBeanInfo.of(targetClass.getSimpleName(), targetClass, bean, true);
            Method[] methods = targetClass.getDeclaredMethods();

            for (Method method : methods) {
                RequestMapping requestMapping = AnnotatedElementUtils.findMergedAnnotation(method, RequestMapping.class);
                if (null == requestMapping) {
                    continue;
                }
                RequestMappingInfo.Builder builder = RequestMappingInfo
                        .paths(requestMapping.path())
                        .methods(requestMapping.method())
                        .params(requestMapping.params())
                        .headers(requestMapping.headers())
                        .consumes(requestMapping.consumes())
                        .produces(requestMapping.produces())
                        .mappingName(requestMapping.name());
                builder.options(configuration);

                RequestMappingInfo mappingInfo = builder.build();
                beanInfo.getMappingInfos().add(mappingInfo);

                mapping.registerMapping(mappingInfo, bean, method);
            }
            fileInfo.getBeans().add(beanInfo);
        }
    }

    private Class<?> loadClass(LatticeClassLoader classLoader, String className) {
        try {
            return classLoader.loadClass(className);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean hasSpringAnnotation(Class<?> targetClass) {
        Annotation annotation = LatticeAnnotationUtils.getAnnotation(targetClass, Service.class);
        if (null != annotation) {
            return true;
        }
        annotation = LatticeAnnotationUtils.getAnnotation(targetClass, Repository.class);
        return null != annotation;
    }

    private boolean hasSpringMVCAnnotation(Class<?> targetClass) {
        Annotation annotation = LatticeAnnotationUtils.getAnnotation(targetClass, RestController.class);
        return null != annotation;
    }
}

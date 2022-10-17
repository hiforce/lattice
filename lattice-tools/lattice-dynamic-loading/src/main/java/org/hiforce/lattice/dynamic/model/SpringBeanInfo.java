package org.hiforce.lattice.dynamic.model;

import com.google.common.collect.Lists;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;

import java.io.Serializable;
import java.util.List;

/**
 * @author Rocky Yu
 * @since 2022/10/17
 */
public class SpringBeanInfo implements Serializable {

    private static final long serialVersionUID = -106882465413341389L;

    @Getter
    @Setter
    private String beanName;

    @Getter
    @Setter
    private Object bean;

    @Getter
    @Setter
    private Class<?> beanClass;

    @Getter
    @Setter
    private boolean mvc;

    @Getter
    private final List<RequestMappingInfo> mappingInfos = Lists.newArrayList();

    public static SpringBeanInfo of(String beanName, Class<?> beanClass, Object bean, boolean mvc) {
        SpringBeanInfo info = new SpringBeanInfo();
        info.beanName = beanName;
        info.beanClass = beanClass;
        info.bean = bean;
        info.mvc = mvc;
        return info;
    }
}

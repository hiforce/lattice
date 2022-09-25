package org.hifforce.lattice.annotation.parser;

import com.google.auto.service.AutoService;
import org.hifforce.lattice.annotation.Business;
import org.hifforce.lattice.spi.annotation.BusinessAnnotationParser;

/**
 * @author Rocky Yu
 * @since 2022/9/21
 */
@AutoService(BusinessAnnotationParser.class)
public class DefaultBusinessAnnotationParser extends BusinessAnnotationParser<Business> {
    @Override
    public String getCode(Business annotation) {
        return annotation.code();
    }

    @Override
    public String getName(Business annotation) {
        return annotation.name();
    }

    @Override
    public String getDesc(Business annotation) {
        return annotation.desc();
    }


    @Override
    public int getPriority(Business annotation) {
        return annotation.priority();
    }

    @Override
    public Class<Business> getAnnotationClass() {
        return Business.class;
    }
}

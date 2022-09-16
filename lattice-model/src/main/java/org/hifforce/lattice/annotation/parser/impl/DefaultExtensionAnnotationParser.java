package org.hifforce.lattice.annotation.parser.impl;

import com.google.auto.service.AutoService;
import org.hifforce.lattice.annotation.Extension;
import org.hifforce.lattice.annotation.model.ReduceType;
import org.hifforce.lattice.annotation.parser.ExtensionAnnotationParser;

/**
 * @author Rocky Yu
 * @since 2022/9/16
 */
@AutoService(ExtensionAnnotationParser.class)
public class DefaultExtensionAnnotationParser extends ExtensionAnnotationParser<Extension> {
    @Override
    public String getName(Extension annotation) {
        return annotation.name();
    }

    @Override
    public String getCode(Extension annotation) {
        return annotation.code();
    }

    @Override
    public String getDesc(Extension annotation) {
        return annotation.desc();
    }

    @Override
    public ReduceType getReduceType(Extension annotation) {
        return annotation.reduceType();
    }

    @Override
    public Class<Extension> getAnnotationClass() {
        return Extension.class;
    }
}

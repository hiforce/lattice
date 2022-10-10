package org.hiforce.lattice.annotation.parser;

import com.google.auto.service.AutoService;
import org.hiforce.lattice.annotation.Extension;
import org.hiforce.lattice.annotation.model.ProtocolType;
import org.hiforce.lattice.annotation.model.ReduceType;
import org.hiforce.lattice.spi.annotation.ExtensionAnnotationParser;

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
    public ProtocolType getProtocolType(Extension annotation) {
        return annotation.protocolType();
    }

    @Override
    public Class<Extension> getAnnotationClass() {
        return Extension.class;
    }
}

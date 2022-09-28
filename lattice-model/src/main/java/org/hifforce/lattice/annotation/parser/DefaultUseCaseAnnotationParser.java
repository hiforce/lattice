package org.hifforce.lattice.annotation.parser;

import com.google.auto.service.AutoService;
import org.hifforce.lattice.annotation.UseCase;
import org.hifforce.lattice.model.ability.IBusinessExt;
import org.hifforce.lattice.spi.annotation.UseCaseAnnotationParser;

/**
 * @author Rocky Yu
 * @since 2022/9/20
 */
@AutoService(UseCaseAnnotationParser.class)
public class DefaultUseCaseAnnotationParser extends UseCaseAnnotationParser<UseCase> {

    @Override
    public Class<UseCase> getAnnotationClass() {
        return UseCase.class;
    }

    @Override
    public String getCode(UseCase annotation) {
        return annotation.code();
    }

    @Override
    public String getName(UseCase annotation) {
        return annotation.name();
    }

    @Override
    public String getDesc(UseCase annotation) {
        return annotation.desc();
    }

    @Override
    public int getPriority(UseCase annotation) {
        return annotation.priority();
    }

    @Override
    public Class<? extends IBusinessExt> getSdk(UseCase annotation) {
        return annotation.sdk();
    }
}

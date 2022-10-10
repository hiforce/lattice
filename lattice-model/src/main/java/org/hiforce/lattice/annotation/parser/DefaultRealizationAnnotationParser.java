package org.hiforce.lattice.annotation.parser;

import com.google.auto.service.AutoService;
import org.hiforce.lattice.annotation.Realization;
import org.hiforce.lattice.spi.annotation.RealizationAnnotationParser;

/**
 * @author Rocky Yu
 * @since 2022/9/18
 */
@AutoService(RealizationAnnotationParser.class)
public class DefaultRealizationAnnotationParser extends RealizationAnnotationParser<Realization> {
    @Override
    public Class<Realization> getAnnotationClass() {
        return Realization.class;
    }

    @Override
    public String[] getCodes(Realization annotation) {
        return annotation.codes();
    }

    @Override
    public String getScenario(Realization annotation) {
        return annotation.scenario();
    }
}

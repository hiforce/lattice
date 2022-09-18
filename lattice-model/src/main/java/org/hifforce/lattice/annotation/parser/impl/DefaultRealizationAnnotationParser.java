package org.hifforce.lattice.annotation.parser.impl;

import com.google.auto.service.AutoService;
import org.hifforce.lattice.annotation.Realization;
import org.hifforce.lattice.annotation.parser.RealizationAnnotationParser;

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

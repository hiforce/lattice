package org.hiforce.lattice.annotation.parser;

import com.google.auto.service.AutoService;
import org.hiforce.lattice.annotation.Priority;
import org.hiforce.lattice.spi.annotation.PriorityAnnotationParser;

/**
 * @author Rocky Yu
 * @since 2023/1/28
 */
@AutoService(PriorityAnnotationParser.class)
public class DefaultPriorityAnnotationParser extends PriorityAnnotationParser<Priority> {
    @Override
    public Class<Priority> getAnnotationClass() {
        return Priority.class;
    }

    @Override
    public int getValue(Priority annotation) {
        return annotation.value();
    }
}

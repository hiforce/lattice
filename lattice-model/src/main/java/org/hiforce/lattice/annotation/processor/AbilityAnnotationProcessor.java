package org.hiforce.lattice.annotation.processor;

import com.google.auto.service.AutoService;
import org.hiforce.lattice.annotation.Ability;
import org.hiforce.lattice.model.ability.IAbility;
import org.hiforce.lattice.spi.annotation.LatticeAnnotationProcessor;

import javax.annotation.processing.Processor;
import javax.annotation.processing.SupportedOptions;
import java.lang.annotation.Annotation;

/**
 * @author Rocky Yu
 * @since 2022/9/15
 */
@AutoService(Processor.class)
@SupportedOptions({"debug", "verify"})
public class AbilityAnnotationProcessor extends LatticeAnnotationProcessor {

    public Class<?> getServiceInterfaceClass() {
        return IAbility.class;
    }

    @Override
    public Class<? extends Annotation> getProcessAnnotationClass() {
        return Ability.class;
    }
}

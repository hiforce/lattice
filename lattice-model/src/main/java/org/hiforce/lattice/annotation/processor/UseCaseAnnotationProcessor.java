package org.hiforce.lattice.annotation.processor;

import com.google.auto.service.AutoService;
import org.hiforce.lattice.annotation.UseCase;
import org.hiforce.lattice.model.business.IUseCase;
import org.hiforce.lattice.spi.annotation.LatticeAnnotationProcessor;

import javax.annotation.processing.Processor;
import javax.annotation.processing.SupportedOptions;
import java.lang.annotation.Annotation;

/**
 * @author Rocky Yu
 * @since 2022/9/20
 */
@AutoService(Processor.class)
@SupportedOptions({"debug", "verify"})
public class UseCaseAnnotationProcessor extends LatticeAnnotationProcessor {

    public Class<?> getServiceInterfaceClass() {
        return IUseCase.class;
    }

    @Override
    public Class<? extends Annotation> getProcessAnnotationClass() {
        return UseCase.class;
    }
}

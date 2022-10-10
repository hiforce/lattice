package org.hiforce.lattice.annotation.processor;

import com.google.auto.service.AutoService;
import org.hiforce.lattice.annotation.Business;
import org.hiforce.lattice.model.business.IBusiness;
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
public class BusinessAnnotationProcessor extends LatticeAnnotationProcessor {

    public Class<?> getServiceInterfaceClass() {
        return IBusiness.class;
    }

    @Override
    public Class<? extends Annotation> getProcessAnnotationClass() {
        return Business.class;
    }
}

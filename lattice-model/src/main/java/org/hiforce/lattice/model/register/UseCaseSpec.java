package org.hiforce.lattice.model.register;

import com.google.common.collect.Sets;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.hiforce.lattice.exception.LatticeRuntimeException;
import org.hiforce.lattice.model.ability.IBusinessExt;
import org.hiforce.lattice.model.business.TemplateType;
import org.hiforce.lattice.model.business.UseCaseTemplate;

import java.util.Set;

/**
 * @author Rocky Yu
 * @since 2022/9/20
 */
@Slf4j
public class UseCaseSpec extends TemplateSpec<UseCaseTemplate> {

    @Getter
    @Setter
    private Class<?> useCaseClass;


    @Getter
    @Setter
    private Class<? extends IBusinessExt> sdk;

    @Getter
    public Set<ExtensionPointSpec> extensions = Sets.newHashSet();

    public UseCaseSpec() {
        this.setPriority(100);
        this.setType(TemplateType.USE_CASE);
    }

    public UseCaseTemplate newInstance() {
        if (null == useCaseClass) {
            return null;
        }
        try {
            return (UseCaseTemplate) useCaseClass.newInstance();
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            throw new LatticeRuntimeException("LATTICE-CORE-004", ex.getMessage());
        }
    }
}

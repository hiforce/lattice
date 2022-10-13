package org.hiforce.lattice.model.register;

import com.google.common.collect.Sets;
import lombok.Getter;
import lombok.Setter;
import org.hiforce.lattice.model.ability.IBusinessExt;

import java.util.Set;

/**
 * @author Rocky Yu
 * @since 2022/9/18
 */
public class RealizationSpec extends BaseSpec {

    @Getter
    @Setter
    private String scenario;

    @Getter
    @Setter
    private IBusinessExt businessExt;

    @Getter
    @Setter
    private Class<IBusinessExt> businessExtClass;

    @Getter
    @Setter
    private boolean remote;

    /**
     * The extension points current realization supported.
     */
    @Getter
    private final Set<String> extensionCodes = Sets.newHashSet();
}

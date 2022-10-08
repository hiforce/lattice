package org.hiforce.lattice.maven.model;

import com.google.common.collect.Sets;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Set;

/**
 * @author Rocky Yu
 * @since 2022/10/8
 */
public class RealizationInfo implements Serializable {
    private static final long serialVersionUID = 4706136921939977069L;

    @Getter
    @Setter
    private String scenario;

    @Getter
    @Setter
    private String businessExtClass;

    @Getter
    private final Set<String> extensionCodes = Sets.newHashSet();
}

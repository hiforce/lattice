package org.hiforce.lattice.remote.client.model;

import com.google.common.collect.Sets;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Set;

/**
 * @author Rocky Yu
 * @since 2022/10/14
 */
public class RemoteBusiness implements Serializable {

    private static final long serialVersionUID = 5819485031371664490L;

    @Getter
    @Setter
    private String bizCode;

    @Getter
    @Setter
    private boolean forceSupportAllCodes;

    @Getter
    @Setter
    private boolean forceStrongDependency = true;

    @Getter
    private final Set<RemoteExtension> extensions = Sets.newHashSet();
}

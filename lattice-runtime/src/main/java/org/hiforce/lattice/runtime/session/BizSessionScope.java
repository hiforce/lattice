package org.hiforce.lattice.runtime.session;

import com.google.common.collect.Lists;
import org.apache.commons.collections4.CollectionUtils;
import org.hifforce.lattice.exception.LatticeRuntimeException;
import org.hifforce.lattice.model.business.IBizObject;
import org.hifforce.lattice.model.scenario.ScenarioRequest;

import java.util.List;

/**
 * @author Rocky Yu
 * @since 2022/9/20
 */
public abstract class BizSessionScope<Resp, ScopeException extends Throwable>
        extends Scope<Resp, ScopeException> {

    private final List<IBizObject> bizObjects = Lists.newArrayList();

    private final List<ScenarioRequest> scenarioRequests = Lists.newArrayList();

    public BizSessionScope(List<IBizObject> bizObjects) {
        if (CollectionUtils.isEmpty(bizObjects)) {
            throw new LatticeRuntimeException("LATTICE-CORE-RT-0010");
        }
        this.bizObjects.addAll(bizObjects);
    }

    public BizSessionScope(IBizObject bizObject) {
        if (null == bizObject) {
            throw new LatticeRuntimeException("LATTICE-CORE-RT-0010");
        }
        this.bizObjects.add(bizObject);
    }


    /**
     * Build the Scenario Request for the business object.
     * Every business object should create a specific scenario request.
     *
     * @param bizObject Business Object.
     * @return ScenarioRequest.
     */
    public abstract ScenarioRequest buildScenarioRequest(IBizObject bizObject);

    @Override
    protected void entrance() {
        //TODO: init the lattice BizSession Context.
        initScenarioRequest();
    }

    private void initScenarioRequest() {
        for (IBizObject bizObject : bizObjects) {
            ScenarioRequest request = buildScenarioRequest(bizObject);
            if (null == request) {
                throw new LatticeRuntimeException("LATTICE-CORE-RT-0011");
            }
            scenarioRequests.add(request);
        }
    }

    @Override
    protected void exit() {
        //TODO: clear the lattice BizSession Context.
    }

    @Override
    protected Entrance getEntrance() {
        return newEntrance();
    }
}

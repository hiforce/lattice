package org.hiforce.lattice.runtime.session;

import com.google.common.collect.Lists;
import lombok.Getter;
import org.apache.commons.collections4.CollectionUtils;
import org.hifforce.lattice.cache.invoke.InvokeCache;
import org.hifforce.lattice.exception.LatticeRuntimeException;
import org.hifforce.lattice.model.business.IBizObject;
import org.hifforce.lattice.model.business.ProductTemplate;
import org.hifforce.lattice.model.context.BizSessionContext;
import org.hifforce.lattice.model.register.ProductSpec;
import org.hifforce.lattice.model.scenario.ScenarioRequest;
import org.hiforce.lattice.runtime.Lattice;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Rocky Yu
 * @since 2022/9/20
 */
public abstract class BizSessionScope<Resp, BizObject extends IBizObject>
        extends Scope<Resp> {

    private final List<BizObject> bizObjects = Lists.newArrayList();

    private final List<ScenarioRequest> scenarioRequests = Lists.newArrayList();

    @Getter
    private BizSessionContext context;

    private boolean invokeCacheInit;


    public BizSessionScope(List<BizObject> bizObjects) {
        if (CollectionUtils.isEmpty(bizObjects)) {
            throw new LatticeRuntimeException("LATTICE-CORE-RT-0010");
        }
        this.bizObjects.addAll(bizObjects);
    }

    public BizSessionScope(BizObject bizObject) {
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
    public abstract ScenarioRequest buildScenarioRequest(BizObject bizObject);

    @Override
    protected void entrance() {
        //TODO: init the lattice BizSession Context.
        invokeCacheInit = InvokeCache.isThreadLocalInit();
        if (!invokeCacheInit) {
            InvokeCache.initInvokeCache();
        }
        context = BizSessionContext.SESSION_CONTEXT_THREAD_LOCAL.get();
        initScenarioRequest();
        buildEffectProducts();
    }

    private void buildEffectProducts() {
        for (ScenarioRequest request : scenarioRequests) {
            List<ProductTemplate> productTemplates =
                    Lattice.getInstance().getAllRegisteredProducts().stream()
                            .map(ProductSpec::createProductInstance)
                            .filter(p -> p.isEffect(request))
                            .collect(Collectors.toList());
            context.getEffectiveProducts().put(request.getBizObject().getBizCode(), productTemplates);
        }

    }

    private void initScenarioRequest() {
        for (BizObject bizObject : bizObjects) {
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
        if (!invokeCacheInit) { //if InvokeCache is init by BizSessionScope, release it.
            InvokeCache.forceClear();
        }
        BizSessionContext.SESSION_CONTEXT_THREAD_LOCAL.remove();
    }

    @Override
    protected Entrance getEntrance() {
        return newEntrance();
    }
}

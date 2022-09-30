package org.hiforce.lattice.remote.container.filter;

import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.rpc.*;


@Slf4j
public class TestFilter implements Filter {

    @Override
    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
        return invoker.invoke(invocation);
    }
}

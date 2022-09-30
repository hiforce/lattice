package org.hiforce.lattice.runtime.ability.delegate.test;

import org.hifforce.lattice.model.ability.execute.ExtensionCallback;
import org.springframework.cglib.proxy.Enhancer;
import org.springframework.cglib.proxy.MethodInterceptor;

/**
 * @author Rocky Yu
 * @since 2022/10/1
 */
public class TestCallProxy {

    public static void main(String[] args) {
        String word = "Jack";
        ExtensionCallback<MyTestBusinessExt, String>
                callback = extension -> extension.hello(word, "Yu");

        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(MyTestBusinessExt.class);
        enhancer.setCallback((MethodInterceptor) (o, method, objects, methodProxy) -> {
            System.out.println("调用方法:" + method);
            return methodProxy.invokeSuper(o, objects);
        });

        MyTestBusinessExt proxy = (MyTestBusinessExt) enhancer.create();
        callback.apply(proxy);
    }
}

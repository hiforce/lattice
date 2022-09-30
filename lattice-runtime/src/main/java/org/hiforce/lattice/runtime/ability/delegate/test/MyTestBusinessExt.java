package org.hiforce.lattice.runtime.ability.delegate.test;

import org.hifforce.lattice.model.ability.BusinessExt;

/**
 * @author Rocky Yu
 * @since 2022/10/1
 */
public class MyTestBusinessExt extends BusinessExt {

    public String hello(String word, String other) {
        System.out.println("Hello: " + word + " and: " + other);
        return "World";
    }
}

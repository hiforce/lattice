package org.hifforce.lattice.annotation.model;

/**
 * @author Rocky Yu
 * @since 2022/9/16
 */
public enum ReduceType {

    NONE,//对优先级排序完全不感知
    ALL, //虽然都需要执行，但还是会感知优先级排序
    FIRST, //对优先级排序非常敏感
    UNKNOWN //还没有梳理的，需要梳理
}

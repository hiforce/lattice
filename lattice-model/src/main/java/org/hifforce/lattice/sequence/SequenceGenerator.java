package org.hifforce.lattice.sequence;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author Rocky Yu
 * @since 2022/9/17
 */
public class SequenceGenerator {

    private static final Map<String, AtomicLong> SEQ_MAP = new ConcurrentHashMap<>();

    private SequenceGenerator() {

    }

    public static long next(String key) {

        AtomicLong sequence = SEQ_MAP.get(key);
        if (null == sequence) {
            synchronized (SequenceGenerator.class) {
                if (null == (sequence = SEQ_MAP.get(key))) {
                    sequence = new AtomicLong();
                    SEQ_MAP.put(key, sequence);
                }
            }
        }
        return sequence.addAndGet(1);
    }

}

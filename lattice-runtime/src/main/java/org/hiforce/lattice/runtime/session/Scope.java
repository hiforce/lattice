package org.hiforce.lattice.runtime.session;

import org.hifforce.lattice.exception.LatticeRuntimeException;

/**
 * @author Rocky Yu
 * @since 2022/9/20
 */
public abstract class Scope<Resp> {

    protected static Entrance newEntrance() {
        return new EntranceImpl();
    }

    protected abstract Entrance getEntrance();

    protected abstract Resp execute() throws LatticeRuntimeException;

    protected abstract void entrance();

    protected abstract void exit();

    /**
     * The BizSession invoke method, main logic entrance.
     *
     * @return Resp
     */
    public Resp invoke() throws LatticeRuntimeException {
        try {
            getEntrance().get().increaseCount();
            if (getEntrance().get().getCount() == 1) {
                entrance();
            }
            return execute();
        } finally {
            getEntrance().get().decreaseCount();
            if (getEntrance().get().getCount() == 0) {
                getEntrance().clearCount();
                exit();
            }
        }
    }

    protected interface Entrance {

        int getCount();

        void increaseCount();

        void decreaseCount();

        void clearCount();

        Entrance get();
    }

    private static class EntranceImpl extends ThreadLocal<EntranceImpl> implements Entrance {

        private int count = 0;

        @Override
        protected EntranceImpl initialValue() {
            return new EntranceImpl();
        }

        @Override
        public int getCount() {
            return count;
        }

        @Override
        public void increaseCount() {
            count++;
        }

        @Override
        public void clearCount() {
            remove();
        }

        @Override
        public void decreaseCount() {
            count--;
        }
    }
}

package org.hifforce.lattice.model.business;

/**
 * @author Rocky Yu
 * @since 2022/9/18
 */
@SuppressWarnings("unused")
public enum TemplateType {

    /**
     * BUSINESS TYPE TEMPLATE.
     */
    BUSINESS {
        @Override
        public boolean isHorizontal() {
            return false;
        }

        @Override
        public boolean isVertical() {
            return true;
        }


        @Override
        public int defaultExtPriority() {
            return 1000;
        }

        @Override
        public boolean needInstall() {
            return false;
        }
    },

    /**
     * PRODUCT TYPE TEMPLATE.
     */
    PRODUCT {
        @Override
        public boolean isHorizontal() {
            return true;
        }

        @Override
        public boolean isVertical() {
            return false;
        }

        @Override
        public int defaultExtPriority() {
            return 500;
        }

        @Override
        public boolean needInstall() {
            return true;
        }
    },

    USE_CASE {
        @Override
        public boolean isHorizontal() {
            return true;
        }

        @Override
        public boolean isVertical() {
            return false;
        }

        @Override
        public int defaultExtPriority() {
            return 100;
        }

        @Override
        public boolean needInstall() {
            return false;
        }
    };

    /**
     * @return Whether current type is vertical template.
     */
    public abstract boolean isHorizontal();

    /**
     * @return Whether current type is horizontal template.
     */
    public abstract boolean isVertical();

    /**
     * @return default extension priority
     */
    public abstract int defaultExtPriority();

    public abstract boolean needInstall();
}

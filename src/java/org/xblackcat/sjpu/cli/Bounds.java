package org.xblackcat.sjpu.cli;

import java.util.Objects;
import java.util.function.Function;

public class Bounds<T extends Comparable<? super T>> {
    private final T lower;
    private final T upper;

    public Bounds(T bound1, T bound2) {
        if (bound1 == null || bound2 == null) {
            throw new NullPointerException("Bound can't contain null value");
        }
        if (bound1.compareTo(bound2) < 0) {
            this.lower = bound1;
            this.upper = bound2;
        } else {
            this.lower = bound2;
            this.upper = bound1;
        }
    }

    public T getLower() {
        return lower;
    }

    public T getUpper() {
        return upper;
    }

    /**
     * Returns true if lower bound is equals to upper bound.
     *
     * @return true if bounds are the same
     */
    public boolean isDot() {
        return Objects.equals(lower, upper);
    }

    public String asString() {
        return asString(String::valueOf);
    }

    public String asString(Function<T, String> formatter) {
        if (isDot()) {
            return formatter.apply(lower);
        } else {
            return formatter.apply(lower) + " - " + formatter.apply(upper);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final Bounds<?> bounds = (Bounds<?>) o;
        return Objects.equals(lower, bounds.lower) &&
                Objects.equals(upper, bounds.upper);
    }

    @Override
    public int hashCode() {
        return Objects.hash(lower, upper);
    }
}

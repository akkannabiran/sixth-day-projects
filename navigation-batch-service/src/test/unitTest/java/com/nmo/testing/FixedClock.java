package com.sixthday.testing;

import java.io.Serializable;
import java.time.*;

/**
 * Inspired by java.time.Clock#FixedClock; but that class is immutable, and we need mutability
 */
public class FixedClock extends Clock implements Serializable {
    private static final long serialVersionUID = 7430389292664866958L;
    private final ZoneId zone;
    private Instant instant;

    public FixedClock(Instant fixedInstant, ZoneId zone) {
        this.instant = fixedInstant;
        this.zone = zone;
    }

    public static FixedClock now() {
        return new FixedClock(Instant.now(), ZoneId.systemDefault());
    }

    @Override
    public ZoneId getZone() {
        return zone;
    }

    @Override
    public Clock withZone(ZoneId zone) {
        if (zone.equals(this.zone)) {
            return this;
        }
        return new FixedClock(instant, zone);
    }

    @Override
    public Instant instant() {
        return instant;
    }

    public void advance(Duration duration) {
        this.instant = this.instant.plus(duration);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof FixedClock) {
            FixedClock other = (FixedClock) obj;
            return instant.equals(other.instant) && zone.equals(other.zone);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return instant.hashCode() ^ zone.hashCode();
    }

    @Override
    public String toString() {
        return "FixedClock[" + instant + "," + zone + "]";
    }
}
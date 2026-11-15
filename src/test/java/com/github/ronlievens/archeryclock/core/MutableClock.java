package com.github.ronlievens.archeryclock.core;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Objects;

final class MutableClock extends Clock {
    private Instant instant;
    private final ZoneId zone;

    MutableClock(Instant instant) {
        this(instant, ZoneId.of("UTC"));
    }

    private MutableClock(Instant instant, ZoneId zone) {
        this.instant = Objects.requireNonNull(instant);
        this.zone = Objects.requireNonNull(zone);
    }

    @Override
    public ZoneId getZone() {
        return zone;
    }

    @Override
    public Clock withZone(ZoneId newZone) {
        return new MutableClock(instant, newZone);
    }

    @Override
    public Instant instant() {
        return instant;
    }

    void advance(Duration duration) {
        instant = instant.plus(duration);
    }
}

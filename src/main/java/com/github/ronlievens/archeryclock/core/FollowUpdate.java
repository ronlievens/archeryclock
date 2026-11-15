package com.github.ronlievens.archeryclock.core;

import java.util.Objects;

public record FollowUpdate(
        int remainingSeconds,
        Phase phase,
        int turnNumber,
        int endNumber,
        String activeArcher) {

    public FollowUpdate {
        Objects.requireNonNull(phase);
        if (remainingSeconds < 0 || turnNumber < 1 || endNumber < 1
                || phase == Phase.PREPARATION) {
            throw new IllegalArgumentException("Invalid follow update");
        }
    }
}

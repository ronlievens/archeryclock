package com.github.ronlievens.archeryclock.core;

public enum Mode {
    FITA,
    FITA_FINALS,
    ONE_ARROW_25M,
    MANUAL,
    FOLLOW;

    public boolean isAutomatic() {
        return this == FITA || this == FITA_FINALS || this == ONE_ARROW_25M;
    }
}

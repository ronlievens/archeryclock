package com.github.ronlievens.archeryclock.ui.main.traffic;

public enum TrafficState {
    RED,
    RED_YELLOW,
    YELLOW,
    GREEN;

    public TrafficState next() {
        switch (this) {
            case RED -> {
                return RED_YELLOW;
            }
            case RED_YELLOW -> {
                return YELLOW;
            }
            case YELLOW -> {
                return GREEN;
            }
            default -> {
                return RED;
            }
        }
    }

    public boolean isRedActive() {
        return this == RED || this == RED_YELLOW;
    }

    public boolean isYellowActive() {
        return this == YELLOW || this == RED_YELLOW;
    }

    public boolean isGreenActive() {
        return this == GREEN;
    }
}

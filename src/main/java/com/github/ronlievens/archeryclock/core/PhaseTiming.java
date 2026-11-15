package com.github.ronlievens.archeryclock.core;

public record PhaseTiming(int redSeconds, int greenSeconds, int orangeSeconds) {
    public PhaseTiming {
        if (redSeconds < 0 || greenSeconds < 0 || orangeSeconds < 0) {
            throw new IllegalArgumentException("Phase durations cannot be negative");
        }
    }

    public int shootingSeconds() {
        return Math.addExact(greenSeconds, orangeSeconds);
    }
}

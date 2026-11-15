package com.github.ronlievens.archeryclock.core;

import java.util.Objects;

public record SignalConfiguration(
        boolean pcAudioEnabled,
        boolean externalOutputEnabled,
        String soundId,
        int emergencyBeepCount,
        boolean keepEmergencyBuzzing) {

    public SignalConfiguration {
        Objects.requireNonNull(soundId);
        if (soundId.isBlank() || emergencyBeepCount < 0) {
            throw new IllegalArgumentException("Invalid signal configuration");
        }
    }

    public static SignalConfiguration defaults() {
        return new SignalConfiguration(true, false, "AC1", 5, false);
    }
}

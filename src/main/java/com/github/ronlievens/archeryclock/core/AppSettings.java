package com.github.ronlievens.archeryclock.core;

import java.util.Objects;

public record AppSettings(
        String language,
        DisplayTimeUnit displayTimeUnit,
        int preparationSeconds,
        String startupScenario,
        String serialPort,
        boolean lanEnabled,
        int networkPort,
        String networkAddress,
        boolean remoteControlEnabled,
        String remotePassword,
        EmergencyPolicy emergencyPolicy,
        int emergencyBeepCount,
        boolean keepEmergencyBuzzing,
        boolean pcAudioEnabled,
        boolean externalSignalsEnabled,
        String soundId,
        boolean prepareAfterEnd) {

    public AppSettings {
        Objects.requireNonNull(language);
        Objects.requireNonNull(displayTimeUnit);
        Objects.requireNonNull(startupScenario);
        Objects.requireNonNull(serialPort);
        Objects.requireNonNull(remotePassword);
        Objects.requireNonNull(emergencyPolicy);
        Objects.requireNonNull(soundId);
        Objects.requireNonNull(networkAddress);
        if (language.isBlank() || preparationSeconds < 0 || startupScenario.isBlank()
                || serialPort.isBlank() || networkPort < 1 || networkPort > 65535
                || networkAddress.isBlank() || emergencyBeepCount < 0 || soundId.isBlank()) {
            throw new IllegalArgumentException("Invalid application settings");
        }
    }

    public static AppSettings defaults() {
        return new AppSettings("English", DisplayTimeUnit.SECONDS, 240, "shiftF5",
                "/dev/ttyACM0", false, 4665, "224.25.26.27", false, "archery",
                EmergencyPolicy.PAUSE, 5, false, true, false, "AC1", false);
    }

    public SignalConfiguration signalConfiguration() {
        return new SignalConfiguration(pcAudioEnabled, externalSignalsEnabled, soundId,
                emergencyBeepCount, keepEmergencyBuzzing);
    }
}

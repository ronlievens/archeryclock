package com.github.ronlievens.archeryclock.core;

public record SessionSnapshot(
        Mode mode,
        Phase phase,
        int remainingSeconds,
        boolean running,
        int turnNumber,
        int endNumber,
        int seriesNumber,
        String activeArcher,
        boolean shootOffActive,
        boolean practiceActive,
        int completedPracticeTurns,
        boolean emergencyActive,
        Side activeSide,
        int leftRemainingSeconds,
        int rightRemainingSeconds) {
}

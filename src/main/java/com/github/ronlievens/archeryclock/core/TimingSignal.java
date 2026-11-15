package com.github.ronlievens.archeryclock.core;

public record TimingSignal(
        Phase phase,
        int beepCount,
        String soundId,
        boolean playOnPc,
        boolean sendExternally,
        boolean emergency,
        boolean keepBuzzing) {
}

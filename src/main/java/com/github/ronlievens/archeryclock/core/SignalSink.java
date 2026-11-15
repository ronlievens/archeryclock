package com.github.ronlievens.archeryclock.core;

@FunctionalInterface
public interface SignalSink {
    void emit(TimingSignal signal);

    static SignalSink none() {
        return signal -> {
        };
    }
}

package com.github.ronlievens.archeryclock.core;

import java.util.Objects;

public final class FollowReceiver {
    private final ArcherySession session;

    public FollowReceiver(ArcherySession session) {
        this.session = Objects.requireNonNull(session);
    }

    public void applySerialUpdate(FollowUpdate update) {
        session.applyFollowUpdate(update);
    }

    public void applyLanUpdate(FollowUpdate update) {
        session.applyFollowUpdate(update);
    }
}

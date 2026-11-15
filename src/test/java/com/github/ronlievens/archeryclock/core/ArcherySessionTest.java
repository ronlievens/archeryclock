package com.github.ronlievens.archeryclock.core;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class ArcherySessionTest {
    private static final Instant START = Instant.parse("2024-01-01T10:00:00Z");

    @Test
    void startsRedAndTransitionsToGreenWithCombinedShootingTime() {
        MutableClock clock = clock();
        ArcherySession session = session(clock);
        session.activate(TimingProfile.fita());

        session.advance();
        assertEquals(Phase.RED, session.snapshot().phase());
        assertEquals(10, session.snapshot().remainingSeconds());

        tick(clock, session, 10);
        assertEquals(Phase.GREEN, session.snapshot().phase());
        assertEquals(120, session.snapshot().remainingSeconds());
    }

    @Test
    void entersOrangeExactlyAtTheConfiguredThreshold() {
        MutableClock clock = clock();
        List<TimingSignal> signals = new ArrayList<>();
        ArcherySession session = new ArcherySession(clock, signals::add);
        session.activate(TimingProfile.fita());
        session.advance();
        tick(clock, session, 10);
        tick(clock, session, 89);
        assertEquals(Phase.GREEN, session.snapshot().phase());
        assertEquals(31, session.snapshot().remainingSeconds());

        tick(clock, session, 1);

        assertEquals(Phase.ORANGE, session.snapshot().phase());
        assertEquals(30, session.snapshot().remainingSeconds());
        assertEquals(1, signals.stream().filter(signal -> signal.phase() == Phase.ORANGE).count());
    }

    @Test
    void ignoresTicksThatDoNotCrossASecondBoundary() {
        MutableClock clock = clock();
        ArcherySession session = session(clock);
        session.activate(TimingProfile.fita());
        session.advance();

        session.tick();
        clock.advance(Duration.ofMillis(400));
        session.tick();
        clock.advance(Duration.ofMillis(599));
        session.tick();

        assertEquals(10, session.snapshot().remainingSeconds());
    }

    @Test
    void appliesOneArrowOrangeThresholdIndependently() {
        MutableClock clock = clock();
        List<TimingSignal> signals = new ArrayList<>();
        ArcherySession session = new ArcherySession(clock, signals::add);
        session.activate(TimingProfile.oneArrow25m());
        session.advance();
        tick(clock, session, 10);
        tick(clock, session, 29);

        assertEquals(Phase.GREEN, session.snapshot().phase());
        assertEquals(16, session.snapshot().remainingSeconds());
        tick(clock, session, 1);

        assertEquals(Phase.ORANGE, session.snapshot().phase());
        assertEquals(15, session.snapshot().remainingSeconds());
        assertEquals(1, signals.stream().filter(signal -> signal.phase() == Phase.ORANGE).count());
    }

    @Test
    void pauseAndResumePreserveRemainingTime() {
        MutableClock clock = clock();
        ArcherySession session = session(clock);
        session.activate(TimingProfile.builder(Mode.FITA)
                .timing(new PhaseTiming(10, 30, 12))
                .build());
        session.advance();
        tick(clock, session, 10);
        session.pauseResume();
        tick(clock, session, 10);
        assertEquals(42, session.snapshot().remainingSeconds());

        session.pauseResume();
        tick(clock, session, 1);

        assertTrue(session.snapshot().running());
        assertEquals(Phase.GREEN, session.snapshot().phase());
        assertEquals(41, session.snapshot().remainingSeconds());
    }

    @Test
    void manualAndFollowModesDoNotToggleAutomatically() {
        MutableClock clock = clock();
        ArcherySession manual = session(clock);
        manual.activate(TimingProfile.manual());
        manual.setManualPhase(Phase.GREEN);
        manual.pauseResume();
        tick(clock, manual, 60);
        assertEquals(Phase.GREEN, manual.snapshot().phase());

        ArcherySession follow = session(clock);
        follow.activate(TimingProfile.follow());
        follow.applyFollowUpdate(new FollowUpdate(45, Phase.ORANGE, 2, 1, "B"));
        follow.advance();
        tick(clock, follow, 60);
        assertEquals(Phase.ORANGE, follow.snapshot().phase());
        assertEquals(45, follow.snapshot().remainingSeconds());
    }

    @Test
    void followReceiverAppliesSerialAndLanUpdatesWithoutLocalCountdown() {
        MutableClock clock = clock();
        ArcherySession session = session(clock);
        session.activate(TimingProfile.follow());
        FollowReceiver receiver = new FollowReceiver(session);
        receiver.applySerialUpdate(new FollowUpdate(123, Phase.GREEN, 2, 1, "B"));

        assertEquals(123, session.snapshot().remainingSeconds());
        assertEquals(Phase.GREEN, session.snapshot().phase());
        assertEquals(2, session.snapshot().turnNumber());
        assertEquals("B", session.snapshot().activeArcher());
        tick(clock, session, 5);
        assertEquals(123, session.snapshot().remainingSeconds());

        receiver.applyLanUpdate(new FollowUpdate(45, Phase.ORANGE, 3, 2, "C"));
        assertEquals(45, session.snapshot().remainingSeconds());
        assertEquals(Phase.ORANGE, session.snapshot().phase());
        assertEquals(3, session.snapshot().turnNumber());
        assertEquals(2, session.snapshot().endNumber());
        assertEquals("C", session.snapshot().activeArcher());
    }

    @Test
    void stopClearsCountdownAndShootOff() {
        MutableClock clock = clock();
        ArcherySession session = session(clock);
        session.activate(TimingProfile.fita());
        assertTrue(session.startShootOff(3));
        session.advance();
        session.stop();

        assertEquals(Phase.WAIT, session.snapshot().phase());
        assertEquals(0, session.snapshot().remainingSeconds());
        assertFalse(session.snapshot().running());
        assertFalse(session.snapshot().shootOffActive());
    }

    @Test
    void advanceFromWaitStartsRedAndTurnCompletionProgressesArchers() {
        MutableClock clock = clock();
        ArcherySession session = session(clock);
        session.activate(TimingProfile.builder(Mode.FITA)
                .turnsPerEnd(2)
                .archerOrder(List.of("A", "B", "C", "D"))
                .build());
        session.advance();
        assertEquals(Phase.RED, session.snapshot().phase());
        tick(clock, session, 10);
        session.advance();

        assertEquals(Phase.WAIT, session.snapshot().phase());
        assertEquals(2, session.snapshot().turnNumber());
        assertEquals(1, session.snapshot().endNumber());
        assertEquals("B", session.snapshot().activeArcher());
    }

    @Test
    void progressionMovesToNextEndAndCyclesThroughTheArcherOrder() {
        MutableClock clock = clock();
        ArcherySession session = session(clock);
        session.activate(TimingProfile.builder(Mode.FITA)
                .archerOrder(List.of("A", "B", "C", "D"))
                .turnsPerEnd(1)
                .endsPerSeries(2)
                .build());

        completeTurn(clock, session);
        assertEquals(2, session.snapshot().endNumber());
        assertEquals(1, session.snapshot().seriesNumber());
        assertEquals("B", session.snapshot().activeArcher());
        completeTurn(clock, session);
        assertEquals(1, session.snapshot().endNumber());
        assertEquals(2, session.snapshot().seriesNumber());
        assertEquals("C", session.snapshot().activeArcher());
        completeTurn(clock, session);
        assertEquals("D", session.snapshot().activeArcher());
        completeTurn(clock, session);
        assertEquals("A", session.snapshot().activeArcher());
    }

    @Test
    void practiceReturnsToTheConfiguredInitialPositionAtItsLimit() {
        MutableClock clock = clock();
        ArcherySession session = session(clock);
        session.activate(TimingProfile.builder(Mode.FITA)
                .archerOrder(List.of("A", "B"))
                .build());
        assertTrue(session.startPractice());
        completeTurn(clock, session);
        assertTrue(session.snapshot().practiceActive());
        assertEquals(1, session.snapshot().completedPracticeTurns());
        completeTurn(clock, session);

        assertFalse(session.snapshot().practiceActive());
        assertEquals(0, session.snapshot().completedPracticeTurns());
        assertEquals(1, session.snapshot().turnNumber());
        assertEquals(1, session.snapshot().endNumber());
        assertEquals("A", session.snapshot().activeArcher());
    }

    @Test
    void duplicateAdvanceWithinGuardDoesNotStartAnotherSequence() {
        MutableClock clock = clock();
        ArcherySession session = new ArcherySession(clock, SignalSink.none(), Duration.ofSeconds(2));
        session.activate(TimingProfile.fita());
        session.advance();
        session.advance();

        assertEquals(Phase.RED, session.snapshot().phase());
        assertEquals(10, session.snapshot().remainingSeconds());
    }

    @Test
    void finalsAlternateSidesAndStopAfterTheConfiguredTurnLimit() {
        MutableClock clock = clock();
        ArcherySession session = session(clock);
        session.activate(TimingProfile.builder(Mode.FITA_FINALS)
                .timing(new PhaseTiming(1, 90, 30))
                .finalsTurnLimit(1)
                .finalsEndLimit(0)
                .build());
        session.advance();
        tick(clock, session, 1);
        assertEquals(Phase.GREEN, session.snapshot().phase());
        assertEquals(120, session.snapshot().remainingSeconds());
        assertEquals(120, session.snapshot().leftRemainingSeconds());

        advance(clock, session);
        assertEquals(Side.RIGHT, session.snapshot().activeSide());
        assertEquals(120, session.snapshot().rightRemainingSeconds());
        advance(clock, session);

        assertEquals(Phase.WAIT, session.snapshot().phase());
        assertNull(session.snapshot().activeSide());
        assertEquals(0, session.snapshot().leftRemainingSeconds());
        assertEquals(0, session.snapshot().rightRemainingSeconds());
    }

    @Test
    void finalsStopResetsBothTimersAndCounters() {
        MutableClock clock = clock();
        ArcherySession session = session(clock);
        session.activate(TimingProfile.finalsPerTurn());
        session.advance();
        tick(clock, session, 10);
        session.advance();
        session.stop();

        assertEquals(Phase.WAIT, session.snapshot().phase());
        assertNull(session.snapshot().activeSide());
        assertEquals(0, session.snapshot().leftRemainingSeconds());
        assertEquals(0, session.snapshot().rightRemainingSeconds());
        assertEquals(1, session.snapshot().turnNumber());
        assertEquals(1, session.snapshot().seriesNumber());
    }

    @Test
    void finalsPerEndLoadsItsConfiguredShootingDurationAfterRed() {
        MutableClock clock = clock();
        ArcherySession session = session(clock);
        session.activate(TimingProfile.builder(Mode.FITA_FINALS)
                .timing(new PhaseTiming(1, 90, 30))
                .finalsTiming(FinalsTiming.PER_END)
                .finalsTurnLimit(0)
                .finalsEndLimit(2)
                .build());
        session.advance();
        tick(clock, session, 1);

        assertEquals(Phase.GREEN, session.snapshot().phase());
        assertEquals(120, session.snapshot().remainingSeconds());
        assertEquals(120, session.snapshot().leftRemainingSeconds());
    }

    @Test
    void finalsShootOffUsesItsArrowLimitInsteadOfTheRegularTurnLimit() {
        MutableClock clock = clock();
        ArcherySession session = session(clock);
        session.activate(TimingProfile.builder(Mode.FITA_FINALS)
                .timing(new PhaseTiming(1, 1, 0))
                .shootOffTiming(new PhaseTiming(1, 2, 0))
                .finalsTurnLimit(1)
                .finalsEndLimit(0)
                .build());
        assertTrue(session.startShootOff(2));
        session.advance();
        tick(clock, session, 1);
        advance(clock, session);
        assertEquals(Side.RIGHT, session.snapshot().activeSide());
        advance(clock, session);
        assertTrue(session.snapshot().shootOffActive());
        assertEquals(Side.LEFT, session.snapshot().activeSide());
        advance(clock, session);
        assertEquals(Side.RIGHT, session.snapshot().activeSide());
        advance(clock, session);

        assertFalse(session.snapshot().shootOffActive());
        assertEquals(Phase.WAIT, session.snapshot().phase());
        assertNull(session.snapshot().activeSide());
    }

    @Test
    void shootOffUsesItsOwnTimingWithoutMutatingTheRegularProfile() {
        MutableClock clock = clock();
        TimingProfile profile = TimingProfile.builder(Mode.FITA)
                .timing(new PhaseTiming(10, 90, 30))
                .shootOffTiming(new PhaseTiming(2, 5, 3))
                .shootOffArrowCount(1)
                .build();
        ArcherySession session = session(clock);
        session.activate(profile);
        assertTrue(session.startShootOff());
        session.advance();
        tick(clock, session, 2);

        assertEquals(Phase.GREEN, session.snapshot().phase());
        assertEquals(8, session.snapshot().remainingSeconds());
        assertEquals(new PhaseTiming(10, 90, 30), session.profile().timing());
        advance(clock, session);
        assertFalse(session.snapshot().shootOffActive());
        assertEquals(Phase.WAIT, session.snapshot().phase());
    }

    @Test
    void preparationTransitionsIntoTheConfiguredShootingStartPhase() {
        MutableClock clock = clock();
        ArcherySession session = session(clock);
        session.activate(TimingProfile.builder(Mode.FITA)
                .preparationSeconds(240)
                .build());
        assertTrue(session.startPreparation());
        tick(clock, session, 239);
        assertEquals(1, session.snapshot().remainingSeconds());
        tick(clock, session, 1);

        assertEquals(Phase.RED, session.snapshot().phase());
        assertEquals(10, session.snapshot().remainingSeconds());
    }

    @Test
    void automaticPreparationCanStartAfterAnEndOnlyForFita() {
        MutableClock clock = clock();
        ArcherySession fita = session(clock);
        fita.activate(TimingProfile.builder(Mode.FITA).prepareAfterEnd(true).build());
        completeTimedTurn(clock, fita);
        assertEquals(Phase.PREPARATION, fita.snapshot().phase());
        assertEquals(240, fita.snapshot().remainingSeconds());

        ArcherySession finals = session(clock);
        finals.activate(TimingProfile.finalsPerTurn());
        assertFalse(finals.startPreparation());
        ArcherySession manual = session(clock);
        manual.activate(TimingProfile.manual());
        assertFalse(manual.startPreparation());
        ArcherySession follow = session(clock);
        follow.activate(TimingProfile.follow());
        assertFalse(follow.startPreparation());
    }

    @Test
    void manualSignalsCycleAndUseConfiguredBeepCountWithoutCountdown() {
        MutableClock clock = clock();
        List<TimingSignal> signals = new ArrayList<>();
        ArcherySession session = new ArcherySession(clock, signals::add);
        session.activate(TimingProfile.builder(Mode.MANUAL).manualBeepCount(4).build());
        session.setManualPhase(Phase.RED);
        session.advance();
        assertEquals(Phase.GREEN, session.snapshot().phase());
        assertEquals(4, signals.getLast().beepCount());
        session.advance();
        assertEquals(Phase.ORANGE, session.snapshot().phase());
        session.advance();
        assertEquals(Phase.RED, session.snapshot().phase());
        tick(clock, session, 60);

        assertEquals(Phase.RED, session.snapshot().phase());
        assertFalse(session.snapshot().running());
    }

    @Test
    void emergencyPauseResumesWithTheSameRemainingTime() {
        MutableClock clock = clock();
        List<TimingSignal> signals = new ArrayList<>();
        ArcherySession session = new ArcherySession(clock, signals::add);
        session.activate(TimingProfile.builder(Mode.FITA)
                .timing(new PhaseTiming(10, 30, 12))
                .build());
        session.advance();
        tick(clock, session, 10);
        session.triggerEmergency();
        tick(clock, session, 10);
        assertEquals(42, session.snapshot().remainingSeconds());
        assertTrue(session.snapshot().emergencyActive());
        assertTrue(signals.getLast().emergency());
        assertEquals(5, signals.getLast().beepCount());
        assertFalse(signals.getLast().keepBuzzing());

        session.advance();
        tick(clock, session, 1);

        assertFalse(session.snapshot().emergencyActive());
        assertEquals(41, session.snapshot().remainingSeconds());
    }

    @Test
    void emergencyStopCurrentKeepsTurnAndNextTurnPolicyAdvancesIt() {
        MutableClock clock = clock();
        ArcherySession stopCurrent = session(clock);
        stopCurrent.activate(TimingProfile.builder(Mode.FITA).turnsPerEnd(2).build());
        stopCurrent.advance();
        stopCurrent.setEmergencyPolicy(EmergencyPolicy.STOP_CURRENT);
        stopCurrent.triggerEmergency();
        assertEquals(1, stopCurrent.snapshot().turnNumber());
        assertEquals(Phase.WAIT, stopCurrent.snapshot().phase());

        ArcherySession nextTurn = session(clock);
        nextTurn.activate(TimingProfile.builder(Mode.FITA).turnsPerEnd(2).build());
        nextTurn.advance();
        nextTurn.setEmergencyPolicy(EmergencyPolicy.NEXT_TURN);
        nextTurn.triggerEmergency();

        assertEquals(2, nextTurn.snapshot().turnNumber());
        assertEquals(Phase.WAIT, nextTurn.snapshot().phase());
    }

    @Test
    void phaseSignalsRespectAudioAndExternalOutputConfiguration() {
        MutableClock clock = clock();
        List<TimingSignal> signals = new ArrayList<>();
        ArcherySession session = new ArcherySession(clock, signals::add);
        session.setSignalConfiguration(new SignalConfiguration(false, true, "bell", 5, false));
        session.activate(TimingProfile.fita());
        session.advance();
        TimingSignal red = signals.getLast();

        assertEquals(Phase.RED, red.phase());
        assertEquals(2, red.beepCount());
        assertEquals("bell", red.soundId());
        assertFalse(red.playOnPc());
        assertTrue(red.sendExternally());
    }

    private static ArcherySession session(MutableClock clock) {
        return new ArcherySession(clock, SignalSink.none());
    }

    private static MutableClock clock() {
        return new MutableClock(START);
    }

    private static void tick(MutableClock clock, ArcherySession session, int seconds) {
        clock.advance(Duration.ofSeconds(seconds));
        session.tick();
    }

    private static void advance(MutableClock clock, ArcherySession session) {
        clock.advance(Duration.ofMillis(300));
        session.advance();
    }

    private static void completeTurn(MutableClock clock, ArcherySession session) {
        clock.advance(Duration.ofMillis(300));
        session.advance();
        tick(clock, session, 10);
        advance(clock, session);
    }

    private static void completeTimedTurn(MutableClock clock, ArcherySession session) {
        session.advance();
        tick(clock, session, 10);
        tick(clock, session, 120);
    }
}

package com.github.ronlievens.archeryclock.core;

import java.time.Clock;
import java.time.Duration;
import java.util.Objects;

public final class ArcherySession {
    private final Clock clock;
    private final SignalSink signalSink;
    private final Duration advanceGuard;
    private TimingProfile profile = TimingProfile.fita();
    private SignalConfiguration signalConfiguration = SignalConfiguration.defaults();
    private EmergencyPolicy emergencyPolicy = EmergencyPolicy.PAUSE;
    private Phase phase = Phase.WAIT;
    private int remainingSeconds;
    private boolean running;
    private int turnNumber = 1;
    private int endNumber = 1;
    private int seriesNumber = 1;
    private int archerIndex;
    private boolean shootOffActive;
    private int shootOffArrows;
    private int completedShootOffTurns;
    private boolean practiceActive;
    private int practiceLimit;
    private int completedPracticeTurns;
    private boolean emergencyActive;
    private Side activeSide;
    private String followArcher;
    private int leftRemainingSeconds;
    private int rightRemainingSeconds;
    private long lastTickSecond;
    private java.time.Instant lastAdvanceAt;

    public ArcherySession(Clock clock, SignalSink signalSink) {
        this(clock, signalSink, Duration.ofMillis(250));
    }

    public ArcherySession(Clock clock, SignalSink signalSink, Duration advanceGuard) {
        this.clock = Objects.requireNonNull(clock);
        this.signalSink = Objects.requireNonNull(signalSink);
        this.advanceGuard = Objects.requireNonNull(advanceGuard);
        if (advanceGuard.isNegative()) {
            throw new IllegalArgumentException("Advance guard cannot be negative");
        }
        lastTickSecond = clock.instant().getEpochSecond();
    }

    public void activate(TimingProfile value) {
        profile = Objects.requireNonNull(value);
        reset();
    }

    public TimingProfile profile() {
        return profile;
    }

    public void setSignalConfiguration(SignalConfiguration value) {
        signalConfiguration = Objects.requireNonNull(value);
    }

    public void setEmergencyPolicy(EmergencyPolicy value) {
        emergencyPolicy = Objects.requireNonNull(value);
    }

    public boolean startPreparation() {
        return startPreparation(profile.preparationSeconds());
    }

    public boolean startPreparation(int seconds) {
        if (profile.mode() != Mode.FITA && profile.mode() != Mode.ONE_ARROW_25M) {
            return false;
        }
        if (seconds < 0 || phase != Phase.WAIT) {
            return false;
        }
        phase = Phase.PREPARATION;
        remainingSeconds = seconds;
        running = true;
        lastTickSecond = clock.instant().getEpochSecond();
        emit(phase, 3, false);
        return true;
    }

    public boolean startPractice(int turns) {
        if (profile.mode() != Mode.FITA || turns < 1 || phase != Phase.WAIT) {
            return false;
        }
        practiceActive = true;
        practiceLimit = turns;
        completedPracticeTurns = 0;
        turnNumber = 1;
        endNumber = 1;
        seriesNumber = 1;
        archerIndex = 0;
        return true;
    }

    public boolean startPractice() {
        return startPractice(profile.practiceTurns());
    }

    public boolean startShootOff() {
        return startShootOff(profile.shootOffArrowCount());
    }

    public boolean startShootOff(int arrows) {
        if (!profile.mode().isAutomatic() || arrows < 1 || phase != Phase.WAIT || running) {
            return false;
        }
        shootOffActive = true;
        shootOffArrows = arrows;
        completedShootOffTurns = 0;
        return true;
    }

    public void advance() {
        if (profile.mode() == Mode.FOLLOW) {
            return;
        }
        if (profile.mode() == Mode.MANUAL) {
            advanceManual();
            return;
        }
        java.time.Instant now = clock.instant();
        if (emergencyActive && emergencyPolicy == EmergencyPolicy.PAUSE) {
            emergencyActive = false;
            if (phase != Phase.WAIT && remainingSeconds > 0) {
                running = true;
                lastTickSecond = now.getEpochSecond();
            }
            return;
        }
        emergencyActive = false;
        if (lastAdvanceAt != null && !now.isBefore(lastAdvanceAt)
                && Duration.between(lastAdvanceAt, now).compareTo(advanceGuard) < 0) {
            return;
        }
        if (lastAdvanceAt != null && now.isBefore(lastAdvanceAt)) {
            return;
        }
        lastAdvanceAt = now;
        if (profile.mode() == Mode.FITA_FINALS) {
            advanceFinals();
            return;
        }
        if (phase == Phase.WAIT) {
            beginSequence();
        } else if (phase == Phase.GREEN || phase == Phase.ORANGE) {
            finishStandardTurn();
        }
    }

    public void pauseResume() {
        if (!profile.mode().isAutomatic() || phase == Phase.WAIT || phase == Phase.PREPARATION) {
            return;
        }
        running = !running;
        if (running) {
            lastTickSecond = clock.instant().getEpochSecond();
        }
    }

    public void tick() {
        if (!running || !profile.mode().isAutomatic()) {
            return;
        }
        long now = clock.instant().getEpochSecond();
        if (now <= lastTickSecond) {
            return;
        }
        long elapsed = now - lastTickSecond;
        lastTickSecond = now;
        while (elapsed > 0 && running) {
            if (remainingSeconds > elapsed) {
                remainingSeconds -= (int) elapsed;
                elapsed = 0;
                updateFinalsRemaining();
                enterOrangeIfNeeded();
                break;
            }
            elapsed -= remainingSeconds;
            remainingSeconds = 0;
            updateFinalsRemaining();
            if (phase == Phase.PREPARATION) {
                beginSequence();
            } else if (phase == Phase.RED) {
                beginShootingPhase();
            } else if (phase == Phase.GREEN || phase == Phase.ORANGE) {
                finishActiveTurn();
            } else {
                running = false;
            }
        }
    }

    public void stop() {
        boolean finals = profile.mode() == Mode.FITA_FINALS;
        running = false;
        phase = Phase.WAIT;
        remainingSeconds = 0;
        leftRemainingSeconds = 0;
        rightRemainingSeconds = 0;
        activeSide = null;
        shootOffActive = false;
        shootOffArrows = 0;
        completedShootOffTurns = 0;
        practiceActive = false;
        practiceLimit = 0;
        completedPracticeTurns = 0;
        emergencyActive = false;
        if (finals) {
            turnNumber = 1;
            endNumber = 1;
            seriesNumber = 1;
            archerIndex = 0;
        }
        lastTickSecond = clock.instant().getEpochSecond();
        emit(phase, 3, false);
    }

    public void triggerEmergency() {
        if (!profile.mode().isAutomatic()) {
            phase = Phase.WAIT;
            remainingSeconds = 0;
            running = false;
            emergencyActive = true;
            emitEmergency();
            return;
        }
        boolean activeTurn = phase == Phase.RED || phase == Phase.GREEN || phase == Phase.ORANGE;
        running = false;
        emergencyActive = true;
        if (emergencyPolicy == EmergencyPolicy.STOP_CURRENT) {
            phase = Phase.WAIT;
            remainingSeconds = 0;
            shootOffActive = false;
        } else if (emergencyPolicy == EmergencyPolicy.NEXT_TURN) {
            phase = Phase.WAIT;
            remainingSeconds = 0;
            if (activeTurn && profile.mode() != Mode.FITA_FINALS) {
                finishStandardTurn();
            }
        }
        lastTickSecond = clock.instant().getEpochSecond();
        emitEmergency();
    }

    public void setManualPhase(Phase value) {
        if (profile.mode() != Mode.MANUAL
                || (value != Phase.RED && value != Phase.GREEN && value != Phase.ORANGE)) {
            throw new IllegalStateException("Manual phases are only available in manual mode");
        }
        phase = value;
        remainingSeconds = 0;
        running = false;
        emit(phase, profile.manualBeepCount(), false);
    }

    public void adjustTurn(int delta) {
        turnNumber = Math.max(1, Math.addExact(turnNumber, delta));
    }

    public void adjustSeries(int delta) {
        seriesNumber = Math.max(1, Math.addExact(seriesNumber, delta));
    }

    public void applyFollowUpdate(FollowUpdate update) {
        Objects.requireNonNull(update);
        if (profile.mode() != Mode.FOLLOW) {
            throw new IllegalStateException("Follow updates require follow mode");
        }
        phase = update.phase();
        remainingSeconds = update.remainingSeconds();
        turnNumber = update.turnNumber();
        endNumber = update.endNumber();
        followArcher = update.activeArcher();
        activeSide = null;
        running = false;
        lastTickSecond = clock.instant().getEpochSecond();
    }

    public SessionSnapshot snapshot() {
        String activeArcher = switch (profile.mode()) {
            case FITA_FINALS -> null;
            case FOLLOW -> followArcher;
            default -> profile.archerOrder().get(archerIndex % profile.archerOrder().size());
        };
        return new SessionSnapshot(profile.mode(), phase, remainingSeconds, running, turnNumber,
                endNumber, seriesNumber, activeArcher, shootOffActive, practiceActive,
                completedPracticeTurns, emergencyActive, activeSide, leftRemainingSeconds,
                rightRemainingSeconds);
    }

    private void advanceManual() {
        phase = switch (phase) {
            case RED -> Phase.GREEN;
            case GREEN -> Phase.ORANGE;
            default -> Phase.RED;
        };
        remainingSeconds = 0;
        running = false;
        emit(phase, profile.manualBeepCount(), false);
    }

    private void beginSequence() {
        if (profile.mode() == Mode.FITA_FINALS) {
            activeSide = Side.LEFT;
            turnNumber = 1;
            seriesNumber = 1;
            leftRemainingSeconds = currentTiming().redSeconds();
            rightRemainingSeconds = 0;
        }
        phase = Phase.RED;
        remainingSeconds = currentTiming().redSeconds();
        running = true;
        lastTickSecond = clock.instant().getEpochSecond();
        updateFinalsRemaining();
        emit(phase, 2, false);
    }

    private void beginShootingPhase() {
        PhaseTiming timing = currentTiming();
        remainingSeconds = timing.shootingSeconds();
        phase = remainingSeconds > 0 && remainingSeconds <= timing.orangeSeconds()
                ? Phase.ORANGE
                : Phase.GREEN;
        running = true;
        updateFinalsRemaining();
        emit(phase, phase == Phase.ORANGE && !profile.orangeCue() ? 0 : 1, false);
        enterOrangeIfNeeded();
    }

    private void enterOrangeIfNeeded() {
        PhaseTiming timing = currentTiming();
        if ((phase == Phase.GREEN || phase == Phase.ORANGE)
                && timing.orangeSeconds() > 0
                && remainingSeconds <= timing.orangeSeconds()
                && phase != Phase.ORANGE) {
            phase = Phase.ORANGE;
            emit(phase, profile.orangeCue() ? 1 : 0, false);
        }
    }

    private void finishActiveTurn() {
        if (profile.mode() == Mode.FITA_FINALS) {
            finishFinalsTurn();
        } else {
            finishStandardTurn();
        }
    }

    private void finishStandardTurn() {
        running = false;
        phase = Phase.WAIT;
        remainingSeconds = 0;
        boolean completedEnd = false;
        if (shootOffActive) {
            completedShootOffTurns++;
            if (completedShootOffTurns >= shootOffArrows) {
                shootOffActive = false;
                completedShootOffTurns = 0;
            }
        } else if (practiceActive) {
            completedPracticeTurns++;
            if (completedPracticeTurns >= practiceLimit) {
                practiceActive = false;
                turnNumber = 1;
                endNumber = 1;
                seriesNumber = 1;
                archerIndex = 0;
                completedPracticeTurns = 0;
            } else {
                progressTurn();
            }
        } else {
            completedEnd = progressTurn();
        }
        emit(phase, 3, false);
        if (completedEnd && profile.mode() == Mode.FITA && profile.prepareAfterEnd()) {
            startPreparation();
        }
    }

    private boolean progressTurn() {
        archerIndex = (archerIndex + 1) % profile.archerOrder().size();
        if (turnNumber < profile.turnsPerEnd()) {
            turnNumber++;
            return false;
        }
        turnNumber = 1;
        endNumber++;
        if (profile.endsPerSeries() > 0 && endNumber > profile.endsPerSeries()) {
            endNumber = 1;
            seriesNumber++;
        }
        return true;
    }

    private void advanceFinals() {
        if (emergencyActive && emergencyPolicy == EmergencyPolicy.PAUSE) {
            emergencyActive = false;
            if (phase != Phase.WAIT && remainingSeconds > 0) {
                running = true;
                lastTickSecond = clock.instant().getEpochSecond();
            }
            return;
        }
        if (phase == Phase.WAIT) {
            beginSequence();
        } else if (phase == Phase.GREEN || phase == Phase.ORANGE) {
            finishFinalsTurn();
        }
    }

    private void finishFinalsTurn() {
        if (activeSide == null) {
            running = false;
            phase = Phase.WAIT;
            remainingSeconds = 0;
            return;
        }
        setSideRemaining(activeSide, 0);
        if (activeSide == Side.LEFT) {
            activeSide = Side.RIGHT;
            if (profile.finalsTiming() == FinalsTiming.PER_TURN || rightRemainingSeconds == 0) {
                setSideRemaining(Side.RIGHT, currentTiming().shootingSeconds());
            }
        } else {
            if (shootOffActive) {
                completedShootOffTurns++;
                if (completedShootOffTurns >= shootOffArrows) {
                    shootOffActive = false;
                    completedShootOffTurns = 0;
                    finishFinalsMatch();
                    return;
                }
            }
            if (!shootOffActive) {
                int limit = profile.finalsTiming() == FinalsTiming.PER_TURN
                        ? profile.finalsTurnLimit()
                        : profile.finalsEndLimit();
                if (seriesNumber >= limit) {
                    finishFinalsMatch();
                    return;
                }
            }
            seriesNumber++;
            turnNumber = seriesNumber;
            activeSide = Side.LEFT;
            if (profile.finalsTiming() == FinalsTiming.PER_TURN || leftRemainingSeconds == 0) {
                setSideRemaining(Side.LEFT, currentTiming().shootingSeconds());
            }
        }
        phase = remainingForActiveSide() <= currentTiming().orangeSeconds()
                && currentTiming().orangeSeconds() > 0
                ? Phase.ORANGE
                : Phase.GREEN;
        remainingSeconds = remainingForActiveSide();
        running = remainingSeconds > 0;
        lastTickSecond = clock.instant().getEpochSecond();
        emit(phase, 1, false);
    }

    private void finishFinalsMatch() {
        running = false;
        phase = Phase.WAIT;
        remainingSeconds = 0;
        leftRemainingSeconds = 0;
        rightRemainingSeconds = 0;
        activeSide = null;
        shootOffActive = false;
        turnNumber = 1;
        seriesNumber = 1;
        emit(phase, 3, false);
    }

    private void updateFinalsRemaining() {
        if (profile.mode() == Mode.FITA_FINALS && activeSide != null) {
            setSideRemaining(activeSide, remainingSeconds);
        }
    }

    private int remainingForActiveSide() {
        return activeSide == Side.LEFT ? leftRemainingSeconds : rightRemainingSeconds;
    }

    private void setSideRemaining(Side side, int seconds) {
        if (side == Side.LEFT) {
            leftRemainingSeconds = seconds;
        } else {
            rightRemainingSeconds = seconds;
        }
    }

    private PhaseTiming currentTiming() {
        return shootOffActive ? profile.shootOffTiming() : profile.timing();
    }

    private void emitEmergency() {
        signalSink.emit(new TimingSignal(phase, signalConfiguration.emergencyBeepCount(),
                signalConfiguration.soundId(), signalConfiguration.pcAudioEnabled(),
                signalConfiguration.externalOutputEnabled(), true,
                signalConfiguration.keepEmergencyBuzzing()));
    }

    private void emit(Phase emittedPhase, int beepCount, boolean emergency) {
        signalSink.emit(new TimingSignal(emittedPhase, beepCount, signalConfiguration.soundId(),
                signalConfiguration.pcAudioEnabled() && beepCount > 0,
                signalConfiguration.externalOutputEnabled(), emergency, false));
    }

    private void reset() {
        phase = Phase.WAIT;
        remainingSeconds = 0;
        running = false;
        turnNumber = 1;
        endNumber = 1;
        seriesNumber = 1;
        archerIndex = 0;
        shootOffActive = false;
        shootOffArrows = 0;
        completedShootOffTurns = 0;
        practiceActive = false;
        practiceLimit = 0;
        completedPracticeTurns = 0;
        emergencyActive = false;
        activeSide = null;
        followArcher = null;
        leftRemainingSeconds = 0;
        rightRemainingSeconds = 0;
        lastTickSecond = clock.instant().getEpochSecond();
        lastAdvanceAt = null;
    }
}

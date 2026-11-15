package com.github.ronlievens.archeryclock.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public record TimingProfile(
        Mode mode,
        PhaseTiming timing,
        PhaseTiming shootOffTiming,
        int turnsPerEnd,
        int endsPerSeries,
        List<String> archerOrder,
        int practiceTurns,
        FinalsTiming finalsTiming,
        int finalsTurnLimit,
        int finalsEndLimit,
        int shootOffArrowCount,
        int preparationSeconds,
        boolean prepareAfterEnd,
        boolean orangeCue,
        int manualBeepCount) {

    public TimingProfile {
        Objects.requireNonNull(mode);
        Objects.requireNonNull(timing);
        Objects.requireNonNull(shootOffTiming);
        Objects.requireNonNull(finalsTiming);
        archerOrder = List.copyOf(archerOrder);
        if (turnsPerEnd < 1 || endsPerSeries < 0 || practiceTurns < 0
                || finalsTurnLimit < 0 || finalsEndLimit < 0 || shootOffArrowCount < 1
                || preparationSeconds < 0 || manualBeepCount < 0 || archerOrder.isEmpty()
                || archerOrder.stream().anyMatch(archer -> archer == null || archer.isBlank())) {
            throw new IllegalArgumentException("Invalid timing profile");
        }
        if (mode == Mode.FITA_FINALS && finalsTurnLimit == 0 && finalsEndLimit == 0) {
            throw new IllegalArgumentException("Finals require a turn or end limit");
        }
    }

    public static Builder builder(Mode mode) {
        return new Builder(mode);
    }

    public static TimingProfile fita() {
        return builder(Mode.FITA).build();
    }

    public static TimingProfile oneArrow25m() {
        return builder(Mode.ONE_ARROW_25M)
                .timing(new PhaseTiming(10, 30, 15))
                .shootOffTiming(new PhaseTiming(10, 30, 15))
                .build();
    }

    public static TimingProfile finalsPerTurn() {
        return builder(Mode.FITA_FINALS)
                .timing(new PhaseTiming(10, 20, 0))
                .finalsTiming(FinalsTiming.PER_TURN)
                .finalsTurnLimit(3)
                .finalsEndLimit(0)
                .build();
    }

    public static TimingProfile finalsPerEnd() {
        return builder(Mode.FITA_FINALS)
                .finalsTiming(FinalsTiming.PER_END)
                .finalsTurnLimit(0)
                .finalsEndLimit(2)
                .build();
    }

    public static TimingProfile manual() {
        return builder(Mode.MANUAL).build();
    }

    public static TimingProfile follow() {
        return builder(Mode.FOLLOW).build();
    }

    public static TimingProfile builtInScenario(String name) {
        String normalized = Objects.requireNonNull(name).toUpperCase(Locale.ROOT);
        if (normalized.startsWith("SHIFT")) {
            normalized = normalized.substring("SHIFT".length());
        }
        if (!normalized.matches("F([1-9]|1[0-2])")) {
            throw new IllegalArgumentException("Unknown scenario: " + name);
        }

        return switch (normalized) {
            case "F1" -> fita();
            case "F2" -> builder(Mode.FITA).archerOrder(List.of("A", "B")).build();
            case "F3" -> builder(Mode.FITA).archerOrder(List.of("A", "B", "C")).build();
            case "F4" -> builder(Mode.FITA).archerOrder(List.of("A", "B", "C", "D")).build();
            case "F5" -> builder(Mode.FITA).archerOrder(List.of("AB", "CD")).build();
            case "F6" -> builder(Mode.FITA).archerOrder(List.of("A", "B")).turnsPerEnd(2).build();
            case "F7" -> builder(Mode.FITA).archerOrder(List.of("AB", "CD")).turnsPerEnd(2).build();
            case "F8" -> finalsPerTurn();
            case "F9" -> finalsPerEnd();
            case "F10" -> oneArrow25m().withArchers(4);
            case "F11" -> oneArrow25m().withArchers(5);
            case "F12" -> oneArrow25m().withArchers(6);
            default -> throw new IllegalArgumentException("Unknown scenario: " + name);
        };
    }

    private TimingProfile withArchers(int count) {
        List<String> archers = new ArrayList<>(count);
        for (int index = 0; index < count; index++) {
            archers.add(String.valueOf((char) ('A' + index)));
        }
        return builder(mode)
                .timing(timing)
                .shootOffTiming(shootOffTiming)
                .archerOrder(archers)
                .turnsPerEnd(turnsPerEnd)
                .endsPerSeries(endsPerSeries)
                .practiceTurns(practiceTurns)
                .finalsTiming(finalsTiming)
                .finalsTurnLimit(finalsTurnLimit)
                .finalsEndLimit(finalsEndLimit)
                .shootOffArrowCount(shootOffArrowCount)
                .preparationSeconds(preparationSeconds)
                .prepareAfterEnd(prepareAfterEnd)
                .orangeCue(orangeCue)
                .manualBeepCount(manualBeepCount)
                .build();
    }

    public static final class Builder {
        private final Mode mode;
        private PhaseTiming timing = new PhaseTiming(10, 90, 30);
        private PhaseTiming shootOffTiming = new PhaseTiming(10, 10, 30);
        private int turnsPerEnd = 1;
        private int endsPerSeries;
        private List<String> archerOrder = List.of("A");
        private int practiceTurns = 2;
        private FinalsTiming finalsTiming = FinalsTiming.PER_TURN;
        private int finalsTurnLimit = 3;
        private int finalsEndLimit = 2;
        private int shootOffArrowCount = 3;
        private int preparationSeconds = 240;
        private boolean prepareAfterEnd;
        private boolean orangeCue = true;
        private int manualBeepCount = 3;

        private Builder(Mode mode) {
            this.mode = Objects.requireNonNull(mode);
        }

        public Builder timing(PhaseTiming value) {
            timing = Objects.requireNonNull(value);
            return this;
        }

        public Builder shootOffTiming(PhaseTiming value) {
            shootOffTiming = Objects.requireNonNull(value);
            return this;
        }

        public Builder turnsPerEnd(int value) {
            turnsPerEnd = value;
            return this;
        }

        public Builder endsPerSeries(int value) {
            endsPerSeries = value;
            return this;
        }

        public Builder archerOrder(List<String> value) {
            archerOrder = List.copyOf(value);
            return this;
        }

        public Builder practiceTurns(int value) {
            practiceTurns = value;
            return this;
        }

        public Builder finalsTiming(FinalsTiming value) {
            finalsTiming = Objects.requireNonNull(value);
            return this;
        }

        public Builder finalsTurnLimit(int value) {
            finalsTurnLimit = value;
            return this;
        }

        public Builder finalsEndLimit(int value) {
            finalsEndLimit = value;
            return this;
        }

        public Builder shootOffArrowCount(int value) {
            shootOffArrowCount = value;
            return this;
        }

        public Builder preparationSeconds(int value) {
            preparationSeconds = value;
            return this;
        }

        public Builder prepareAfterEnd(boolean value) {
            prepareAfterEnd = value;
            return this;
        }

        public Builder orangeCue(boolean value) {
            orangeCue = value;
            return this;
        }

        public Builder manualBeepCount(int value) {
            manualBeepCount = value;
            return this;
        }

        public TimingProfile build() {
            return new TimingProfile(mode, timing, shootOffTiming, turnsPerEnd, endsPerSeries,
                    archerOrder, practiceTurns, finalsTiming, finalsTurnLimit, finalsEndLimit,
                    shootOffArrowCount, preparationSeconds, prepareAfterEnd, orangeCue, manualBeepCount);
        }
    }
}

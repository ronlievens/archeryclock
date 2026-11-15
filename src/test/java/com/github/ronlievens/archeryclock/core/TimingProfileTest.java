package com.github.ronlievens.archeryclock.core;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

final class TimingProfileTest {
    @Test
    void createsIndependentFitaAndOneArrowTimings() {
        TimingProfile fita = TimingProfile.builder(Mode.FITA)
                .timing(new PhaseTiming(10, 90, 30))
                .build();
        TimingProfile oneArrow = TimingProfile.builder(Mode.ONE_ARROW_25M)
                .timing(new PhaseTiming(10, 30, 15))
                .build();

        assertEquals(Mode.FITA, fita.mode());
        assertEquals(new PhaseTiming(10, 90, 30), fita.timing());
        assertEquals(Mode.ONE_ARROW_25M, oneArrow.mode());
        assertEquals(new PhaseTiming(10, 30, 15), oneArrow.timing());
        assertEquals(new PhaseTiming(10, 90, 30), fita.timing());
    }

    @Test
    void selectsFinalsAndNonAutomaticProfiles() {
        TimingProfile finals = TimingProfile.finalsPerTurn();
        TimingProfile manual = TimingProfile.manual();
        TimingProfile follow = TimingProfile.follow();

        assertEquals(FinalsTiming.PER_TURN, finals.finalsTiming());
        assertEquals(Mode.FITA_FINALS, finals.mode());
        assertEquals(Mode.MANUAL, manual.mode());
        assertEquals(Mode.FOLLOW, follow.mode());
        assertFalse(manual.mode().isAutomatic());
        assertFalse(follow.mode().isAutomatic());
    }

    @Test
    void builtInFitaProfileMatchesDefaultScenario() {
        TimingProfile profile = TimingProfile.builtInScenario("shiftF5");

        assertEquals(Mode.FITA, profile.mode());
        assertEquals(new PhaseTiming(10, 90, 30), profile.timing());
        assertEquals(List.of("AB", "CD"), profile.archerOrder());
    }

    @Test
    void rejectsInvalidDurationsAndFinalsWithoutLimits() {
        assertThrows(IllegalArgumentException.class, () -> new PhaseTiming(10, -1, 0));
        assertThrows(IllegalArgumentException.class, () -> TimingProfile.builder(Mode.FITA_FINALS)
                .finalsTurnLimit(0)
                .finalsEndLimit(0)
                .build());
    }
}

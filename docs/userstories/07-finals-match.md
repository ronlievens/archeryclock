# AC-07 - Run a finals match

**As a** finals match operator, **I want** left and right archers to alternate
under the selected per-turn or per-end timing rule, **so that** progression
matches the configured finals format.

The source maintains two timers, switches the active side, and supports timing
per turn or per end.

**Dependencies:** AC-01, AC-02, AC-06.

**Source:** [nextfinal](../original-src/Countdown.pas#L14112).

**JUnit seam:** Seed finals state for each side and timing policy; assert the
side, phase, and both countdown values after an advance.

```gherkin
Feature: Finals match timing

  Scenario: [AC-07-T01] Alternate sides for per-turn timing
    Given a finals session uses per-turn timing
    And the left side has completed its turn
    When finals progression advances
    Then the right side becomes active
    And the right timer is initialized from the per-turn profile

  Scenario: [AC-07-T02] Load the configured per-end duration after red
    Given a finals session uses per-end timing
    And the active side is in RED with 1 second remaining
    And that side has green 90 seconds and orange 30 seconds configured
    When the fake clock advances by 1 second and the timer ticks
    Then that side enters GREEN
    And its remaining time is 120 seconds

  Scenario: [AC-07-T03] Finish the configured finals match
    Given both sides have completed the configured final turn or end
    When the last side completes
    Then both timers stop
    And the session returns to waiting with no active side
```

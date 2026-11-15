# AC-04 - Stop and reset a timed sequence

**As a** tournament operator, **I want** to stop an active timed sequence and
return it to a known waiting state, **so that** I can safely restart the round.

Stopping an automatic timed session disables its timers, clears active
countdowns and shoot-off state, and returns it to wait. Finals also reset the
active side and turn/series counters.

**Dependencies:** AC-02.

**Source:** [stop](../original-src/Countdown.pas#L13969).

**JUnit seam:** Assert a domain snapshot and timer-running flags; use a fake
signal sink to verify any stop cue.

```gherkin
Feature: Stop and reset

  Scenario: [AC-04-T01] Stop a standard timed sequence
    Given a FITA session is running with 25 seconds remaining
    And a shoot-off is active
    When stop is applied
    Then the session is waiting
    And the timer is stopped
    And the remaining time is zero
    And shoot-off state is cleared

  Scenario: [AC-04-T02] Reset finals counters
    Given a finals session is active on the right side at turn 3, series 2
    When stop is applied
    Then both side timers are stopped
    And no side is active
    And the turn and series counters are reset to 1
```

# AC-02 - Run a time-synchronized phase clock

**As a** tournament operator, **I want** timed phases to advance on whole
seconds and change at their configured thresholds, **so that** competitors
receive consistent timing signals.

The original aligns timer decrements to system time and transitions through
wait, red, green, and orange states.

**Dependencies:** AC-01.

**Source:** [FirstTimerTimer](../original-src/Countdown.pas#L7015).

**JUnit seam:** Inject a controllable `Clock`; provide an explicit tick method
or scheduler. Record phase-change events using a fake event/signal sink.

```gherkin
Feature: Timed phase clock

  Scenario: [AC-02-T01] Start a standard sequence
    Given a FITA session is waiting with a red duration of 10 seconds
    When the start operation is applied
    Then the phase is RED
    And the remaining time is 10 seconds

  Scenario: [AC-02-T02] Expire red and start green
    Given a single-turn FITA session is in RED with 1 second remaining
    When the fake clock advances to the next second and the timer ticks
    Then the phase is GREEN
    And the remaining time is the configured green duration plus orange duration

  Scenario: [AC-02-T03] Enter orange at its configured threshold
    Given a FITA session is in GREEN with 31 seconds remaining
    And the orange duration is 30 seconds
    When one second elapses
    Then the phase is ORANGE
    And exactly one phase-change event is recorded

  Scenario: [AC-02-T04] Ignore repeated ticks within the same second
    Given a timed phase has 10 seconds remaining
    And its countdown reference is aligned to the current second
    When multiple timer ticks occur without the fake clock crossing a second boundary
    Then the remaining time is still 10 seconds

  Scenario: [AC-02-T05] Use the one-arrow orange threshold
    Given a 25m one-arrow session is in GREEN with 16 seconds remaining
    And its orange duration is 15 seconds
    When one second elapses
    Then the phase is ORANGE
    And exactly one phase-change event is recorded
```

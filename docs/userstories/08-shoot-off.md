# AC-08 - Run a shoot-off

**As a** tournament operator, **I want** to start a shoot-off with its own
timings and arrow count, **so that** a tie can be resolved without changing
the normal round profile.

The source tracks shoot-off state and uses separate shoot-off timings and
turn limits in standard and finals modes.

**Dependencies:** AC-01, AC-02, AC-06.

**Source:** [soff](../../original-src/Countdown.pas#L10431),
[nextfinal](../../original-src/Countdown.pas#L14112),
[finalyzeturn](../../original-src/Countdown.pas#L6924).

**JUnit seam:** Use a profile with distinct regular and shoot-off timings.
Assert that activating a shoot-off does not mutate regular timing values.

```gherkin
Feature: Shoot-off timing

  Scenario: [AC-08-T01] Start a shoot-off with its configured arrow count
    Given a timed profile has shoot-off timings and an arrow count of 3
    And the regular sequence is stopped
    When a shoot-off is activated
    Then shoot-off state is active
    And the shoot-off timing values are loaded
    And the regular profile timing values are unchanged

  Scenario: [AC-08-T02] Complete a shoot-off
    Given a shoot-off is active
    When its configured turn or arrow limit is reached
    Then shoot-off state is cleared
    And the session returns to its normal waiting state

```

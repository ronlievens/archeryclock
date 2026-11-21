# AC-09 - Run a pre-match countdown

**As a** tournament operator, **I want** to start a configurable countdown
before the match, **so that** competitors receive a predictable preparation
period.

The original supports a manual pre-count in non-finals automatic modes and an
option to start one after each FITA end. It excludes finals, manual, and follow
modes.

**Dependencies:** AC-02.

**Source:** [starttostart](../../original-src/Countdown.pas#L19638),
[finalyzeturn](../../original-src/Countdown.pas#L6924).

**JUnit seam:** Use the fake clock and a profile with a known preparation
duration. Assert phase transitions and remaining time.

```gherkin
Feature: Pre-match countdown

  Scenario: [AC-09-T01] Start a configured preparation countdown
    Given a FITA session is waiting
    And the preparation duration is 4 minutes
    When the preparation countdown is started
    Then the phase is PREPARATION
    And 240 seconds remain

  Scenario: [AC-09-T02] Begin shooting when preparation expires
    Given a preparation countdown has 1 second remaining
    When the fake clock advances by 1 second and the timer ticks
    Then the preparation phase ends
    And the session enters its configured shooting start phase

  Scenario: [AC-09-T03] Automatically prepare after a FITA end
    Given a FITA session has automatic preparation enabled
    And an end has just completed
    When end progression finishes
    Then a preparation countdown starts with the configured duration

  Scenario: [AC-09-T04] Do not start automatic preparation in excluded modes
    Given the active mode is finals, manual, or follow
    When preparation is requested through the automatic-after-end rule
    Then no preparation countdown starts
```

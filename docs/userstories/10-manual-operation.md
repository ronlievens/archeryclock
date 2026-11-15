# AC-10 - Operate the clock manually

**As a** tournament operator, **I want** to set signal phases manually,
**so that** the system can be used when automatic timing is not appropriate.

In manual mode, `next` sets green after red, orange after green, and red
otherwise. Manual mode does not run the automatic countdown.

**Dependencies:** AC-01.

**Source:** [next](../original-src/Countdown.pas#L14222),
[soundsignal](../original-src/Countdown.pas#L6828).

**JUnit seam:** Use a manual-mode session and fake signal sink; assert phase
state and emitted manual beep count.

```gherkin
Feature: Manual signal operation

  Scenario: [AC-10-T01] Cycle through manual signal phases
    Given manual mode is active in RED
    When advance is applied
    Then the phase is GREEN
    When advance is applied again
    Then the phase is ORANGE
    When advance is applied again
    Then the phase is RED

  Scenario: [AC-10-T02] Do not run an automatic countdown
    Given manual mode is active in GREEN
    When the fake clock advances by 60 seconds
    Then the phase remains GREEN
    And no timed countdown is running

  Scenario: [AC-10-T03] Use the configured manual beep count
    Given manual mode is configured for 4 beeps
    When the operator changes the signal phase
    Then the fake signal sink records 4 beep events
```

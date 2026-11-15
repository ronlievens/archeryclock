# AC-11 - Handle an emergency interruption

**As a** tournament operator, **I want** an emergency action to pause or stop
the current timing according to a configured policy, **so that** an
interruption is handled consistently.

The source supports pause, stop-current, and move-to-next policies, plus
configurable emergency text and alert beeps.

**Dependencies:** AC-02, AC-05.

**Source:** [stopemergency](../original-src/Countdown.pas#L14023),
[next](../original-src/Countdown.pas#L14222).

**JUnit seam:** Use a fake clock and fake signal sink. Test each policy with a
non-final timed session and a known active turn.

```gherkin
Feature: Emergency interruption

  Scenario: [AC-11-T01] Pause and retain the current countdown
    Given a FITA GREEN phase is running with 42 seconds remaining
    And emergency policy is set to pause
    When an emergency is triggered
    Then the phase timer stops
    And 42 seconds remain
    And the emergency alert is recorded

  Scenario: [AC-11-T02] Resume a paused emergency phase
    Given an emergency has paused a FITA phase with 42 seconds remaining
    When the operator advances to resume
    Then the phase is restored from the remaining time
    And the phase timer runs again

  Scenario: [AC-11-T03] Stop the current turn
    Given a timed turn is active
    And emergency policy is set to stop the current turn
    When an emergency is triggered
    Then the session returns to waiting
    And the current turn is not advanced

  Scenario: [AC-11-T04] Move to the next turn
    Given a non-final timed turn is active and is not the last turn of the end
    And emergency policy is set to move to the next turn
    When an emergency is triggered
    Then the session returns to waiting
    And the turn number advances by one
```

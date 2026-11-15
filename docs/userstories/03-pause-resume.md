# AC-03 - Pause and resume a timed sequence

**As a** tournament operator, **I want** to pause and resume an eligible timed
sequence, **so that** an interruption does not discard the remaining time.

Pause/resume toggles the active timers in automatic timed modes. Manual and
follow modes use separate control paths.

**Dependencies:** AC-02.

**Source:** [playpauze](../original-src/Countdown.pas#L10441).

**JUnit seam:** Use the fake clock and inspect a session snapshot before and
after pause/resume.

```gherkin
Feature: Pause and resume

  Scenario: [AC-03-T01] Pause an active timed phase
    Given a non-manual, non-follow phase is running with 42 seconds remaining
    When pause is applied
    And the fake clock advances by 10 seconds
    Then the phase remains unchanged
    And 42 seconds remain

  Scenario: [AC-03-T02] Resume a paused phase
    Given a timed phase is paused with 42 seconds remaining
    When resume is applied
    And the fake clock advances by 1 second
    Then the same phase is running
    And 41 seconds remain

  Scenario: [AC-03-T03] Do not toggle a follow or manual session
    Given a session is in follow mode or manual mode
    When the automatic pause/resume operation is applied
    Then its state is unchanged
```

# AC-06 - Progress through archers, turns, and ends

**As a** tournament operator, **I want** turns and ends to follow the selected
archer sequence and repeat rules, **so that** a round progresses without
manual counter correction.

The source supports A-F sequences, multi-archer groupings, repeated/double
turns, and practice turns.

**Dependencies:** AC-01, AC-05.

**Source:** [finalyzeturn](../original-src/Countdown.pas#L6924);
sequence variants are loaded by [Setsettings](../original-src/Countdown.pas#L15896).

**JUnit seam:** Use explicit sequence/profile fixtures; assert the progression
state after each accepted turn completion.

```gherkin
Feature: Turn and end progression

  Scenario: [AC-06-T01] Advance to the next turn
    Given an end has 2 turns and turn 1 is active
    When turn 1 is finalized
    Then turn 2 is active
    And the end number is unchanged

  Scenario: [AC-06-T02] Advance to the next end
    Given an end has 2 turns and turn 2 is active
    When turn 2 is finalized
    Then the turn number is reset to 1
    And the end or series number advances according to the profile

  Scenario: [AC-06-T03] Complete practice turns
    Given a FITA practice sequence has a limit of 2 turns
    When the final practice turn is finalized
    Then practice mode ends
    And the normal sequence returns to its configured initial position

  Scenario: [AC-06-T04] Follow a multi-archer sequence
    Given the configured sequence is A-B-C-D
    When four successive turns are finalized
    Then the active archer order is A, B, C, D
```

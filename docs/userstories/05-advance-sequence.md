# AC-05 - Advance a sequence

**As a** tournament operator, **I want** to advance the current sequence,
**so that** the next phase or turn starts at the correct point.

The original `next` operation starts a standard sequence from wait, finalizes
green/orange turns, and handles finals separately. A short timeout prevents
duplicate advances.

**Dependencies:** AC-02.

**Source:** [next](../original-src/Countdown.pas#L14222),
[end timeout](../original-src/Countdown.pas#L14973).

**JUnit seam:** Apply operations directly to a session service. Use a
controllable guard timer for duplicate-advance tests.

```gherkin
Feature: Advance a sequence

  Scenario: [AC-05-T01] Start from the waiting state
    Given a standard timed session is waiting
    When advance is applied
    Then the red phase begins with its configured duration

  Scenario: [AC-05-T02] Finalize a green or orange turn
    Given a standard timed session is in GREEN or ORANGE
    When advance is applied
    Then the current turn is finalized
    And progression selects the next configured turn or end

  Scenario: [AC-05-T03] Ignore a duplicate advance during the guard interval
    Given an advance has been accepted
    When another advance is applied before the guard interval expires
    Then the second advance does not skip another turn
```

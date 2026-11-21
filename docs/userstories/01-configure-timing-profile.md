# AC-01 - Configure a competition timing profile

**As a** tournament operator, **I want** to choose a supported archery timing
system and its timing rules, **so that** the clock follows the rules for the
current event.

The original supports FITA, FITA finals, 25m one-arrow, manual, and follow
systems. Their timing and sequence settings differ.

**Dependencies:** None.

**Source:** [FITA mode](../../original-src/Countdown.pas#L18830),
[FITA finals](../../original-src/Countdown.pas#L18846),
[25m one-arrow](../../original-src/Countdown.pas#L18865),
[follow mode](../../original-src/Countdown.pas#L10354),
[manual mode](../../original-src/Countdown.pas#L10361).

**JUnit seam:** Create profiles as domain values. Assert the activated profile
and derived session configuration without constructing Swing components.

```gherkin
Feature: Competition timing profiles

  Scenario: [AC-01-T01] Activate a FITA profile
    Given a FITA profile with red 10 seconds, green 90 seconds, and orange 30 seconds
    When the profile is activated
    Then the active mode is FITA
    And its red, green, and orange durations are 10, 90, and 30 seconds

  Scenario: [AC-01-T02] Activate a 25 metre one-arrow profile
    Given a one-arrow profile with red 10 seconds, green 30 seconds, and orange 15 seconds
    When the profile is activated
    Then the active mode is 25m one-arrow
    And its timings are independent of the FITA timings

  Scenario: [AC-01-T03] Activate a finals profile
    Given a finals profile configured for timing per turn
    When the profile is activated
    Then the active mode is FITA finals
    And the session contains distinct left-side and right-side timer state

  Scenario: [AC-01-T04] Activate manual and follow modes
    Given a manual profile and a follow profile
    When each profile is activated in turn
    Then the active mode changes to the selected mode
    And neither mode is treated as an automatic FITA session
```

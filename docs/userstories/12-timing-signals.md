# AC-12 - Emit audible and external signal cues

**As a** range operator, **I want** phase changes and exceptional states to
produce configured audible and external signals, **so that** competitors can
follow timing without relying on the software display.

The original maps phase states to beep counts and sound files, and routes
state changes through signal-switching routines.

**Dependencies:** AC-02.

**Source:** [soundsignal](../../original-src/Countdown.pas#L6828),
[TLswitch](../../original-src/Countdown.pas#L2050),
[buzzerTimerTimer](../../original-src/Countdown.pas#L14465).

**JUnit seam:** Inject fake audio and signal-output ports that record event
type, selected sound, and beep count.

```gherkin
Feature: Timing cues

  Scenario: [AC-12-T01] Emit the red-phase cue
    Given audio signaling is enabled with a selected sound
    When the timing engine enters RED
    Then the selected sound is recorded
    And 2 beep events are recorded

  Scenario: [AC-12-T02] Emit the green or orange phase cue
    Given audio signaling is enabled
    When the timing engine enters GREEN or ORANGE
    Then 1 beep event is recorded

  Scenario: [AC-12-T03] Suppress PC audio while keeping external signaling
    Given PC audio is disabled and an external signal output is enabled
    When the timing engine changes phase
    Then no PC audio event is recorded
    And the external output receives the phase change

  Scenario: [AC-12-T04] Emit the configured emergency beep count
    Given an emergency is active with a configured count of 5 beeps
    And the keep-buzzing option is disabled
    When the emergency signal is emitted
    Then 5 beep events are recorded
```

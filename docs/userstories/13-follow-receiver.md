# AC-13 - Synchronize as a follow receiver

**As a** range operator, **I want** a receiver clock to follow timing data
from a master clock over a supported input, **so that** multiple displays stay
aligned.

The original decodes follow data through serial and LAN paths and applies
received timing, phase, turn, and related state.

**Dependencies:** AC-01.

**Source:** [serialRxData/perfsercommand](../original-src/Countdown.pas#L7552),
[TCP1Receive](../original-src/Countdown.pas#L9979),
[connectports](../original-src/Countdown.pas#L5361).

**JUnit seam:** Feed normalized updates through fake serial/network adapters.
Do not open real ports or start real sockets in unit tests.

```gherkin
Feature: Follow a master clock

  Scenario: [AC-13-T01] Apply a master update received over serial
    Given follow mode is configured for serial input
    When a valid update reports 123 seconds, GREEN, and turn 2
    Then the receiver snapshot reports 123 seconds
    And the receiver phase is GREEN
    And the receiver turn is 2

  Scenario: [AC-13-T02] Apply a master update received over LAN
    Given follow mode is configured for LAN input
    When a valid network update reports 45 seconds and ORANGE
    Then the receiver snapshot reports 45 seconds and ORANGE

  Scenario: [AC-13-T03] Do not locally advance while awaiting a master update
    Given follow mode is active with 45 seconds in the last received update
    When the fake clock advances by 5 seconds without a master update
    Then the receiver does not start a local competition countdown
```

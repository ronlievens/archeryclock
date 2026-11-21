# AC-16 - Control a session remotely over LAN

**As a** range operator, **I want** an enabled remote controller to issue
supported session commands, **so that** I can operate the clock from another
device.

The original accepts next, play/pause, stop, emergency, turn/end adjustment,
preparation, shoot-off, and scenario commands when remote control is enabled
and its configured command check passes.

**Dependencies:** AC-03 through AC-06, AC-08 through AC-11, AC-15.

**Source:** [TCPaccess1Receive](../../original-src/Countdown.pas#L10136).

**JUnit seam:** Test a command handler with an in-memory command transport.
Assert command dispatch and resulting session state; do not bind a TCP port.

```gherkin
Feature: Remote session control

  Scenario: [AC-16-T01] Apply an enabled remote next command
    Given LAN remote control is enabled
    And a remote command passes the configured check
    And the session is waiting
    When the command NEXT is received
    Then the session enters its configured start phase

  Scenario: [AC-16-T02] Ignore a command when remote control is disabled
    Given LAN remote control is disabled
    When a validly formed NEXT command is received
    Then the session state does not change

  Scenario: [AC-16-T03] Ignore a command that fails the configured check
    Given LAN remote control is enabled
    When a NEXT command with an invalid check value is received
    Then the session state does not change
```

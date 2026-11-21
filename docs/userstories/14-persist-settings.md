# AC-14 - Persist application settings

**As an** operator, **I want** application and device settings to survive
restarts, **so that** I do not have to reconfigure the range each time.

The original reads and writes a line-based `settings` file containing
preferences such as language, time units, audio, startup scenario, ports,
network options, and remote-control settings.

**Dependencies:** AC-01.

**Source:** [first-run defaults](../../original-src/Countdown.pas#L12346),
[Setsystemsettings](../../original-src/Countdown.pas#L15716),
[repository defaults](../../original-src/settings).

**JUnit seam:** Use JUnit `@TempDir` and a settings repository/service that can
load and save settings without initializing hardware.

```gherkin
Feature: Persist application settings

  Scenario: [AC-14-T01] Round-trip saved settings
    Given settings specify seconds, a 4-minute preparation, and startup scenario shiftF5
    When the settings are saved and loaded into a new service
    Then the loaded time unit is seconds
    And the loaded preparation duration is 4 minutes
    And the loaded startup scenario is shiftF5

  Scenario: [AC-14-T02] Create defaults when the settings file is missing
    Given the settings file does not exist
    When settings are loaded for first startup
    Then default settings are returned
    And a settings file is created
    And the startup scenario is shiftF5

  Scenario: [AC-14-T03] Restore the selected serial port
    Given saved settings contain serial port "/dev/ttyACM0"
    When the settings are loaded into a new configuration
    Then the selected serial port is "/dev/ttyACM0"
```

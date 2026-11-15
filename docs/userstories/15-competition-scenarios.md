# AC-15 - Load and save competition scenarios

**As a** tournament operator, **I want** to save and reload named competition
scenarios, **so that** event-specific timing and sequence settings can be
reused.

The original has built-in F1-F12 profiles and loads custom files from
`scenarios/<name>`. Scenario save handlers write to named slots.

**Dependencies:** AC-01, AC-14.

**Source:** [changescenario](../original-src/Countdown.pas#L13200),
[getscenario](../original-src/Countdown.pas#L16285),
[savevarF1buttonClick](../original-src/Countdown.pas#L19386),
[built-in defaults](../original-src/Countdown.pas#L15896).

**JUnit seam:** Use a temporary scenarios directory. Assert the loaded profile
values, not the legacy file's line ordering or encoding.

```gherkin
Feature: Reusable competition scenarios

  Scenario: [AC-15-T01] Load a custom scenario file
    Given a custom scenario F1 defines FITA timings of red 8, green 80, orange 20
    When scenario F1 is selected
    Then the active profile has timings of red 8, green 80, orange 20

  Scenario: [AC-15-T02] Use built-in settings when a custom file is absent
    Given no custom scenario file exists for F5
    When scenario F5 is selected
    Then the built-in F5 profile is applied
    And the active mode is FITA

  Scenario: [AC-15-T03] Save and reload a scenario
    Given a valid profile has been saved to scenario F2
    When the scenario repository is reloaded and F2 is selected
    Then the loaded profile equals the saved profile

  Scenario: [AC-15-T04] Use the default startup scenario
    Given application settings select startup scenario shiftF5
    When startup settings are applied
    Then the built-in F5 profile is selected
```

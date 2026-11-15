# BDD User Stories

The original backlog has been split into one numbered file per story under
[`docs/userstories/`](./docs/userstories/). The numbering gives a suggested
implementation order; each file contains the story, source references, and
deterministic BDD scenarios exercised by the JUnit core tests.

These are behavior-focused requirements derived from `original-src`. They do
not specify GUI layout. The GUI-independent Java core implements the timing,
progression, persistence, normalized follow-update, signal, and remote-command
behaviors; hardware and live network adapters remain outside the core.

## Suggested order

| Order | Story | Depends on |
|---:|---|---|
| 01 | [Configure a competition timing profile](./docs/userstories/01-configure-timing-profile.md) | - |
| 02 | [Run a time-synchronized phase clock](./docs/userstories/02-run-phase-clock.md) | 01 |
| 03 | [Pause and resume a timed sequence](./docs/userstories/03-pause-resume.md) | 02 |
| 04 | [Stop and reset a timed sequence](./docs/userstories/04-stop-reset.md) | 02 |
| 05 | [Advance a sequence](./docs/userstories/05-advance-sequence.md) | 02 |
| 06 | [Progress through archers, turns, and ends](./docs/userstories/06-turn-end-progression.md) | 01, 05 |
| 07 | [Run a finals match](./docs/userstories/07-finals-match.md) | 01, 02, 06 |
| 08 | [Run a shoot-off](./docs/userstories/08-shoot-off.md) | 01, 02, 06 |
| 09 | [Run a pre-match countdown](./docs/userstories/09-pre-match-countdown.md) | 02 |
| 10 | [Operate the clock manually](./docs/userstories/10-manual-operation.md) | 01 |
| 11 | [Handle an emergency interruption](./docs/userstories/11-emergency-interruption.md) | 02, 05 |
| 12 | [Emit audible and external signal cues](./docs/userstories/12-timing-signals.md) | 02 |
| 13 | [Synchronize as a follow receiver](./docs/userstories/13-follow-receiver.md) | 01 |
| 14 | [Persist application settings](./docs/userstories/14-persist-settings.md) | 01 |
| 15 | [Load and save competition scenarios](./docs/userstories/15-competition-scenarios.md) | 01, 14 |
| 16 | [Control a session remotely over LAN](./docs/userstories/16-lan-remote-control.md) | 03-06, 08-11, 15 |

## JUnit scenario conventions

- Drive time through an injected/fake clock and an explicit timer tick; do not
  use `Thread.sleep` or depend on wall-clock timing.
- Assert domain state and emitted events through read-only snapshots and fake
  signal/device/network ports; do not instantiate Swing components.
- Use JUnit `@TempDir` for settings and scenario file tests.
- Hardware and network acceptance tests should use fakes for ordinary CI.
  Real-device tests are separate because the original scenario files and
  protocol fixtures are not included in the repository.

## Source-derived constraints

- `original-src/settings` is populated, but there is no
  `original-src/scenarios/` directory. The default configuration selects
  `shiftF5`; custom scenario contents are unavailable.
- Serial, LAN-follow, remote-control, K8055, and GPIO behavior needs protocol
  and hardware contract tests before it is treated as complete.
- The stories capture observed legacy behavior. Confirm competition-rule
  edge cases, especially finals, practice sequences, and emergency recovery,
  before treating them as normative rules.

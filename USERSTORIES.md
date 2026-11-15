# BDD User Stories

The original backlog has been split into one numbered file per story under
[`BDD_USER_STORIES/`](./BDD_USER_STORIES/). The numbering gives a suggested
implementation order; each file contains the story, source references, and
deterministic BDD scenarios suitable for later JUnit tests.

These are behavior-focused requirements derived from `original-src`. They do
not specify GUI layout and do not claim the Java rewrite already implements
them.

## Suggested order

| Order | Story | Depends on |
|---:|---|---|
| 01 | [Configure a competition timing profile](./BDD_USER_STORIES/01-configure-timing-profile.md) | - |
| 02 | [Run a time-synchronized phase clock](./BDD_USER_STORIES/02-run-phase-clock.md) | 01 |
| 03 | [Pause and resume a timed sequence](./BDD_USER_STORIES/03-pause-resume.md) | 02 |
| 04 | [Stop and reset a timed sequence](./BDD_USER_STORIES/04-stop-reset.md) | 02 |
| 05 | [Advance a sequence](./BDD_USER_STORIES/05-advance-sequence.md) | 02 |
| 06 | [Progress through archers, turns, and ends](./BDD_USER_STORIES/06-turn-end-progression.md) | 01, 05 |
| 07 | [Run a finals match](./BDD_USER_STORIES/07-finals-match.md) | 01, 02, 06 |
| 08 | [Run a shoot-off](./BDD_USER_STORIES/08-shoot-off.md) | 01, 02, 06 |
| 09 | [Run a pre-match countdown](./BDD_USER_STORIES/09-pre-match-countdown.md) | 02 |
| 10 | [Operate the clock manually](./BDD_USER_STORIES/10-manual-operation.md) | 01 |
| 11 | [Handle an emergency interruption](./BDD_USER_STORIES/11-emergency-interruption.md) | 02, 05 |
| 12 | [Emit audible and external signal cues](./BDD_USER_STORIES/12-timing-signals.md) | 02 |
| 13 | [Synchronize as a follow receiver](./BDD_USER_STORIES/13-follow-receiver.md) | 01 |
| 14 | [Persist application settings](./BDD_USER_STORIES/14-persist-settings.md) | 01 |
| 15 | [Load and save competition scenarios](./BDD_USER_STORIES/15-competition-scenarios.md) | 01, 14 |
| 16 | [Control a session remotely over LAN](./BDD_USER_STORIES/16-lan-remote-control.md) | 03-06, 08-11, 15 |

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

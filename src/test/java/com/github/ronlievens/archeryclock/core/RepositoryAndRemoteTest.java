package com.github.ronlievens.archeryclock.core;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class RepositoryAndRemoteTest {
    @TempDir
    Path temporaryDirectory;

    @Test
    void settingsRoundTripAndPersistSerialPort() throws Exception {
        Path file = temporaryDirectory.resolve("settings.json");
        SettingsRepository repository = new SettingsRepository(file);
        AppSettings expected = new AppSettings("Nederlands", DisplayTimeUnit.MINUTES, 240,
                "shiftF5", "/dev/ttyACM0", true, 5005, "192.168.1.20", true, "secret",
                EmergencyPolicy.NEXT_TURN, 4, true, false, true, "bell", true);
        repository.save(expected);

        AppSettings loaded = new SettingsRepository(file).load();

        assertEquals(expected, loaded);
        assertEquals("/dev/ttyACM0", loaded.serialPort());
        assertEquals(5005, loaded.networkPort());
        assertEquals("192.168.1.20", loaded.networkAddress());
        assertEquals("Nederlands", loaded.language());
        assertEquals(240, loaded.preparationSeconds());
        assertEquals("shiftF5", loaded.startupScenario());
    }

    @Test
    void missingSettingsCreateFirstRunDefaultsAndPersistThem() throws Exception {
        Path file = temporaryDirectory.resolve("settings.json");
        SettingsRepository repository = new SettingsRepository(file);

        AppSettings settings = repository.load();

        assertTrue(Files.exists(file));
        assertEquals(AppSettings.defaults(), settings);
        assertEquals("shiftF5", new SettingsRepository(file).load().startupScenario());
    }

    @Test
    void customScenarioCanBeSavedReloadedAndSelectedByStartupName() throws Exception {
        ScenarioRepository repository = new ScenarioRepository(temporaryDirectory.resolve("scenarios"));
        TimingProfile profile = TimingProfile.builder(Mode.FITA)
                .timing(new PhaseTiming(8, 80, 20))
                .build();
        repository.save("F2", profile);

        TimingProfile loaded = new ScenarioRepository(temporaryDirectory.resolve("scenarios"))
                .select("shiftF2");

        assertEquals(profile, loaded);
        assertEquals(profile, repository.select("F2"));
    }

    @Test
    void absentScenarioUsesBuiltInProfileAndDefaultStartupSelectsF5() throws Exception {
        ScenarioRepository repository = new ScenarioRepository(temporaryDirectory);

        TimingProfile customDefault = repository.select("F5");
        TimingProfile startup = repository.select(AppSettings.defaults().startupScenario());

        assertEquals(Mode.FITA, customDefault.mode());
        assertEquals(TimingProfile.builtInScenario("F5"), customDefault);
        assertEquals(customDefault, startup);
    }

    @Test
    void invalidScenarioNamesAndCorruptCustomFilesFailExplicitly() throws Exception {
        ScenarioRepository repository = new ScenarioRepository(temporaryDirectory);
        assertFalse(Files.exists(temporaryDirectory.resolve("F13")));
        org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class,
                () -> repository.select("F13"));
        Files.writeString(temporaryDirectory.resolve("F1"), "{not-json");
        org.junit.jupiter.api.Assertions.assertThrows(java.io.IOException.class,
                () -> repository.select("F1"));
    }

    @Test
    void remoteNextRequiresEnabledControlAndValidCheck() {
        MutableClock clock = new MutableClock(Instant.parse("2024-01-01T10:00:00Z"));
        ArcherySession session = new ArcherySession(clock, SignalSink.none());
        session.activate(TimingProfile.fita());
        AppSettings disabled = settings(false);
        RemoteCommandHandler disabledHandler = new RemoteCommandHandler(disabled, session, ignored -> {
        });
        String command = RemoteCommandHandler.encode("next", "secret");

        assertFalse(disabledHandler.handle(command));
        assertEquals(Phase.WAIT, session.snapshot().phase());

        RemoteCommandHandler enabledHandler = new RemoteCommandHandler(settings(true), session, ignored -> {
        });
        assertFalse(enabledHandler.handle(RemoteCommandHandler.encode("next", "other")));
        String validCommand = RemoteCommandHandler.encode("next", "secret");
        assertTrue(enabledHandler.handle(validCommand));
        assertEquals(Phase.RED, session.snapshot().phase());
    }

    @Test
    void remoteCommandsDispatchPreparationShootOffAndScenarioSelection() {
        MutableClock clock = new MutableClock(Instant.parse("2024-01-01T10:00:00Z"));
        ArcherySession session = new ArcherySession(clock, SignalSink.none());
        session.activate(TimingProfile.fita());
        List<String> selectedScenarios = new ArrayList<>();
        RemoteCommandHandler handler = new RemoteCommandHandler(settings(true), session,
                selectedScenarios::add);

        assertTrue(handler.handle(RemoteCommandHandler.encode("pr04", "secret")));
        assertEquals(240, session.snapshot().remainingSeconds());
        session.stop();
        assertTrue(handler.handle(RemoteCommandHandler.encode("sof3", "secret")));
        assertTrue(session.snapshot().shootOffActive());
        assertTrue(handler.handle(RemoteCommandHandler.encode("sf05", "secret")));
        assertEquals(List.of("F5"), selectedScenarios);
    }

    private static AppSettings settings(boolean remoteEnabled) {
        return new AppSettings("English", DisplayTimeUnit.SECONDS, 240, "shiftF5",
                "/dev/ttyACM0", false, 4665, "224.25.26.27", remoteEnabled, "secret",
                EmergencyPolicy.PAUSE, 5, false, true, false, "AC1", false);
    }
}

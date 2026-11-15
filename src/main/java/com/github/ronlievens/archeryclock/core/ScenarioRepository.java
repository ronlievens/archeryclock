package com.github.ronlievens.archeryclock.core;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Objects;

public final class ScenarioRepository {
    private final Path directory;
    private final ObjectMapper mapper;

    public ScenarioRepository(Path directory) {
        this.directory = Objects.requireNonNull(directory).toAbsolutePath().normalize();
        mapper = new ObjectMapper();
    }

    public TimingProfile select(String scenarioName) throws IOException {
        String name = normalize(scenarioName);
        Path file = directory.resolve(name);
        if (Files.exists(file)) {
            return JsonFileStore.read(file, mapper, TimingProfile.class);
        }
        return TimingProfile.builtInScenario(name);
    }

    public void save(String scenarioName, TimingProfile profile) throws IOException {
        String name = normalize(scenarioName);
        JsonFileStore.write(directory.resolve(name), mapper, Objects.requireNonNull(profile));
    }

    private String normalize(String scenarioName) {
        String name = Objects.requireNonNull(scenarioName).toUpperCase(Locale.ROOT);
        if (name.startsWith("SHIFT")) {
            name = name.substring("SHIFT".length());
        }
        if (!name.matches("F([1-9]|1[0-2])")) {
            throw new IllegalArgumentException("Unknown scenario: " + scenarioName);
        }
        return name;
    }
}

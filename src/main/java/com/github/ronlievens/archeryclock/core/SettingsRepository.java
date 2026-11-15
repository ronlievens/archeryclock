package com.github.ronlievens.archeryclock.core;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

public final class SettingsRepository {
    private final Path file;
    private final ObjectMapper mapper;

    public SettingsRepository(Path file) {
        this.file = Objects.requireNonNull(file).toAbsolutePath().normalize();
        mapper = new ObjectMapper();
    }

    public AppSettings load() throws IOException {
        if (!Files.exists(file)) {
            AppSettings defaults = AppSettings.defaults();
            save(defaults);
            return defaults;
        }
        return JsonFileStore.read(file, mapper, AppSettings.class);
    }

    public void save(AppSettings settings) throws IOException {
        JsonFileStore.write(file, mapper, Objects.requireNonNull(settings));
    }
}

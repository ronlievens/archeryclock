package com.github.ronlievens.archeryclock.core;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

final class JsonFileStore {
    private JsonFileStore() {
    }

    static <T> T read(Path path, ObjectMapper mapper, Class<T> type) throws IOException {
        return mapper.readValue(path.toFile(), type);
    }

    static void write(Path path, ObjectMapper mapper, Object value) throws IOException {
        Path absolutePath = path.toAbsolutePath().normalize();
        Path parent = absolutePath.getParent();
        Files.createDirectories(parent);
        Path temporary = Files.createTempFile(parent, absolutePath.getFileName().toString(), ".tmp");
        try {
            mapper.writeValue(temporary.toFile(), value);
            try {
                Files.move(temporary, absolutePath, StandardCopyOption.ATOMIC_MOVE,
                        StandardCopyOption.REPLACE_EXISTING);
            } catch (AtomicMoveNotSupportedException exception) {
                Files.move(temporary, absolutePath, StandardCopyOption.REPLACE_EXISTING);
            }
        } finally {
            Files.deleteIfExists(temporary);
        }
    }
}

package com.github.ronlievens.archeryclock.ui.font;

import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.awt.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public final class RobotoMonoFont {

    private static final String FONTS_FOLDER = "fonts/RobotoMono";
    private static final String FONTS_EXTENSION = ".ttf";
    private static final String FONTS_EXTENSION_REGEX = "(?i)^RobotoMono-|\\.ttf$";

    private static RobotoMonoFont _instance;

    private final Map<String, Font> fonts;

    private RobotoMonoFont() {
        this.fonts = new HashMap<>();
    }

    public static synchronized RobotoMonoFont getInstance() {
        if (_instance == null) {
            _instance = new RobotoMonoFont();
        }
        return _instance;
    }

    public static void load() throws IOException {

        val classLoader = Thread.currentThread().getContextClassLoader();
        val resources = classLoader.getResources(FONTS_FOLDER);

        while (resources.hasMoreElements()) {
            val resource = resources.nextElement();

            if ("file".equals(resource.getProtocol())) {
                val path = Paths.get(resource.getPath());

                try (val stream = Files.walk(path)) {
                    stream
                        .filter(Files::isRegularFile)
                        .filter(file -> file.toString().endsWith(FONTS_EXTENSION))
                        .forEach(file -> addFont(file));
                }
            }
        }
    }

    private static void addFont(final Path file) {
        val key = file.getFileName().toString()
            .replaceAll(FONTS_EXTENSION_REGEX, "")
            .toLowerCase();
        log.trace("Adding font [{}] with key [{}]", file, key);
        getInstance().fonts.put(key, load(file));
    }

    private static Font load(final Path path) {
        try (val stream = Files.newInputStream(path)) {
            val font = Font.createFont(Font.TRUETYPE_FONT, stream);
            GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(font);
            return font;
        } catch (Exception e) {
            throw new RuntimeException("Unable to load font: %s".formatted(path), e);
        }
    }

    public static Font get(final String name, final float size) {
        val font = getInstance().fonts.get(name.toLowerCase());
        if (font == null) {
            throw new IllegalArgumentException("Font [%s] not found in: %s ".formatted(name, getInstance().fonts.keySet()));
        }
        return font.deriveFont(size);
    }
}

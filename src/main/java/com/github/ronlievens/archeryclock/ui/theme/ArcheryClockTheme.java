package com.github.ronlievens.archeryclock.ui.theme;

import lombok.*;

import java.awt.*;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ArcheryClockTheme {

    private static ArcheryClockTheme _instance;

    private Color backgroundColor;
    private Color fontColor;

    public static synchronized ArcheryClockTheme getInstance() {
        if (_instance == null) {
            _instance = new ArcheryClockTheme();
        }
        return _instance;
    }

    public static ArcheryClockTheme createDefaultTheme() {
        val theme = getInstance();
        theme.setBackgroundColor(Color.BLACK);
        theme.setBackgroundColor(Color.WHITE);

        return theme;
    }
}

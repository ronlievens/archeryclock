package com.github.ronlievens.archeryclock.ui.main;

import com.github.ronlievens.archeryclock.ui.font.RobotoMonoFont;
import com.github.ronlievens.archeryclock.ui.stream.StreamableJPanel;
import lombok.val;

import javax.swing.*;
import java.awt.*;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public final class MainPanel extends StreamableJPanel {

    public MainPanel() {
        super(1920, 1080, 25, new GridBagLayout());
        setBackground(Color.BLACK);
    }

    public void start() {
        val clock = new JLabel("clock");
        clock.setForeground(Color.GREEN);
        clock.setFont(RobotoMonoFont.get("bold", 72f));
        add(clock);

        val timer = new Timer(40, e -> {
            clock.setText(LocalTime.now().format(
                    DateTimeFormatter.ofPattern("HH:mm:ss")));

            repaint();
        });
        timer.start();

        setVisible(true);
        startStream();
    }
}

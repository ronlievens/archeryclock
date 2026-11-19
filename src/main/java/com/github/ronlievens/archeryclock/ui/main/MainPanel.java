package com.github.ronlievens.archeryclock.ui.main;

import com.github.ronlievens.archeryclock.ui.font.RobotoMonoFont;
import com.github.ronlievens.archeryclock.ui.stream.StreamableJPanel;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import javax.swing.*;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import java.awt.*;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

@Slf4j
public final class MainPanel extends StreamableJPanel {

    private Clip ac1Clip;

    public MainPanel() {
        super(1920, 1080, 25, new GridBagLayout());
        setBackground(Color.BLACK);
    }

    public void start() {
        val clock = new JLabel("clock");
        clock.setForeground(Color.GREEN);
        clock.setFont(RobotoMonoFont.get("bold", 72f));

        val constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.insets = new Insets(10, 10, 10, 10);
        add(clock, constraints);

        val ac1Button = new JButton("Speel ac1");
        ac1Button.setFont(RobotoMonoFont.get("bold", 24f));
        ac1Button.addActionListener(e -> playAc1());

        constraints.gridy = 1;
        add(ac1Button, constraints);

        val timer = new Timer(40, e -> {
            clock.setText(LocalTime.now().format(
                    DateTimeFormatter.ofPattern("HH:mm:ss")));

            repaint();
        });
        timer.start();

        setVisible(true);
        startStream();
    }

    private void playAc1() {
        try {
            if (ac1Clip != null) {
                ac1Clip.stop();
                ac1Clip.close();
            }

            val sound = getClass().getResource("/sounds/buzzer.wav");
            if (sound == null) {
                log.error("Sound resource /sounds/ac1.wav not found");
                return;
            }

            try (val inputStream = AudioSystem.getAudioInputStream(sound)) {
                ac1Clip = AudioSystem.getClip();
                ac1Clip.open(inputStream);
                ac1Clip.setFramePosition(0);
                ac1Clip.start();
            }
        } catch (Exception e) {
            log.error("Could not play /sounds/ac1.wav", e);
        }
    }
}

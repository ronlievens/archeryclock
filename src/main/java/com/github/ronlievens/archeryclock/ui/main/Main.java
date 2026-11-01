package com.github.ronlievens.archeryclock.ui.main;

import com.github.ronlievens.archeryclock.ui.font.RobotoMonoFont;
import lombok.val;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;

public record Main(JFrame frame,
                   JPanel panel) {


    private static final int WINDOW_WIDTH = 1920;
    private static final int WINDOW_HEIGHT = 1080;

    public static void createWindow() {
        val main = new Main(new JFrame("ArcheryClock"), new JPanel(new GridBagLayout()));
        main.start();
    }

    private void start() {
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        panel.setBackground(Color.BLACK);

        val welcome = new JLabel("Welkom");
        welcome.setForeground(Color.WHITE);
        welcome.setFont(RobotoMonoFont.get("bold", 72f));
        panel.add(welcome);

        frame.setContentPane(panel);

        installKeyboardShortcuts();

        setFullscreen();

        frame.setVisible(true);
    }

    private void installKeyboardShortcuts() {
        JRootPane root = frame.getRootPane();

        // F11: fullscreen / windowed
        root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
            .put(KeyStroke.getKeyStroke(KeyEvent.VK_F11, 0), "toggleFullscreen");

        root.getActionMap().put("toggleFullscreen", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                setFullscreen();
            }
        });

        // ESC: terug naar windowed
        root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
            .put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "windowed");

        root.getActionMap().put("windowed", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                setWindowed();
            }
        });
    }

//    private void toggleFullscreen() {
//        if (frame.getInputContext()) {
//            setWindowed();
//        } else {
//            setFullscreen();
//        }
//    }

    private void setFullscreen() {
//        fullscreen = true;

        frame.dispose();
        frame.setUndecorated(true);

        GraphicsDevice device =
            GraphicsEnvironment.getLocalGraphicsEnvironment()
                .getDefaultScreenDevice();

        Rectangle bounds = device.getDefaultConfiguration().getBounds();

        frame.setBounds(bounds);
        frame.setVisible(true);
    }

    private void setWindowed() {
//        fullscreen = false;

        frame.dispose();
        frame.setUndecorated(false);

        frame.setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

}

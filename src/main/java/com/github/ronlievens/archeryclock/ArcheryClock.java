package com.github.ronlievens.archeryclock;

import com.github.ronlievens.archeryclock.ui.font.RobotoMonoFont;
import com.github.ronlievens.archeryclock.ui.main.MainPanel;

import lombok.val;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;

@Slf4j
public final class ArcheryClock extends JFrame {

    private static final int WINDOW_WIDTH = 1920;
    private static final int WINDOW_HEIGHT = 1080;
    private boolean fullscreen;

    public static void createWindow() {
        val archeryClock = new ArcheryClock();
        archeryClock.setTitle("ArcheryClock");
        archeryClock.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        archeryClock.setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
        archeryClock.setLocationRelativeTo(null);
        archeryClock.setVisible(true);

        archeryClock.installKeyboardShortcuts();

        val mainPanel = new MainPanel();
        archeryClock.setContentPane(mainPanel);

        mainPanel.start();
    }

    private void installKeyboardShortcuts() {
        // F11: fullscreen / windowed
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke(KeyEvent.VK_F11, 0), "toggleFullscreen");

        getRootPane().getActionMap().put("toggleFullscreen", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                toggleFullscreen();
            }
        });

        // ESC: terug naar windowed
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "windowed");

        getRootPane().getActionMap().put("windowed", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                setWindowed();
            }
        });
    }

    private void toggleFullscreen() {
        if (fullscreen) {
            setWindowed();
        } else {
            setFullscreen();
        }
    }

    private void setFullscreen() {
        fullscreen = true;

        dispose();
        setUndecorated(true);

        GraphicsDevice device = GraphicsEnvironment.getLocalGraphicsEnvironment()
                .getDefaultScreenDevice();

        Rectangle bounds = device.getDefaultConfiguration().getBounds();

        setBounds(bounds);
        setVisible(true);
    }

    private void setWindowed() {
        fullscreen = false;

        dispose();
        setUndecorated(false);

        setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    static void main(final String... args) {
        // enable HiDPI-scaling for Java2D
        System.setProperty("sun.java2d.uiScale.enabled", "true");

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (UnsupportedLookAndFeelException
                | ClassNotFoundException
                | InstantiationException
                | IllegalAccessException e) {
            System.err.println("Unable to initialize native Look & Feel: " + e);
            throw new RuntimeException(e);
        }

        // Load all RoboMono fonts
        try {
            RobotoMonoFont.load();
        } catch (Exception e) {
            System.err.println("Unable to load fonts: " + e);
            throw new RuntimeException(e);
        }

        // Initialize the application on Swing's Event Dispatch Thread.
        log.trace("Starting ArcheryClock...");
        try {
            SwingUtilities.invokeAndWait(ArcheryClock::createWindow);
        } catch (Exception e) {
            System.err.println("Unable to initialize ArcheryClock: " + e);
            throw new RuntimeException(e);
        }
        // SwingUtilities.invokeLater(ArcheryClock::createWindow);
        log.trace("ArcheryClock started.");
    }
}

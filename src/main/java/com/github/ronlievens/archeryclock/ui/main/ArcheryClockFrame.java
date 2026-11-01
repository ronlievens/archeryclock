package com.github.ronlievens.archeryclock.ui.main;

import com.github.ronlievens.archeryclock.streaming.SwingPanelStreamer;
import com.github.ronlievens.archeryclock.ui.font.RobotoMonoFont;
import com.github.ronlievens.archeryclock.ui.theme.ArcheryClockTheme;
import lombok.val;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public final class ArcheryClockFrame extends JFrame {

    private static final int WINDOW_WIDTH = 1920;
    private static final int WINDOW_HEIGHT = 1080;

    private static ArcheryClockFrame _instance;

    private final JPanel mainPanel;
    private final SwingPanelStreamer streamer;
    private final ArcheryClockTheme theme;
    private boolean fullscreen;

    private ArcheryClockFrame() throws HeadlessException {
        super();
        setTitle("ArcheryClock");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        fullscreen = false;

        theme = ArcheryClockTheme.getInstance();

        mainPanel = new JPanel(new GridBagLayout());
        streamer = new SwingPanelStreamer(mainPanel);
        mainPanel.setBackground(theme.getBackgroundColor());
        setContentPane(mainPanel);


        installKeyboardShortcuts();
    }

    public static synchronized ArcheryClockFrame getInstance() {
        if (_instance == null) {
            _instance = new ArcheryClockFrame();
        }
        return _instance;
    }

    public static void createWindow() {
        getInstance().start();
        startStream();
    }

    private void start() {
        val welcome = new JLabel("Welkom");
        welcome.setForeground(theme.getBackgroundColor());
        welcome.setFont(RobotoMonoFont.get("bold", 72f));
        mainPanel.add(welcome);

        Timer timer = new Timer(40, e -> {
            welcome.setText(LocalTime.now().format(
                DateTimeFormatter.ofPattern("HH:mm:ss")
            ));

            mainPanel.repaint();
        });
        timer.start();

        setFullscreen();
        setVisible(true);
    }

    public static void startStream() {
        getInstance().streamer.start();
    }

    public static void stopStream() {
        getInstance().streamer.stop();
    }

    private void installKeyboardShortcuts() {
        // F11: fullscreen / windowed
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
            .put(KeyStroke.getKeyStroke(KeyEvent.VK_F11, 0), "toggleFullscreen");

        getRootPane().getActionMap().put("toggleFullscreen", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                setFullscreen();
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

        GraphicsDevice device =
            GraphicsEnvironment.getLocalGraphicsEnvironment()
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

}

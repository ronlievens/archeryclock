package com.github.ronlievens.archeryclock.ui.main;

import com.github.ronlievens.archeryclock.ui.main.traffic.TrafficLight;
import com.github.ronlievens.archeryclock.ui.stream.StreamableJPanel;
import lombok.val;

import java.awt.*;
import javax.swing.JPanel;
import javax.swing.Timer;

public final class MainPanel extends StreamableJPanel {

    private final TrafficLight trafficPanel;
    private final JPanel clockPanel;

    public MainPanel(final int height, final int width) {
        super(height, width, 25, new GridBagLayout());
        setBackground(Color.BLACK);

        trafficPanel = new TrafficLight();
        clockPanel = new ClockPanel();

        setSidePanelRight(false);

        // Timer voor automatisch wisselen
        Timer trafficTimer = new Timer(1000, e ->
            trafficPanel.next()
            
        );

        // Timer starten
        trafficTimer.start();
    }

    public void setSidePanelRight(boolean right) {

        removeAll();

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1.0;

        // Zijpaneel
        gbc.gridx = right ? 1 : 0;
        gbc.weightx = 0.2;
        add(trafficPanel, gbc);

        // Hoofdpaneel
        gbc.gridx = right ? 0 : 1;
        gbc.weightx = 0.8;
        add(clockPanel, gbc);

        revalidate();
        repaint();
    }

    private static GridBagConstraints createGridBagConstraints() {
        val gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.NORTH;
        gbc.insets = new Insets(5, 5, 5, 5);
        return gbc;
    }
}

package com.github.ronlievens.archeryclock.ui.main.traffic;

import lombok.val;

import javax.swing.*;
import java.awt.*;

import static java.awt.RenderingHints.*;

public class TrafficLight extends JPanel {

    private static final Color RED = Color.RED;
    private static final Color YELLOW = Color.YELLOW;
    private static final Color GREEN = Color.GREEN;
    private static final Color DARK = new Color(35, 38, 43);

    private TrafficState trafficState;

    public TrafficLight() {
        setOpaque(false);
        setMinimumSize(new Dimension(50, 150));

        trafficState = TrafficState.RED;
    }

    public void next() {
        trafficState = trafficState.next();
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        val g2 = (Graphics2D) g.create();
        g2.setRenderingHint(KEY_ANTIALIASING, VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(KEY_RENDERING, VALUE_RENDER_QUALITY);

        int w = getWidth();
        int h = getHeight();

        int diameter = Math.max(
            1,
            Math.min(w - 15, (h - 30) / 3)
        );

        int spacing = diameter + 15;
        int totalHeight = diameter * 3 + 30;

        int x = (w - diameter) / 2;
        int y = (h - totalHeight) / 2;

        drawLEDModule(g2, x, y, diameter, RED, trafficState.isRedActive());
        drawLEDModule(g2, x, y + spacing, diameter, YELLOW, trafficState.isYellowActive());
        drawLEDModule(g2, x, y + spacing * 2, diameter, GREEN, trafficState.isGreenActive());

        g2.dispose();
    }

    private void drawLEDModule(
        Graphics2D g,
        int x,
        int y,
        int size,
        Color color,
        boolean active
    ) {
        Graphics2D g2 = (Graphics2D) g.create();

        // Buitenste donkere behuizing
        g2.setColor(new Color(75, 75, 82));
        g2.fillOval(x, y, size, size);

        // Donkere lens
        g2.setColor(Color.BLACK);
        g2.fillOval(x + 3, y + 3, size - 6, size - 6);

        // LED-verdeling
        int center = size / 2;
        int radius = size / 2 - 10;

        int ledSize = Math.max(2, size / 13);

        if (active) {
            // Zachte lichtgloed
            g2.setColor(new Color(
                color.getRed(),
                color.getGreen(),
                color.getBlue(),
                35
            ));

            g2.fillOval(
                x + 4, y + 4,
                size - 8, size - 8
            );
        }

        // Middelste LED
        drawLED(g2,
            x + center - ledSize / 2,
            y + center - ledSize / 2,
            ledSize, color, active);

        // LED-ringen
        int[] counts = {6, 12, 18};

        for (int ring = 0; ring < counts.length; ring++) {

            int count = counts[ring];
            double r = radius * (ring + 1) / 3.3;

            for (int i = 0; i < count; i++) {

                double angle =
                    2 * Math.PI * i / count;

                int lx = (int) (
                    x + center + Math.cos(angle) * r
                );

                int ly = (int) (
                    y + center + Math.sin(angle) * r
                );

                drawLED(
                    g2,
                    lx - ledSize / 2,
                    ly - ledSize / 2,
                    ledSize,
                    color,
                    active
                );
            }
        }

        // Buitenste lensrand
        g2.setColor(new Color(115, 115, 125));
        g2.setStroke(new BasicStroke(2f));
        g2.drawOval(
            x + 3, y + 3,
            size - 6, size - 6
        );

        g2.dispose();
    }

    private void drawLED(
        Graphics2D g,
        int x,
        int y,
        int size,
        Color color,
        boolean active
    ) {

        // LED-behuizing
        g.setColor(new Color(20, 20, 24));
        g.fillOval(x - 1, y - 1, size + 2, size + 2);

        if (active) {

            // LED brandt
            g.setColor(color);
            g.fillOval(x, y, size, size);

            // Kleine lichtreflectie
            if (size >= 5) {
                g.setColor(new Color(
                    255, 255, 255, 120
                ));

                g.fillOval(
                    x + size / 4,
                    y + size / 5,
                    Math.max(1, size / 3),
                    Math.max(1, size / 4)
                );
            }

        } else {

            // Uitgeschakelde LED
            g.setColor(color.darker().darker().darker().darker());
            g.fillOval(x, y, size, size);
        }
    }
}


package com.github.ronlievens.archeryclock;

import com.github.ronlievens.archeryclock.ui.font.RobotoMonoFont;
import com.github.ronlievens.archeryclock.ui.main.ArcheryClockFrame;

import javax.swing.*;

public class ArcheryClock {

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
        SwingUtilities.invokeLater(ArcheryClockFrame::createWindow);

    }
}

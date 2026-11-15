package com.github.ronlievens.archeryclock.core;

import java.util.Locale;
import java.util.Objects;
import java.util.function.Consumer;

public final class RemoteCommandHandler {
    private final AppSettings settings;
    private final ArcherySession session;
    private final Consumer<String> scenarioSelector;

    public RemoteCommandHandler(
            AppSettings settings,
            ArcherySession session,
            Consumer<String> scenarioSelector) {
        this.settings = Objects.requireNonNull(settings);
        this.session = Objects.requireNonNull(session);
        this.scenarioSelector = Objects.requireNonNull(scenarioSelector);
    }

    public boolean handle(String message) {
        if (!settings.remoteControlEnabled() || message == null || message.length() != 12
                || !message.startsWith("ctr")
                || !message.substring(7).equals(checkValue(settings.remotePassword()))) {
            return false;
        }
        String command = message.substring(3, 7);
        if (command.equals("next")) {
            session.advance();
        } else if (command.equals("play")) {
            session.pauseResume();
        } else if (command.equals("stop")) {
            session.stop();
        } else if (command.equals("emer")) {
            session.triggerEmergency();
        } else if (command.equals("lear")) {
            session.adjustTurn(-1);
        } else if (command.equals("riar")) {
            session.adjustTurn(1);
        } else if (command.equals("upen")) {
            session.adjustSeries(1);
        } else if (command.equals("dnen")) {
            session.adjustSeries(-1);
        } else if (command.matches("pr[0-9]{2}")) {
            session.startPreparation(Integer.parseInt(command.substring(2)) * 60);
        } else if (command.matches("sof[1-9]")) {
            session.startShootOff(command.charAt(3) - '0');
        } else if (command.matches("sf([0-9]{2})")) {
            int scenario = Integer.parseInt(command.substring(2));
            if (scenario < 1 || scenario > 12) {
                return false;
            }
            scenarioSelector.accept("F" + scenario);
        } else {
            return false;
        }
        return true;
    }

    public static String encode(String command, String password) {
        Objects.requireNonNull(command);
        Objects.requireNonNull(password);
        if (command.length() != 4) {
            throw new IllegalArgumentException("Remote command must contain four characters");
        }
        return "ctr" + command + checkValue(password);
    }

    private static String checkValue(String password) {
        int sum = password.chars().sum();
        int checksum = Math.addExact(10000, sum);
        if (checksum > 99999) {
            throw new IllegalArgumentException("Remote password checksum is out of range");
        }
        return String.format(Locale.ROOT, "%05d", checksum);
    }
}

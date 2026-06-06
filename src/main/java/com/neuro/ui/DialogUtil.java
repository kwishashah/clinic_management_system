package com.neuro.ui;

import com.neuro.config.ClinicConfig;
import com.neuro.model.ClinicInfo;

import javax.swing.*;
import java.io.File;
import java.awt.Image;
import java.awt.Component;
public final class DialogUtil {

    private DialogUtil() {}

    private static Icon getClinicIcon() {
        try {
            ClinicInfo info = ClinicConfig.load();

            if (info != null
                    && info.getLogoPath() != null
                    && !info.getLogoPath().isBlank()) {

                File file = new File(info.getLogoPath());

                if (file.exists()) {
                    ImageIcon original = new ImageIcon(info.getLogoPath());

                    Image scaled = original.getImage().getScaledInstance(
                            32,
                            32,
                            Image.SCALE_SMOOTH);

                    return new ImageIcon(scaled);
                    //return new ImageIcon(info.getLogoPath());
                }
            }
        } catch (Exception ignored) {
        }

        return null;
    }

    public static void info(java.awt.Component parent, String message) {
        JOptionPane.showMessageDialog(
                parent,
                message,
                "Success",
                JOptionPane.PLAIN_MESSAGE,
                getClinicIcon());
    }

    public static void warning(java.awt.Component parent, String message) {
        JOptionPane.showMessageDialog(
                parent,
                message,
                "Warning",
                JOptionPane.PLAIN_MESSAGE,
                getClinicIcon());
    }

    public static void error(java.awt.Component parent, String message) {
        JOptionPane.showMessageDialog(
                parent,
                message,
                "Error",
                JOptionPane.PLAIN_MESSAGE,
                getClinicIcon());
    }
    public static boolean confirm(Component parent, String message, String title) {

        int result = JOptionPane.showOptionDialog(
                parent,
                message,
                title,
                JOptionPane.YES_NO_OPTION,
                JOptionPane.PLAIN_MESSAGE,
                getClinicIcon(),
                null,
                null);

        return result == JOptionPane.YES_OPTION;
    }
}
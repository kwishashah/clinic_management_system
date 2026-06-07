/*
 * Copyright (c) 2026. All rights reserved. contact kwisha.shah2004 for more details.
 */
package com.neuro.ui;

import com.neuro.config.ClinicConfig;
import com.neuro.model.ClinicInfo;
import java.awt.Image;
import java.awt.Taskbar;
import java.awt.Window;
import java.io.File;
import javax.imageio.ImageIO;
import javax.swing.JFrame;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Loads and applies the configured clinic logo as the application's window/taskbar/dock icon.
 *
 * <p>By default Swing displays the generic Java "coffee cup" icon in the OS taskbar (Windows/Linux)
 * and Dock (macOS). This helper resolves the logo configured via {@link ClinicConfig} and applies
 * it both to top-level windows ({@link JFrame#setIconImage(Image)}) and to the platform Taskbar
 * (via {@link Taskbar}, Java 9+).
 *
 * <p>If no logo is configured, or the file is missing/unreadable, the methods are silent no-ops:
 * the default Java icon remains, but nothing breaks. All failures are logged at WARN level.
 */
public final class AppIcon {
    private static final Logger logger = LogManager.getLogger(AppIcon.class);

    private AppIcon() {
        // Static utility; not instantiable.
    }

    /**
     * Loads the currently configured clinic logo, or returns {@code null} if no logo is configured
     * or the file cannot be read. The returned image is suitable for both
     * {@link JFrame#setIconImage(Image)} and {@link Taskbar#setIconImage(Image)}.
     */
    public static Image loadConfiguredLogo() {
        ClinicInfo info = ClinicConfig.load();
        if (info == null) {
            return null;
        }
        String path = info.getLogoPath();
        if (path == null || path.isEmpty()) {
            return null;
        }
        File file = new File(path);
        if (!file.isFile()) {
            logger.debug("Configured clinic logo not found at path={}", path);
            return null;
        }
        try {
            Image image = ImageIO.read(file);
            if (image == null) {
                logger.warn("Clinic logo could not be decoded path={}", path);
            }
            return image;
        } catch (Exception e) {
            logger.warn("Failed to read clinic logo path={}", path, e);
            return null;
        }
    }

    /**
     * Sets {@code window}'s icon to the configured clinic logo, if available. No-op if the window
     * is not a {@link JFrame} (icons on plain dialogs are inherited from their owner frame) or
     * if no logo is configured.
     */
    public static void applyToWindow(Window window) {
        if (!(window instanceof JFrame frame)) {
            return;
        }
        Image image = loadConfiguredLogo();
        if (image != null) {
            frame.setIconImage(image);
        }
    }

    /**
     * Sets the OS-level taskbar/dock icon (macOS Dock, Windows/Linux taskbar grouping icon).
     * Safe to call on any platform: on systems where {@link Taskbar} is unsupported, the call is
     * silently ignored.
     */
    public static void applyToTaskbar() {
        Image image = loadConfiguredLogo();
        if (image == null) {
            return;
        }
        try {
            if (!Taskbar.isTaskbarSupported()) {
                return;
            }
            Taskbar taskbar = Taskbar.getTaskbar();
            if (taskbar.isSupported(Taskbar.Feature.ICON_IMAGE)) {
                taskbar.setIconImage(image);
            }
        } catch (UnsupportedOperationException | SecurityException e) {
            logger.debug("Taskbar icon update not supported on this platform", e);
        } catch (Exception e) {
            logger.warn("Failed to apply taskbar icon", e);
        }
    }

    /** Convenience: applies the configured logo to both the given window and the OS taskbar. */
    public static void applyAll(Window window) {
        applyToWindow(window);
        applyToTaskbar();
    }
}

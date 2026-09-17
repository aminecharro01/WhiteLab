package ma.WhiteLab.mvc.ui.palette.utils;

import java.awt.Color;

/**
 * Centralized "modern minimal dental clinic" visual identity: a calm clinical
 * teal accent on white/off-white, used across the sidebar, buttons, and
 * global FlatLaf theme so every screen shares one consistent look.
 */
public final class AppTheme {

    private AppTheme() {}

    public static final Color PRIMARY        = new Color(0x0E, 0xA5, 0xA5); // dental teal
    public static final Color PRIMARY_DARK   = new Color(0x0B, 0x84, 0x84);
    public static final Color PRIMARY_LIGHT  = new Color(0x5E, 0xEA, 0xD4);

    public static final Color BACKGROUND     = new Color(0xF8, 0xFA, 0xFC); // off-white
    public static final Color SURFACE        = Color.WHITE;
    public static final Color BORDER         = new Color(0xE2, 0xE8, 0xF0);

    public static final Color TEXT_PRIMARY   = new Color(0x1E, 0x29, 0x3B); // slate-800
    public static final Color TEXT_SECONDARY = new Color(0x64, 0x74, 0x8B); // slate-500

    public static final Color DANGER         = new Color(0xEF, 0x44, 0x44);
    public static final Color SUCCESS        = new Color(0x22, 0xC5, 0x5E);
    public static final Color WARNING        = new Color(0xF5, 0x9E, 0x0B);
}

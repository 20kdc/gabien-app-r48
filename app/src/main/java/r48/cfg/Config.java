/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */
package r48.cfg;

import java.util.LinkedList;

import org.eclipse.jdt.annotation.Nullable;

import gabien.GaBIEn;
import gabien.GaBIEnUI;
import gabien.ui.FontManager;
import gabien.ui.theming.Theme;
import gabien.ui.theming.ThemingCentral;
import r48.cfg.FontSizes.FontSizeField;
import r48.tr.LanguageList;

/**
 * Here goes nothing...
 * Created 27th February 2023
 */
public class Config {
    public final FontSizes f = new FontSizes();

    public boolean windowingExternal;

    // This is the secondary image path which is *defaulted to*.
    public final LinkedList<String> secondaryImageLoadLocationBackup = new LinkedList<String>();
    // This is the root path which is *defaulted to*.
    public final LinkedList<String> rootPathBackup = new LinkedList<String>();

    // Requires special handling when changed, can only be changed from launcher as it reboots part of ILG
    public String language;

    // UI globals, see applyUIGlobals
    public @Nullable String fontOverride;
    public boolean fontOverrideUE8;
    public int borderTheme;

    public Config(boolean isMobile) {
        reset(isMobile);
    }

    public void reset(boolean isMobile) {
        try {
            for (final FontSizeField field : f.fields)
                field.accept(field.defValue);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        windowingExternal = false;
        fontOverride = GaBIEn.getDefaultNativeFontName();
        fontOverrideUE8 = false;
        borderTheme = 0;

        secondaryImageLoadLocationBackup.clear();
        rootPathBackup.clear();

        language = LanguageList.defaultLang;

        // If single-window, assume we're on Android,
        //  so the user probably wants to be able to use EasyRPG Player
        // If EasyRPG Player has an issue with this, please bring it up at any time, and I will change this.
        if (isMobile)
            rootPathBackup.add("easyrpg/games/R48 Game");
    }

    /**
     * Applies changes.
     */
    public void applyUIGlobals() {
        borderTheme %= ThemingCentral.themes.length;
        Theme base = ThemingCentral.themes[borderTheme];
        base = Theme.FM_GLOBAL.with(base, new FontManager(fontOverride, fontOverrideUE8));
        GaBIEnUI.sysThemeRoot.setThemeOverride(base);
        GaBIEn.sysCoreFontSize = f.gSysCoreTH;
    }
}

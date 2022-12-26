/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.toolsets.utils;

import gabien.ui.UIElement;

/**
 * Clickable site button.
 * Runnable represents a text updater (unused functionality as of right now, it kept malfunctioning)
 * Moved from inner class on 17th September 2022
 */
public abstract class CommandSite implements Runnable {
    public final UIElement element;
    public CommandSite(UIElement b) {
        element = b;
    }
}

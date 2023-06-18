/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */
package r48.wm;

import java.util.HashSet;

import gabien.ui.UIElement;
import gabien.ui.WindowCreatingUIElementConsumer;

/**
 * Used to keep the WindowManager honest.
 * Created 27th February 2023
 */
public class TrackedUITicker {
    private final WindowCreatingUIElementConsumer real;
    private final HashSet<UIElement> aliveWindows = new HashSet<>();

    public TrackedUITicker(WindowCreatingUIElementConsumer r) {
        real = r;
    }

    public void shakeOffDeadWindows() {
        HashSet<UIElement> ok = new HashSet<UIElement>();
        for (UIElement uie : real.runningWindows())
            if (aliveWindows.contains(uie))
                ok.add(uie);
        aliveWindows.clear();
        aliveWindows.addAll(ok);
    }

    public Iterable<UIElement> runningWindows() {
        return aliveWindows;
    }

    public void forceRemove(UIElement uie) {
        real.forceRemove(uie);
    }

    public void accept(UIElement uie, boolean fullscreen) {
        aliveWindows.add(uie);
        real.accept(uie, 1, fullscreen);
    }
}

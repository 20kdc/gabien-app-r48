/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package gabienapp;

import gabien.IPeripherals;
import gabien.ui.Rect;
import gabien.ui.UIElement;
import gabien.ui.UILabel;
import gabien.ui.UIScrollLayout;
import r48.FontSizes;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicReference;

/**
 * A loading screen that makes it clear stuff is actually happening.
 * (Think "zdoom on Windows".)
 * When doneInjector shows up, shuts itself down, and calls it.
 * Frankly... this code may as well not be run outside of Android, but for testing purposes it's good to keep the codepaths the same
 * Created on May 14, 2019.
 */
public class UIFancyInit extends UIElement.UIProxy {
    // Don't get particularly worked up about memory-safety here, it's not exactly *essential* and so long as the copy-to-local works...
    public static ConcurrentLinkedQueue<String> consoletronDataInput;
    public AtomicReference<Runnable> doneInjector = new AtomicReference<Runnable>();

    private final UIScrollLayout layout = new UIScrollLayout(true, FontSizes.generalScrollersize);

    public UIFancyInit() {
        super();
        consoletronDataInput = new ConcurrentLinkedQueue<String>();
        proxySetElement(layout, false);
        setForcedBounds(null, new Rect(0, 0, FontSizes.scaleGuess(400), FontSizes.scaleGuess(300)));
    }

    public static void submitToStdoutAndConsoletron(String text) {
        ConcurrentLinkedQueue<String> get = consoletronDataInput;
        if (get != null)
            get.add(text);
        System.out.println(text);
    }

    @Override
    public void update(double deltaTime, boolean selected, IPeripherals peripherals) {
        ConcurrentLinkedQueue<String> get = consoletronDataInput;
        if (get != null) {
            String st = get.poll();
            if (st != null) {
                layout.panelsAdd(new UILabel(st, FontSizes.launcherTextHeight));
                layout.scrollbar.scrollPoint = 1;
            }
        }
        super.update(deltaTime, selected, peripherals);
    }

    @Override
    public boolean requestsUnparenting() {
        return doneInjector.get() != null;
    }

    @Override
    public void onWindowClose() {
        consoletronDataInput = null;
        while (true) {
            try {
                Runnable r = doneInjector.get();
                if (r != null) {
                    r.run();
                    return;
                }
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}

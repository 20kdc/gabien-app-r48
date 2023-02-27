/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package gabienapp;

import gabien.IGrDriver;
import gabien.IPeripherals;
import gabien.ui.Rect;
import gabien.ui.UIElement;
import gabien.ui.UILabel;
import gabien.uslx.append.IConsumer;
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
public class UIFancyInit extends UIElement.UIProxy implements IConsumer<String> {
    // Don't get particularly worked up about memory-safety here, it's not exactly *essential* and so long as the copy-to-local works...
    private ConcurrentLinkedQueue<String> consoletronDataInput;
    public AtomicReference<Runnable> doneInjector = new AtomicReference<Runnable>();

    private final UILabel layout = new UILabel("", FontSizes.launcherTextHeight);
    private int ackDoneInjector = 0;

    public UIFancyInit() {
        super();
        consoletronDataInput = new ConcurrentLinkedQueue<String>();
        proxySetElement(layout, false);
        setForcedBounds(null, new Rect(0, 0, FontSizes.scaleGuess(400), FontSizes.scaleGuess(300)));
    }

    @Override
    public void accept(String t) {
        ConcurrentLinkedQueue<String> get = consoletronDataInput;
        if (get != null)
            get.add(t);
    }

    @Override
    public void update(double deltaTime, boolean selected, IPeripherals peripherals) {
        // The ordering here is important; the idea is to in *theory* get the consoletronDataInput finished before acknowledging.
        if (ackDoneInjector == 0)
            if (doneInjector.get() != null)
                ackDoneInjector = 1;
        ConcurrentLinkedQueue<String> get = consoletronDataInput;
        if (get != null) {
            boolean didThing = false;
            while (true) {
                String st = get.poll();
                if (st != null) {
                    layout.text = st;
                    didThing = true;
                } else {
                    break;
                }
            }
            if (didThing)
                runLayoutLoop();
        }
        super.update(deltaTime, selected, peripherals);
    }

    @Override
    public void render(IGrDriver igd) {
        super.render(igd);
        if (ackDoneInjector >= 1)
            ackDoneInjector++;
    }

    @Override
    public boolean requestsUnparenting() {
        return ackDoneInjector == 10;
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

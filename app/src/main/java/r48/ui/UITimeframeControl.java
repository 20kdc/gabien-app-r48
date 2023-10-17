/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.ui;

import gabien.ui.*;
import gabien.uslx.append.*;
import gabien.wsi.IPeripherals;
import r48.App;

/**
 * Handles frame management, copy/paste, etc.
 * Essentially an "outer structure control".
 * Notably, the actual current frame number is stored by the Root Panel since everything needs that.
 * Created on 2/17/17.
 */
public class UITimeframeControl extends App.Prx {
    public final Supplier<Integer> getFrameIdx, getFrameCount;
    public final Consumer<Integer> setFrameIdx;

    private double playTimer = 0;
    public int recommendedFramerate;

    public UILabel currentFrame = new UILabel("", app.f.rmaTimeframeTH);

    public UIAppendButton playController = new UIAppendButton(Art.Symbol.Play, currentFrame, new Runnable() {
        @Override
        public void run() {
        }
    }, app.f.rmaTimeframeTH);
    public UIAppendButton loopController = new UIAppendButton(Art.Symbol.Loop, playController, new Runnable() {
        @Override
        public void run() {
        }
    }, app.f.rmaTimeframeTH);
    public UIAppendButton hsController = new UIAppendButton(Art.Symbol.Div2, loopController, new Runnable() {
        @Override
        public void run() {
        }
    }, app.f.rmaTimeframeTH);
    public UIAppendButton tsController = new UIAppendButton(Art.Symbol.Div3, hsController, new Runnable() {
        @Override
        public void run() {
        }
    }, app.f.rmaTimeframeTH);

    // The rest of the toolbar is constructed in the constructor
    public UIElement toolbar = tsController;

    public UITimeframeControl(App app, Supplier<Integer> gfi, Supplier<Integer> gfc, Consumer<Integer> sfi, int framerate) {
        super(app);
        getFrameIdx = gfi;
        getFrameCount = gfc;
        setFrameIdx = sfi;
        playController.button.toggle = true;
        loopController.button.toggle = true;
        hsController.button.toggle = true;
        tsController.button.toggle = true;
        recommendedFramerate = framerate;
        toolbar = new UIAppendButton("<", toolbar, new Runnable() {
            @Override
            public void run() {
                setFrameIdx.accept(getFrameIdx.get() - 1);
            }
        }, app.f.rmaTimeframeTH);
        toolbar = new UIAppendButton(">", toolbar, new Runnable() {
            @Override
            public void run() {
                setFrameIdx.accept(getFrameIdx.get() + 1);
            }
        }, app.f.rmaTimeframeTH);

        proxySetElement(toolbar, true);
    }

    @Override
    public void update(double deltaTime, boolean selected, IPeripherals peripherals) {
        if (playController.button.state) {
            playTimer += deltaTime;
            double frameTime = 1.0d / recommendedFramerate;
            if (hsController.button.state)
                frameTime *= 2;
            if (tsController.button.state)
                frameTime *= 3;
            while (playTimer >= frameTime) {
                playTimer -= frameTime;
                int oldIdx = getFrameIdx.get();
                setFrameIdx.accept(oldIdx + 1);
                if (getFrameIdx.get() != (oldIdx + 1))
                    if (!loopController.button.state) {
                        playController.button.state = false;
                        break;
                    }
            }
        } else {
            playTimer = 0;
        }
        currentFrame.text = T.u.frameDisplay.r(getFrameIdx.get(), getFrameCount.get());

        super.update(deltaTime, selected, peripherals);
    }
}

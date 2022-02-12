/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.ui;

import gabien.IPeripherals;
import gabien.ui.*;
import r48.FontSizes;
import r48.dbs.TXDB;

/**
 * Handles frame management, copy/paste, etc.
 * Essentially an "outer structure control".
 * Notably, the actual current frame number is stored by the Root Panel since everything needs that.
 * Created on 2/17/17.
 */
public class UITimeframeControl extends UIElement.UIProxy {
    public final ISupplier<Integer> getFrameIdx, getFrameCount;
    public final IConsumer<Integer> setFrameIdx;

    private double playTimer = 0;
    public int recommendedFramerate;

    public UILabel currentFrame = new UILabel(TXDB.get("Loading..."), FontSizes.rmaTimeframeTextHeight);

    public UIAppendButton playController = new UIAppendButton(Art.Symbol.Play, currentFrame, new Runnable() {
        @Override
        public void run() {
        }
    }, FontSizes.rmaTimeframeTextHeight);
    public UIButton playControllerButton = playController.button;
    public UIAppendButton loopController = new UIAppendButton(Art.Symbol.Loop, playController, new Runnable() {
        @Override
        public void run() {
        }
    }, FontSizes.rmaTimeframeTextHeight);
    public UIButton loopControllerButton = loopController.button;
    public UIAppendButton hsController = new UIAppendButton(Art.Symbol.Div2, loopController, new Runnable() {
        @Override
        public void run() {
        }
    }, FontSizes.rmaTimeframeTextHeight);
    public UIButton hsControllerButton = hsController.button;

    public UIAppendButton tsController = new UIAppendButton(Art.Symbol.Div3, hsController, new Runnable() {
        @Override
        public void run() {
        }
    }, FontSizes.rmaTimeframeTextHeight);
    public UIButton tsControllerButton = tsController.button;

    // The rest of the toolbar is constructed in the constructor
    public UIElement toolbar = tsController;

    public UITimeframeControl(ISupplier<Integer> gfi, ISupplier<Integer> gfc, IConsumer<Integer> sfi, int framerate) {
        getFrameIdx = gfi;
        getFrameCount = gfc;
        setFrameIdx = sfi;
        playControllerButton.toggle = true;
        loopControllerButton.toggle = true;
        hsControllerButton.toggle = true;
        tsControllerButton.toggle = true;
        recommendedFramerate = framerate;
        toolbar = new UIAppendButton("<", toolbar, new Runnable() {
            @Override
            public void run() {
                setFrameIdx.accept(getFrameIdx.get() - 1);
            }
        }, FontSizes.rmaTimeframeTextHeight);
        toolbar = new UIAppendButton(">", toolbar, new Runnable() {
            @Override
            public void run() {
                setFrameIdx.accept(getFrameIdx.get() + 1);
            }
        }, FontSizes.rmaTimeframeTextHeight);

        proxySetElement(toolbar, true);
    }

    @Override
    public void update(double deltaTime, boolean selected, IPeripherals peripherals) {
        if (playControllerButton.state) {
            playTimer += deltaTime;
            double frameTime = 1.0d / recommendedFramerate;
            if (hsControllerButton.state)
                frameTime *= 2;
            if (tsControllerButton.state)
                frameTime *= 3;
            while (playTimer >= frameTime) {
                playTimer -= frameTime;
                int oldIdx = getFrameIdx.get();
                setFrameIdx.accept(oldIdx + 1);
                if (getFrameIdx.get() != (oldIdx + 1))
                    if (!loopControllerButton.state) {
                        playControllerButton.state = false;
                        break;
                    }
            }
        } else {
            playTimer = 0;
        }
        currentFrame.text = (getFrameIdx.get() + 1) + " / " + getFrameCount.get();

        super.update(deltaTime, selected, peripherals);
    }
}

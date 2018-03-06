/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.schema.specialized.genpos;

import gabien.IPeripherals;
import gabien.ui.UIButton;
import gabien.ui.UIElement;
import gabien.ui.UILabel;
import r48.AppMain;
import r48.FontSizes;
import r48.RubyIO;
import r48.dbs.TXDB;
import r48.ui.Art;
import r48.ui.UIAppendButton;

/**
 * Handles frame management, copy/paste, etc.
 * Essentially an "outer structure control".
 * Notably, the actual current frame number is stored by the Root Panel since everything needs that.
 * Created on 2/17/17.
 */
public class UITimeframeControl extends UIElement.UIProxy {
    public GenposAnimRootPanel rootPanel;
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

    public UITimeframeControl(GenposAnimRootPanel rp, int framerate) {
        playControllerButton.toggle = true;
        loopControllerButton.toggle = true;
        hsControllerButton.toggle = true;
        tsControllerButton.toggle = true;
        rootPanel = rp;
        recommendedFramerate = framerate;
        toolbar = new UIAppendButton("<", toolbar, new Runnable() {
            @Override
            public void run() {
                rootPanel.target.setFrameIdx(rootPanel.target.getFrameIdx() - 1);
                rootPanel.frameChanged();
            }
        }, FontSizes.rmaTimeframeTextHeight);
        toolbar = new UIAppendButton(">", toolbar, new Runnable() {
            @Override
            public void run() {
                rootPanel.target.setFrameIdx(rootPanel.target.getFrameIdx() + 1);
                rootPanel.frameChanged();
            }
        }, FontSizes.rmaTimeframeTextHeight);
        toolbar = new UIAppendButton(TXDB.get("Copy"), toolbar, new Runnable() {
            @Override
            public void run() {
                AppMain.theClipboard = new RubyIO().setDeepClone(rootPanel.target.getFrame());
            }
        }, FontSizes.rmaTimeframeTextHeight);
        toolbar = new UIAppendButton(TXDB.get("Paste"), toolbar, new Runnable() {
            @Override
            public void run() {
                if (rootPanel.target.acceptableForPaste(AppMain.theClipboard)) {
                    rootPanel.target.getFrame().setDeepClone(AppMain.theClipboard);
                    rootPanel.target.modifiedFrame();
                    rootPanel.frameChanged();
                }
            }
        }, FontSizes.rmaTimeframeTextHeight);
        toolbar = new UIAppendButton("+", toolbar, new Runnable() {
            @Override
            public void run() {
                rootPanel.target.insertFrame(new RubyIO().setDeepClone(rootPanel.target.getFrame()));
                rootPanel.frameChanged();
            }
        }, FontSizes.rmaTimeframeTextHeight);
        toolbar = new UIAppendButton("-", toolbar, new Runnable() {
            @Override
            public void run() {
                rootPanel.target.deleteFrame();
                rootPanel.frameChanged();
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
                int oldIdx = rootPanel.target.getFrameIdx();
                rootPanel.target.setFrameIdx(oldIdx + 1);
                rootPanel.frameChanged();
                if ((oldIdx + 1) != rootPanel.target.getFrameIdx())
                    if (!loopControllerButton.state) {
                        playControllerButton.state = false;
                        break;
                    }
            }
        } else {
            playTimer = 0;
        }
        currentFrame.text = (rootPanel.target.getFrameIdx() + 1) + " / " + rootPanel.target.getFrameCount();

        super.update(deltaTime, selected, peripherals);
    }
}

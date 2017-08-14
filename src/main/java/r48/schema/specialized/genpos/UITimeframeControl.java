/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */
package r48.schema.specialized.genpos;

import gabien.IGrInDriver;
import gabien.ui.*;
import r48.AppMain;
import r48.FontSizes;
import r48.RubyIO;
import r48.dbs.TXDB;
import r48.ui.UIAppendButton;

/**
 * Handles frame management, copy/paste, etc.
 * Essentially an "outer structure control".
 * Notably, the actual current frame number is stored by the Root Panel since everything needs that.
 * Created on 2/17/17.
 */
public class UITimeframeControl extends UIPanel {
    public GenposAnimRootPanel rootPanel;
    private double playTimer = 0;
    public int recommendedFramerate;

    public UILabel currentFrame = new UILabel(TXDB.get("Loading..."), FontSizes.rmaTimeframeTextHeight);

    public UIAppendButton playController = new UIAppendButton(TXDB.get("Play"), currentFrame, new Runnable() {
        @Override
        public void run() {
        }
    }, FontSizes.rmaTimeframeTextHeight);
    public UITextButton playControllerButton = playController.button.togglable();
    public UIAppendButton loopController = new UIAppendButton(TXDB.get("Loop"), playController, new Runnable() {
        @Override
        public void run() {
        }
    }, FontSizes.rmaTimeframeTextHeight);
    public UITextButton loopControllerButton = loopController.button.togglable();
    public UIAppendButton hsController = new UIAppendButton(TXDB.get("HS"), loopController, new Runnable() {
        @Override
        public void run() {
        }
    }, FontSizes.rmaTimeframeTextHeight);
    public UITextButton hsControllerButton = hsController.button.togglable();

    public UIAppendButton tsController = new UIAppendButton(TXDB.get("TS"), hsController, new Runnable() {
        @Override
        public void run() {
        }
    }, FontSizes.rmaTimeframeTextHeight);
    public UITextButton tsControllerButton = tsController.button.togglable();

    // The rest of the toolbar is constructed in the constructor
    public UIElement toolbar = tsController;

    public UITimeframeControl(GenposAnimRootPanel rp, int framerate) {
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
        toolbar = new UIAppendButton("C", toolbar, new Runnable() {
            @Override
            public void run() {
                AppMain.theClipboard = new RubyIO().setDeepClone(rootPanel.target.getFrame());
            }
        }, FontSizes.rmaTimeframeTextHeight);
        toolbar = new UIAppendButton("P", toolbar, new Runnable() {
            @Override
            public void run() {
                if (AppMain.theClipboard.type == 'o') {
                    if (AppMain.theClipboard.symVal.equals("RPG::Animation::Frame")) {
                        rootPanel.target.getFrame().setDeepClone(AppMain.theClipboard);
                        rootPanel.target.modifiedFrame();
                        rootPanel.frameChanged();
                    }
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

        allElements.add(toolbar);
        setBounds(toolbar.getBounds());
    }

    @Override
    public void setBounds(Rect r) {
        super.setBounds(r);
        toolbar.setBounds(new Rect(0, 0, r.width, r.height));
    }

    @Override
    public void updateAndRender(int ox, int oy, double deltaTime, boolean select, IGrInDriver igd) {
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
        currentFrame.Text = (rootPanel.target.getFrameIdx() + 1) + " / " + rootPanel.target.getFrameCount();
        super.updateAndRender(ox, oy, deltaTime, select, igd);
    }
}

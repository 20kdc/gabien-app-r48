/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */
package r48.schema.specialized.rmanim;

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
    public RMAnimRootPanel rootPanel;
    private double playTimer = 0;
    public int recommendedFramerate;

    public UILabel currentFrame = new UILabel(TXDB.get("Loading..."), FontSizes.rmaTimeframeFontSize);

    public UIAppendButton playController = new UIAppendButton(TXDB.get("Play"), currentFrame, new Runnable() {
        @Override
        public void run() {
        }
    }, FontSizes.rmaTimeframeFontSize);
    public UITextButton playControllerButton = playController.button.togglable();
    public UIAppendButton hsController = new UIAppendButton(TXDB.get("HS"), playController, new Runnable() {
        @Override
        public void run() {
        }
    }, FontSizes.rmaTimeframeFontSize);
    public UITextButton hsControllerButton = hsController.button.togglable();

    public UIAppendButton tsController = new UIAppendButton(TXDB.get("TS"), hsController, new Runnable() {
        @Override
        public void run() {
        }
    }, FontSizes.rmaTimeframeFontSize);
    public UITextButton tsControllerButton = tsController.button.togglable();

    // The rest of the toolbar is constructed in the constructor
    public UIElement toolbar = tsController;

    public UITimeframeControl(RMAnimRootPanel rp, int framerate) {
        rootPanel = rp;
        recommendedFramerate = framerate;
        toolbar = new UIAppendButton("<", toolbar, new Runnable() {
            @Override
            public void run() {
                rootPanel.frameIdx--;
                rootPanel.frameChanged();
            }
        }, FontSizes.rmaTimeframeFontSize);
        toolbar = new UIAppendButton(">", toolbar, new Runnable() {
            @Override
            public void run() {
                rootPanel.frameIdx++;
                rootPanel.frameChanged();
            }
        }, FontSizes.rmaTimeframeFontSize);
        toolbar = new UIAppendButton("C", toolbar, new Runnable() {
            @Override
            public void run() {
                AppMain.theClipboard = new RubyIO().setDeepClone(rootPanel.getFrame());
            }
        }, FontSizes.rmaTimeframeFontSize);
        toolbar = new UIAppendButton("P", toolbar, new Runnable() {
            @Override
            public void run() {
                if (AppMain.theClipboard.type == 'o') {
                    if (AppMain.theClipboard.symVal.equals("RPG::Animation::Frame")) {
                        rootPanel.getFrame().setDeepClone(AppMain.theClipboard);
                        rootPanel.updateNotify.run();
                        rootPanel.frameChanged();
                    }
                }
            }
        }, FontSizes.rmaTimeframeFontSize);
        toolbar = new UIAppendButton("+", toolbar, new Runnable() {
            @Override
            public void run() {
                rootPanel.insertFrame(new RubyIO().setDeepClone(rootPanel.getFrame()));
            }
        }, FontSizes.rmaTimeframeFontSize);
        toolbar = new UIAppendButton("-", toolbar, new Runnable() {
            @Override
            public void run() {
                rootPanel.deleteFrame();
            }
        }, FontSizes.rmaTimeframeFontSize);

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
                rootPanel.frameIdx++;
                rootPanel.frameChanged();
            }
        } else {
            playTimer = 0;
        }
        currentFrame.Text = (rootPanel.frameIdx + 1) + " / " + rootPanel.target.getInstVarBySymbol("@frames").arrVal.length;
        super.updateAndRender(ox, oy, deltaTime, select, igd);
    }
}

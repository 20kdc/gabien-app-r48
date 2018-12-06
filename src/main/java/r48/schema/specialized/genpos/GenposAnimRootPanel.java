/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.schema.specialized.genpos;

import gabien.ui.IConsumer;
import gabien.ui.ISupplier;
import gabien.ui.UIElement;
import gabien.ui.UISplitterLayout;
import r48.AppMain;
import r48.FontSizes;
import r48.RubyIO;
import r48.dbs.TXDB;
import r48.schema.util.ISchemaHost;
import r48.ui.UIAppendButton;
import r48.ui.UITimeframeControl;

import java.util.HashMap;

/**
 * Animation Software For Serious Animation Purposes.
 * ...for stick figure animation. Ignore the 'RM'.
 * AAAA
 * --+-
 * BB|C
 * BB|C
 * A: timeframe
 * B: Frame Editor
 * C: Cell Editor
 * Created on 2/17/17.
 */
public class GenposAnimRootPanel extends UIElement.UIProxy {
    public final IGenposAnim target;
    public final GenposAnimTweening tweening;
    public final GenposFramePanelController framePanelController;
    public final UITimeframeControl timeframe;

    private final HashMap<String, IGenposTweeningManagement.KeyTrack> propTracks = new HashMap<String, IGenposTweeningManagement.KeyTrack>();

    public GenposAnimRootPanel(IGenposAnim t, ISchemaHost launcher, int recommendedFramerate) {
        target = t;
        IGenposFrame frame = target.getFrameDisplay();
        // NOTE: This does a scan of the frames without calling frameChanged to run interpolation implication
        tweening = new GenposAnimTweening(t, frame);
        framePanelController = new GenposFramePanelController(frame, new IGenposTweeningManagement() {
            @Override
            public KeyTrack propertyKeytrack(int prop) {
                String k = prop + "@" + framePanelController.cellSelection.cellNumber;
                KeyTrack kt = propTracks.get(k);
                if (kt != null)
                    return kt;
                kt = tweening.getTrack(framePanelController.cellSelection.cellNumber, prop);
                propTracks.put(k, kt);
                return kt;
            }

            @Override
            public boolean propertyKeyed(int prop, KeyTrack track) {
                return track.track[target.getFrameIdx()];
            }

            @Override
            public void disablePropertyKey(int prop, KeyTrack track) {
                tweening.disablePropertyKey(target.getFrameIdx(), framePanelController.cellSelection.cellNumber, prop, track);
            }
        }, launcher);
        timeframe = new UITimeframeControl(new ISupplier<Integer>() {
            @Override
            public Integer get() {
                return target.getFrameIdx();
            }
        }, new ISupplier<Integer>() {
            @Override
            public Integer get() {
                return target.getFrameCount();
            }
        }, new IConsumer<Integer>() {
            @Override
            public void accept(Integer integer) {
                target.setFrameIdx(integer);
                frameChanged();
            }
        }, recommendedFramerate);

        UIElement toolbar = timeframe;

        toolbar = new UIAppendButton(TXDB.get("Copy"), toolbar, new Runnable() {
            @Override
            public void run() {
                AppMain.theClipboard = new RubyIO().setDeepClone(target.getFrame());
            }
        }, FontSizes.rmaTimeframeTextHeight);
        toolbar = new UIAppendButton(TXDB.get("Paste"), toolbar, new Runnable() {
            @Override
            public void run() {
                if (target.acceptableForPaste(AppMain.theClipboard)) {
                    target.getFrame().setDeepClone(AppMain.theClipboard);
                    target.modifiedFrames();
                    frameChanged();
                }
            }
        }, FontSizes.rmaTimeframeTextHeight);
        toolbar = new UIAppendButton("+", toolbar, new Runnable() {
            @Override
            public void run() {
                target.insertFrame(new RubyIO().setDeepClone(target.getFrame()));
                frameChanged();
            }
        }, FontSizes.rmaTimeframeTextHeight);
        toolbar = new UIAppendButton("-", toolbar, new Runnable() {
            @Override
            public void run() {
                target.deleteFrame();
                frameChanged();
            }
        }, FontSizes.rmaTimeframeTextHeight);

        proxySetElement(new UISplitterLayout(toolbar, framePanelController.rootLayout, true, 0), true);

        frameChanged();
    }

    @Override
    public String toString() {
        return TXDB.get("Animation Editor");
    }

    // This alerts everything to rebuild, but doesn't run the updateNotify.
    // Use alone for things like advancing through frames.
    public void frameChanged() {
        // This does bounds checks
        target.setFrameIdx(target.getFrameIdx());
        // Actually start alerting things
        framePanelController.frameChanged();
    }

    public void incomingModification() {
        propTracks.clear();
        frameChanged();
    }
}

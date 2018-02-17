/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.schema.specialized.genpos;

import gabien.ui.UIElement;
import gabien.ui.UISplitterLayout;
import r48.dbs.TXDB;
import r48.schema.util.ISchemaHost;

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
    public final GenposFramePanelController framePanelController;
    public final UITimeframeControl timeframe;

    public GenposAnimRootPanel(IGenposAnim t, ISchemaHost launcher, int recommendedFramerate) {
        target = t;

        framePanelController = new GenposFramePanelController(target.getFrameDisplay(), launcher);
        timeframe = new UITimeframeControl(this, recommendedFramerate);

        proxySetElement(new UISplitterLayout(timeframe, framePanelController.rootLayout, true, 1), true);

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
}

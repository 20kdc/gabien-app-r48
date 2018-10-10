/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.schema.specialized.genpos2;

import gabien.IGrDriver;
import gabien.IPeripherals;
import gabien.ui.*;
import r48.AppMain;
import r48.FontSizes;

/**
 * Created on October 10, 2018.
 */
public class UIGenpos2 extends UIElement.UIProxy {
    public final GP2File file;
    public final IGP2Renderer renderer;
    public GP2Cell selectedCell;
    public final UIScrollLayout timeline;
    public final int columnWidth = 8, rowHeight = 8;
    public int currentFrame;

    public UIGenpos2(GP2File gp2File, IGP2Renderer r) {
        file = gp2File;
        renderer = r;
        UIScrollLayout usl = new UIScrollLayout(true, FontSizes.generalScrollersize);
        timeline = new UIScrollLayout(false, FontSizes.generalScrollersize);
        selectedCell = gp2File.allObjects.getFirst();
        resetTimeline();
        usl.panelsAdd(timeline);
        usl.runLayoutLoop();
        proxySetElement(new UISplitterLayout(new UIGP2View(this), usl, true, 1), true);
        setForcedBounds(null, new Rect(0, 0, AppMain.mainWindowWidth / 2, AppMain.mainWindowHeight / 2));
    }

    public void resetTimeline() {
        timeline.panelsClear();
        if (selectedCell == null)
            return;
        for (int i = 0; i < file.setLength; i++)
            timeline.panelsAdd(new UIGP2Frame(i, selectedCell));
        timeline.runLayoutLoop();
    }

    private class UIGP2Frame extends UIElement {
        private final GP2Cell cell;
        private final int frame;

        public UIGP2Frame(int i, GP2Cell cel) {
            super(columnWidth, rowHeight * (cel.fields.length + 1));
            frame = i;
            cell = cel;
        }

        @Override
        public void update(double deltaTime, boolean selected, IPeripherals peripherals) {

        }

        @Override
        public void handlePointerBegin(IPointer state) {
            if (state.getType() == IPointer.PointerType.Generic)
                currentFrame = frame;
        }

        @Override
        public void render(IGrDriver igd) {
            for (int i = 0; i < cell.fields.length; i++) {
                GP2Timeline.TimePoint res = cell.fields[i].getPrevPoint(frame);
                if (res == null)
                    continue;
                if (res.frame == frame)
                    igd.clearRect(255, 255, 0, 0, i * rowHeight, columnWidth, rowHeight);
            }
            if (currentFrame == frame)
                igd.clearRect(255, 0, 0, columnWidth / 4, 0, columnWidth / 2, getSize().height);
        }
    }
}

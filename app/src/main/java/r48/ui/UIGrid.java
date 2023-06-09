/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.ui;

import gabien.FontManager;
import gabien.IDesktopPeripherals;
import gabien.IGrDriver;
import gabien.IPeripherals;
import gabien.ui.*;
import r48.App;

/**
 * Notably, despite the name of "uiGridScaleTenths", this does NOT actually do the grid size adjustment.
 * It's too particular for that.
 * Created on 12/28/16.
 */
public class UIGrid extends UIElement.UIPanel implements OldMouseEmulator.IOldMouseReceiver {
    public int tileCount;
    public int tileSizeW, tileSizeH;

    public int bkgR = 0;
    public int bkgG = 0;
    public int bkgB = 0;

    // Region selection.
    public boolean canMultiSelect = false;
    private int selTile = 0;
    public int selWidth = 1;
    public int selHeight = 1;

    private int tmWidth = 8;

    public UIScrollbar uivScrollbar;

    public Runnable onSelectionChange = null;
    public OldMouseEmulator mouseEmulator = new OldMouseEmulator(this);

    public final App app;

    public UIGrid(App app, int tSizeW, int tSizeH, int tCount) {
        super(app.f.scaleGuess(320), app.f.scaleGuess(200));
        this.app = app;
        uivScrollbar = new UIScrollbar(true, app.f.gridS);
        tileSizeW = tSizeW;
        tileSizeH = tSizeH;
        tileCount = tCount;
        layoutAddElement(uivScrollbar);
    }

    private int getScrollOffset() {
        if (tmWidth <= 0)
            return 0;
        int totalRows = tileCount;
        if (totalRows % tmWidth > 0)
            totalRows += tmWidth;
        totalRows /= tmWidth;
        int screenRows = getSize().height / tileSizeH;
        int extraRows = totalRows - screenRows;
        return ((int) Math.floor((uivScrollbar.scrollPoint * extraRows) + 0.5)) * tmWidth;
    }

    @Override
    public void update(double deltaTime, boolean selected, IPeripherals peripherals) {
        super.update(deltaTime, selected, peripherals);
        if (peripherals instanceof IDesktopPeripherals) {
            mouseEmulator.mouseX = ((IDesktopPeripherals) peripherals).getMouseX();
            mouseEmulator.mouseY = ((IDesktopPeripherals) peripherals).getMouseY();
        }
    }

    @Override
    public void renderLayer(IGrDriver igd, UILayer layer) {
        Size r = getSize();
        if (layer == UILayer.Clear)
            igd.clearRect(bkgR, bkgG, bkgB, 0, 0, r.width, r.height);
        super.renderLayer(igd, layer);
        if (layer != UILayer.Content)
            return;

        if (tmWidth <= 0)
            return;

        int mouseSel = -1;
        int mouseX = mouseEmulator.mouseX;
        int mouseY = mouseEmulator.mouseY;
        if (new Rect(0, 0, tileSizeW * tmWidth, r.height).contains(mouseX, mouseY)) {
            int tx = UIElement.sensibleCellDiv(mouseX, tileSizeW);
            int ty = UIElement.sensibleCellDiv(mouseY, tileSizeH);
            mouseSel = tx + (ty * tmWidth) + getScrollOffset();
        }

        int pi = 0;
        int visibleTiles = (((r.height + (tileSizeH - 1)) / tileSizeH) * tmWidth);
        int scrollOffset = getScrollOffset();
        for (int p = scrollOffset; p < scrollOffset + visibleTiles; p++) {
            int px = ((pi % tmWidth) * tileSizeW);
            int py = (UIGrid.sensibleCellDiv(pi, tmWidth) * tileSizeH);
            if (py >= r.height)
                break;
            if ((p < 0) || (p >= tileCount)) {
                pi++;
                // error
                igd.clearRect(128, 0, 0, px, py, tileSizeW, tileSizeH);
                continue;
            }
            if (p == selTile)
                igd.clearRect(128, 0, 128, px, py, tileSizeW, tileSizeH);
            drawTile(p, p == mouseSel, px, py, igd);
            if (p == selTile)
                Art.drawSelectionBox(px, py, tileSizeW, tileSizeH, app.f.getSpriteScale(), igd);
            pi++;
        }
        for (int ty = 0; ty < selHeight; ty++) {
            pi = (selTile - getScrollOffset()) + (ty * tmWidth);
            for (int tx = 0; tx < selWidth; tx++) {
                int px = (pi % tmWidth) * tileSizeW;
                int py = UIGrid.sensibleCellDiv(pi, tmWidth) * tileSizeH;
                if (py < 0)
                    continue;
                if (py >= r.height)
                    continue;
                Art.drawSelectionBox(px, py, tileSizeW, tileSizeH, app.f.getSpriteScale(), igd);
                pi++;
            }
        }
    }

    protected void drawTile(int t, boolean hover, int x, int y, IGrDriver igd) {
        FontManager.drawString(igd, x, y + 1, Integer.toHexString(t).toUpperCase(), false, false, app.f.gridTH);
    }

    @Override
    public void runLayout() {
        int scrollBarW = uivScrollbar.getWantedSize().width;
        Size r = getSize();
        int tiles = (r.width - scrollBarW) / tileSizeW;
        if (tiles < 2)
            tiles = 2;
        int availableRows = r.height / tileSizeH;
        if (availableRows < 1)
            availableRows = 1;
        tmWidth = tiles;
        uivScrollbar.setForcedBounds(this, new Rect(r.width - scrollBarW, 0, scrollBarW, availableRows * tileSizeH));
        setWantedSize(new Size(r.width, availableRows * tileSizeH));
    }

    @Override
    public void handleClick(int x, int y, int button) {
        if (x < tileSizeW * tmWidth) {
            int tx = UIElement.sensibleCellDiv(x, tileSizeW);
            int ty = UIElement.sensibleCellDiv(y, tileSizeH);
            selTile = tx + (ty * tmWidth) + getScrollOffset();
            selWidth = 1;
            selHeight = 1;
            selectionChanged();
        }
    }

    @Override
    public void handleDrag(int x, int y) {
        if (tmWidth <= 0)
            return;
        if (x < tileSizeW * tmWidth) {
            if (!canMultiSelect)
                return;
            int tx = UIElement.sensibleCellDiv(x, tileSizeW);
            int ty = UIElement.sensibleCellDiv(y, tileSizeH) + (getScrollOffset() / tmWidth);
            int ox = selTile % tmWidth;
            int oy = selTile / tmWidth;
            selWidth = (tx - ox) + 1;
            if (selWidth < 1)
                selWidth = 1;
            selHeight = (ty - oy) + 1;
            if (selHeight < 1)
                selHeight = 1;
            selectionChanged();
        }
    }

    @Override
    public void handleRelease(int x, int y) {

    }

    @Override
    public void handleMousewheel(int x, int y, boolean north) {
        uivScrollbar.handleMousewheel(x, y, north);
    }

    @Override
    public IPointerReceiver handleNewPointer(IPointer state) {
        // UIPanel returns null to allow this trick to work.
        IPointerReceiver ipr = super.handleNewPointer(state);
        if (ipr != null)
            return ipr;
        return mouseEmulator;
    }

    private void selectionChanged() {
        if (selTile < 0)
            selTile = 0;
        if (selTile >= tileCount)
            selTile = tileCount - 1;
        if (onSelectionChange != null)
            onSelectionChange.run();
    }

    public int getSelectStride() {
        return tmWidth;
    }

    public int getSelected() {
        return selTile;
    }

    public void setSelected(int i) {
        if (i < 0)
            i = 0;
        if (i >= tileCount)
            i = tileCount - 1;
        selTile = i;
        selWidth = 1;
        selHeight = 1;
        selectionChanged();
        // work out a general estimate
        uivScrollbar.scrollPoint = i / (double) tileCount;
    }
}

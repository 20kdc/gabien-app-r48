/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */
package r48.schema.specialized.genpos;

import gabien.GaBIEn;
import gabien.IGrInDriver;
import gabien.ui.Rect;
import gabien.ui.UIPanel;
import r48.AppMain;
import r48.ArrayUtils;
import r48.RubyIO;
import r48.RubyTable;
import r48.dbs.ObjectDB;
import r48.dbs.TXDB;
import r48.schema.util.SchemaPath;
import r48.ui.UINSVertLayout;

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
 * The 3-pane layout is controlled entirely from this class. Good luck.
 * Created on 2/17/17.
 */
public class RMAnimRootPanel extends UIPanel implements IGenposFrame {
    public RubyIO target;
    public Runnable updateNotify;
    public SpriteCache spriteCache = new SpriteCache();
    public GenposFramePanelController framePanelController;
    public UITimeframeControl timeframe;
    public int frameIdx = 0;

    // used for indicator setup
    public boolean vxaAnim;

    public RMAnimRootPanel(RubyIO t, boolean vxaAnimation, Runnable runnable, String a, String b, int recommendedFramerate) {
        vxaAnim = vxaAnimation;
        target = t;
        updateNotify = runnable;
        spriteCache.target = target;
        spriteCache.framesetALoc = a;
        spriteCache.framesetBLoc = b;
        // Stop animation elements escaping the window
        useScissoring = true;

        framePanelController = new GenposFramePanelController(this);
        timeframe = new UITimeframeControl(this, recommendedFramerate);

        allElements.add(timeframe);
        allElements.add(framePanelController.rootLayout);

        // uhoh.
        spriteCache.prepareFramesetCache();

        frameChanged();
    }

    @Override
    public void setBounds(Rect r) {
        super.setBounds(r);
        int th = timeframe.getBounds().height;
        timeframe.setBounds(new Rect(0, 0, r.width, th));
        framePanelController.rootLayout.setBounds(new Rect(0, th, r.width, r.height - th));
    }

    public RubyIO getFrame() {
        RubyIO[] frames = target.getInstVarBySymbol("@frames").arrVal;
        if (frames.length == 0) {
            // Create a frame from scratch to avoid crashing
            RubyIO copy = SchemaPath.createDefaultValue(AppMain.schemas.getSDBEntry("RPG::Animation::Frame"), null);
            frameIdx = -1;
            insertFrame(copy);
            frameChanged();
            return copy;
        }
        if (frameIdx < 0) {
            frameIdx = frames.length - 1;
            frameChanged();
        }
        if (frameIdx >= frames.length) {
            frameIdx = 0;
            frameChanged();
        }
        return frames[frameIdx];
    }

    @Override
    public int[] getIndicators() {
        int halfSW = 320; // xp.320
        int halfSH = 240; // xp.240
        if (vxaAnim) {
            halfSW = 272;
            halfSH = 208;
        }
        return new int[] {
                // Centre (Character)
                0, 0,
                // Screen
                halfSW, halfSH,
                halfSW, -halfSH,
                -halfSW, halfSH,
                -halfSW, -halfSH,
        };
    }

    private RubyTable getTable() {
        RubyIO frameData = getFrame().getInstVarBySymbol("@cell_data");
        return new RubyTable(frameData.userVal);
    }

    @Override
    public void deleteCell(int i2) {
        RubyIO frame = getFrame();
        RubyIO frameData = frame.getInstVarBySymbol("@cell_data");
        RubyTable table = new RubyTable(frameData.userVal);
        frame.getInstVarBySymbol("@cell_max").fixnumVal = table.width - 1;
        RubyTable newTable = new RubyTable(table.width - 1, 8, 1, new int[1]);
        for (int p = 0; p < 8; p++) {
            for (int j = 0; j < i2; j++)
                newTable.setTiletype(j, p, 0, table.getTiletype(j, p, 0));
            for (int j = i2 + 1; j < table.width; j++)
                newTable.setTiletype(j - 1, p, 0, table.getTiletype(j, p, 0));
        }
        frameData.userVal = newTable.innerBytes;
        updateNotify.run();
    }

    @Override
    public int getCellProp(int ct, int i) {
        return getTable().getTiletype(ct, i, 0);
    }

    @Override
    public void setCellProp(int ct, int i, short number) {
        getTable().setTiletype(ct, i, 0, number);
        updateNotify.run();
    }

    @Override
    public int getCellCount() {
        return getTable().width;
    }

    @Override
    public void addCell(int i2) {
        RubyIO frame = getFrame();
        RubyIO frameData = frame.getInstVarBySymbol("@cell_data");
        RubyTable table = new RubyTable(frameData.userVal);
        frame.getInstVarBySymbol("@cell_max").fixnumVal = table.width + 1;
        RubyTable newTable = new RubyTable(table.width + 1, 8, 1, new int[1]);
        short[] initValues = new short[] {
                1, 0, 0, 100, 0, 0, 255, 1
        };
        for (int p = 0; p < 8; p++) {
            for (int j = 0; j < i2; j++)
                newTable.setTiletype(j, p, 0, table.getTiletype(j, p, 0));
            for (int j = i2; j < table.width; j++)
                newTable.setTiletype(j + 1, p, 0, table.getTiletype(j, p, 0));
            newTable.setTiletype(i2, p, 0, initValues[p]);
        }
        frameData.userVal = newTable.innerBytes;
        updateNotify.run();
    }

    @Override
    public String[] getCellProps() {
        return new String[] {
                TXDB.get("cellID"),
                TXDB.get("xPos"),
                TXDB.get("yPos"),
                TXDB.get("scale"),
                TXDB.get("angle"),
                TXDB.get("mirror"),
                TXDB.get("opacity"),
                TXDB.get("blendType")
        };
    }

    @Override
    public void drawCell(int i, int opx, int opy, IGrInDriver igd) {
        RubyTable rt = getTable();
        // Slightly less unfinished.

        // In the target versions, 7 is blend_type, 6 is opacity (0-255), 5 is mirror (int_boolean),
        // 4 is angle, 3 is scale (0-100), x is modified by 1, y is modified by 2.
        // 0 is presumably cell-id.

        int cell = rt.getTiletype(i, 0, 0);
        boolean mirror = rt.getTiletype(i, 5, 0) != 0;
        int opacity = Math.min(Math.max(rt.getTiletype(i, 6, 0), 0), 255);
        if (opacity == 0)
            return;
        IGrInDriver.IImage scaleImage;
        if (cell >= 100) {
            cell -= 100;
            scaleImage = spriteCache.getFramesetCache(true, mirror, opacity);
        } else {
            scaleImage = spriteCache.getFramesetCache(false, mirror, opacity);
        }
        int angle = rt.getTiletype(i, 4, 0);
        int scale = rt.getTiletype(i, 3, 0);
        int ts = spriteCache.getScaledImageIconSize(scale);
        int ofx = rt.getTiletype(i, 1, 0) - (ts / 2);
        int ofy = rt.getTiletype(i, 2, 0) - (ts / 2);
        int cellX = (cell % 5) * 192;
        int cellY = (cell / 5) * 192;
        // try to avoid using rotated images
        if (scaleImage != null) {
            if ((angle % 360) == 0) {
                igd.blitScaledImage(cellX, cellY, 192, 192, opx + ofx, opy + ofy, ts, ts, scaleImage);
            } else {
                igd.blitRotatedScaledImage(cellX, cellY, 192, 192, opx + ofx, opy + ofy, ts, ts, angle, scaleImage);
            }
        }
    }

    @Override
    public IGrInDriver.IImage getBackground() {
        return null;
    }

    @Override
    public String toString() {
        return TXDB.get("Animation Editor");
    }

    public void insertFrame(RubyIO source) {
        ArrayUtils.insertRioElement(target.getInstVarBySymbol("@frames"), source, frameIdx + 1);
        updateNotify.run();
        frameIdx++;
        frameChanged();
    }

    public void deleteFrame() {
        ArrayUtils.removeRioElement(target.getInstVarBySymbol("@frames"), frameIdx);
        updateNotify.run();
        frameIdx--;
        frameChanged();
    }

    // This alerts everything to rebuild, but doesn't run the updateNotify.
    // Use alone for things like advancing through frames.
    public void frameChanged() {
        // This does bounds checks
        getFrame();
        // Actually start alerting things
        framePanelController.frameChanged();
    }
}

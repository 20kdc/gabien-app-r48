/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */
package r48.schema.specialized.genpos.backend;

import gabien.IGrInDriver;
import gabien.ui.IFunction;
import gabien.ui.ISupplier;
import gabien.ui.Rect;
import r48.AppMain;
import r48.RubyIO;
import r48.RubyTable;
import r48.dbs.TXDB;
import r48.schema.SchemaElement;
import r48.schema.integers.IntegerSchemaElement;
import r48.schema.specialized.IMagicalBinder;
import r48.schema.specialized.MagicalBindingSchemaElement;
import r48.schema.specialized.genpos.IGenposFrame;
import r48.schema.util.SchemaPath;

/**
 * This exists so that I can try and reuse RMGenposAnim code for the 2k3 animations,
 *  while keeping this unreusable code here.
 * Created on 29/07/17.
 */
public class RGSSGenposFrame implements IGenposFrame {
    public SpriteCache spriteCache;

    // used for indicator setup
    public boolean vxaAnim;

    public Runnable updateNotify;
    public SchemaPath path;

    // Must be initialized before this is used...
    public ISupplier<RubyIO> frameSource;

    public RGSSGenposFrame(SpriteCache sc, SchemaPath basePath, boolean vxaAnimation, Runnable runnable) {
        updateNotify = runnable;
        vxaAnim = vxaAnimation;
        path = basePath;

        spriteCache = sc;
    }

    public RubyIO getFrame() {
        return frameSource.get();
    }

    private RubyTable getTable() {
        RubyIO frameData = getFrame().getInstVarBySymbol("@cell_data");
        return new RubyTable(frameData.userVal);
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

    @Override
    public boolean canAddRemoveCells() {
        return true;
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

    private SchemaElement[] getCellPropSchemas() {
        return new SchemaElement[] {
                new IntegerSchemaElement(0),
                new IntegerSchemaElement(0),
                new IntegerSchemaElement(0),
                new IntegerSchemaElement(0),
                new IntegerSchemaElement(0),
                new IntegerSchemaElement(0),
                new IntegerSchemaElement(0),
                new IntegerSchemaElement(0)
        };
    }

    @Override
    public SchemaPath getCellProp(final int ct, final int i) {
        // oh, this'll be *hilarious*. NOT.
        SchemaElement se = new MagicalBindingSchemaElement(new IMagicalBinder() {
            @Override
            public RubyIO targetToBound(RubyIO target) {
                short val = new RubyTable(target.userVal).getTiletype(ct, i, 0);
                return new RubyIO().setFX(val);
            }

            @Override
            public boolean applyBoundToTarget(RubyIO bound, RubyIO target) {
                short s = new RubyTable(target.userVal).getTiletype(ct, i, 0);
                short s2 = (short) bound.fixnumVal;
                if (s != s2) {
                    new RubyTable(target.userVal).setTiletype(ct, i, 0, s2);
                    return true;
                }
                return false;
            }
        }, getCellPropSchemas()[i]);
        return path.newWindow(se, getFrame().getInstVarBySymbol("@cell_data"), null);
    }

    @Override
    public void moveCell(int ct, IFunction<Integer, Integer> x, IFunction<Integer, Integer> y) {
        RubyTable rt = new RubyTable(getFrame().getInstVarBySymbol("@cell_data").userVal);
        rt.setTiletype(ct, 1, 0, (short) ((int) x.apply((int) rt.getTiletype(ct, 1, 0))));
        rt.setTiletype(ct, 2, 0, (short) ((int) y.apply((int) rt.getTiletype(ct, 2, 0))));
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
    public Rect getCellSelectionIndicator(int i) {
        RubyTable rt = getTable();
        int x = (int) rt.getTiletype(i, 1, 0);
        int y = (int) rt.getTiletype(i, 2, 0);
        int scale = rt.getTiletype(i, 3, 0);
        int ts = spriteCache.getScaledImageIconSize(scale);
        return new Rect(x - (ts / 2), y - (ts / 2), ts, ts);
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
}
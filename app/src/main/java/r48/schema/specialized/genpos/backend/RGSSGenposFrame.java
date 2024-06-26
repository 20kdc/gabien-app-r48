/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.schema.specialized.genpos.backend;

import java.util.function.Function;
import java.util.function.Supplier;

import gabien.render.IGrDriver;
import gabien.render.IImage;
import gabien.uslx.append.*;
import gabien.uslx.io.ByteArrayMemoryish;
import r48.App;
import r48.RubyTable;
import r48.RubyTableR;
import r48.io.data.IRIO;
import r48.schema.SchemaElement;
import r48.schema.integers.IntegerSchemaElement;
import r48.schema.specialized.RubyTableIndividualCellSchemaElement;
import r48.schema.specialized.SpritesheetCoreSchemaElement;
import r48.schema.specialized.genpos.IGenposFrame;
import r48.schema.specialized.genpos.IGenposTweeningProp;
import r48.schema.specialized.genpos.TableGenposTweeningProp;
import r48.schema.util.SchemaPath;
import r48.ui.dialog.ISpritesheetProvider;

/**
 * This exists so that I can try and reuse RMGenposAnim code for the 2k3 animations,
 * while keeping this unreusable code here.
 * Created on 29/07/17.
 */
public class RGSSGenposFrame extends App.Svc implements IGenposFrame {
    public SpriteCache spriteCache;

    // used for indicator setup
    public boolean vxaAnim;

    public Runnable updateNotify;
    public SchemaPath path;

    // Must be initialized before this is used...
    public Supplier<IRIO> frameSource;

    public RGSSGenposFrame(App app, SpriteCache sc, SchemaPath basePath, boolean vxaAnimation, Runnable runnable) {
        super(app);
        updateNotify = runnable;
        vxaAnim = vxaAnimation;
        path = basePath;

        spriteCache = sc;
    }

    @Override
    public boolean isStillValid() {
        return true;
    }

    public IRIO getFrame() {
        return frameSource.get();
    }

    private RubyTableR getTable() {
        IRIO frameData = getFrame().getIVar("@cell_data");
        return new RubyTableR(frameData.getBuffer());
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
        IRIO frame = getFrame();
        IRIO frameData = frame.getIVar("@cell_data");
        RubyTable table = new RubyTable(frameData.editUser());
        frame.getIVar("@cell_max").setFX(table.width - 1);
        ByteArrayMemoryish newTableBAM = RubyTable.initNewTable(3, table.width - 1, 8, 1, new int[1]);
        RubyTable newTable = new RubyTable(newTableBAM);
        for (int p = 0; p < 8; p++) {
            for (int j = 0; j < i2; j++)
                newTable.setTiletype(j, p, 0, table.getTiletype(j, p, 0));
            for (int j = i2 + 1; j < table.width; j++)
                newTable.setTiletype(j - 1, p, 0, table.getTiletype(j, p, 0));
        }
        frameData.putBuffer(newTableBAM.data);
        updateNotify.run();
    }

    @Override
    public int getCellCount() {
        return getTable().width;
    }

    @Override
    public void addCell(int i2) {
        IRIO frame = getFrame();
        IRIO frameData = frame.getIVar("@cell_data");
        RubyTable table = new RubyTable(frameData.editUser());
        frame.getIVar("@cell_max").setFX(table.width + 1);
        ByteArrayMemoryish newTableBAM = RubyTable.initNewTable(3, table.width + 1, 8, 1, new int[1]);
        RubyTable newTable = new RubyTable(newTableBAM);
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
        frameData.putBuffer(newTableBAM.data);
        updateNotify.run();
    }

    private SchemaElement[] getCellPropSchemas() {
        return new SchemaElement[] {
                new SpritesheetCoreSchemaElement(app, (v) -> app.format(v), 0, Function.identity(), (rubyIO) -> {
                    return new ISpritesheetProvider() {
                        @Override
                        public int itemWidth() {
                            return 96;
                        }

                        @Override
                        public int itemHeight() {
                            return 96;
                        }

                        @Override
                        public int itemCount() {
                            if (vxaAnim)
                                return 5 * 6 * 2;
                            return 5 * 6;
                        }

                        @Override
                        public int mapValToIdx(long itemVal) {
                            if (itemVal >= 100)
                                return (int) ((itemVal - 100) + (5 * 6));
                            return (int) itemVal;
                        }

                        @Override
                        public long mapIdxToVal(int idx) {
                            if (idx >= (5 * 6))
                                return (idx - (5 * 6)) + 100;
                            return idx;
                        }

                        @Override
                        public void drawItem(long t, int x, int y, int spriteScale, IGrDriver igd) {
                            boolean b = false;
                            if (t >= 100) {
                                t -= 100;
                                b = true;
                            }
                            int tx = (int) t % 5;
                            int ty = (int) t / 5;
                            igd.clearRect(255, 0, 255, x, y, 96, 96);
                            igd.blitScaledImage(tx * 192, ty * 192, 192, 192, x, y, 96 * spriteScale, 96 * spriteScale, spriteCache.getFramesetCache(b, false, 255));
                        }
                    };
                }),
                new IntegerSchemaElement(app, 0),
                new IntegerSchemaElement(app, 0),
                new IntegerSchemaElement(app, 0),
                new IntegerSchemaElement(app, 0),
                new IntegerSchemaElement(app, 0),
                new IntegerSchemaElement(app, 0),
                app.sdb.getSDBEntry("blend_type")
        };
    }

    @Override
    public SchemaPath getCellProp(final int ct, final int i) {
        // much better
        SchemaElement se = new RubyTableIndividualCellSchemaElement(ct, i, 0, getCellPropSchemas()[i]);
        return path.newWindow(se, getFrame().getIVar("@cell_data"));
    }

    @Override
    public IGenposTweeningProp getCellPropTweening(int ct, int i) {
        if (i < 7)
            return new TableGenposTweeningProp(new RubyTable(getFrame().getIVar("@cell_data").editUser()), ct, i, 0);
        return null;
    }

    @Override
    public void moveCell(int ct, Function<Integer, Integer> x, Function<Integer, Integer> y) {
        RubyTable rt = new RubyTable(getFrame().getIVar("@cell_data").editUser());
        rt.setTiletype(ct, 1, 0, (short) ((int) x.apply((int) rt.getTiletype(ct, 1, 0))));
        rt.setTiletype(ct, 2, 0, (short) ((int) y.apply((int) rt.getTiletype(ct, 2, 0))));
        updateNotify.run();
    }

    @Override
    public String[] getCellProps() {
        return new String[] {
                T.gp.cCID,
                T.gp.cX,
                T.gp.cY,
                T.gp.cScale,
                T.gp.cAngle,
                T.gp.cMirror,
                T.gp.cOpacity,
                T.gp.cBlendType
        };
    }

    @Override
    public Rect getCellSelectionIndicator(int i) {
        RubyTableR rt = getTable();
        int x = (int) rt.getTiletype(i, 1, 0);
        int y = (int) rt.getTiletype(i, 2, 0);
        int scale = rt.getTiletype(i, 3, 0);
        int ts = spriteCache.getScaledImageIconSize(scale);
        return new Rect(x - (ts / 2), y - (ts / 2), ts, ts);
    }

    @Override
    public void drawCell(int i, int opx, int opy, IGrDriver igd) {
        RubyTableR rt = getTable();
        // Slightly less unfinished.

        // In the target versions, 7 is blend_type, 6 is opacity (0-255), 5 is mirror (int_boolean),
        // 4 is angle, 3 is scale (0-100), x is modified by 1, y is modified by 2.
        // 0 is presumably cell-id.

        int cell = rt.getTiletype(i, 0, 0);
        boolean mirror = rt.getTiletype(i, 5, 0) != 0;
        int opacity = Math.min(Math.max(rt.getTiletype(i, 6, 0), 0), 255);
        if (opacity == 0)
            return;
        IImage scaleImage;
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
        int blendType = rt.getTiletype(i, 7, 0);
        int cellX = (cell % 5) * 192;
        int cellY = (cell / 5) * 192;
        int blendMode = IGrDriver.BLEND_NORMAL;
        if (blendType == 1)
            blendMode = IGrDriver.BLEND_ADD;
        if (blendType == 2)
            blendMode = IGrDriver.BLEND_SUB;
        igd.drawRotatedScaled(cellX, cellY, 192, 192, opx + ofx, opy + ofy, ts, ts, angle, scaleImage, blendMode, 0);
    }

    @Override
    public IImage getBackground() {
        return null;
    }
}

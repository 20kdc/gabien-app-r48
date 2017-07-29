/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */
package r48.schema.specialized.genpos.backend;

import gabien.IGrInDriver;
import gabien.ui.ISupplier;
import r48.AppMain;
import r48.ArrayUtils;
import r48.RubyIO;
import r48.dbs.TXDB;
import r48.schema.BooleanSchemaElement;
import r48.schema.SchemaElement;
import r48.schema.integers.IntegerSchemaElement;
import r48.schema.specialized.genpos.IGenposFrame;
import r48.schema.util.SchemaPath;

/**
 * Created on 29/07/17.
 */
public class R2kGenposFrame implements IGenposFrame {
    public ISupplier<RubyIO> frameSource;
    public SpriteCache cache;
    public SchemaPath rootPath;
    public Runnable updateNotify;

    public R2kGenposFrame(SpriteCache spriteCache, SchemaPath path, Runnable updater) {
        cache = spriteCache;
        rootPath = path;
        updateNotify = updater;
    }

    @Override
    public int[] getIndicators() {
        int[] cp = new int[TroopGenposFrame.gameBattleDisplay.length + 2];
        int resW = 160;
        int resH = 120;
        for (int i = 0; i < TroopGenposFrame.gameBattleDisplay.length; i += 2) {
            cp[i] = TroopGenposFrame.gameBattleDisplay[i] - resW;
            cp[i + 1] = TroopGenposFrame.gameBattleDisplay[i + 1] - resH;
        }
        return cp;
    }

    @Override
    public boolean canAddRemoveCells() {
        return true;
    }

    @Override
    public void addCell(int i2) {
        RubyIO rio = SchemaPath.createDefaultValue(AppMain.schemas.getSDBEntry("RPG::Animation::Cell"), new RubyIO().setFX(i2));
        ArrayUtils.insertRioElement(frameSource.get().getInstVarBySymbol("@cells"), rio, i2);
        updateNotify.run();
    }

    @Override
    public void deleteCell(int i2) {
        ArrayUtils.removeRioElement(frameSource.get().getInstVarBySymbol("@cells"), i2);
        updateNotify.run();
    }

    @Override
    public SchemaPath getCellProp(int ct, int i) {
        String[] trueIVars = new String[] {
                "@visible",
                "@cell_id",
                "@x",
                "@y",
                "@scale",
                "@tone_r",
                "@tone_g",
                "@tone_b",
                "@tone_grey",
                "@transparency"
        };
        SchemaPath memberPath = rootPath.otherIndex("FX").arrayHashIndex(new RubyIO().setFX(ct + 1), "[" + (ct + 1) + "]");
        RubyIO member = frameSource.get().getInstVarBySymbol("@cells").arrVal[ct + 1];
        SchemaElement se = new IntegerSchemaElement(0);
        if (i == 0)
            se = new BooleanSchemaElement(false);
        return memberPath.newWindow(se, member.getInstVarBySymbol(trueIVars[i]), null);
    }

    @Override
    public void moveCell(int ct, int x, int y) {
        RubyIO cell = frameSource.get().getInstVarBySymbol("@cells").arrVal[ct + 1];
        cell.getInstVarBySymbol("@x").fixnumVal += x;
        cell.getInstVarBySymbol("@y").fixnumVal += y;
        updateNotify.run();
    }

    @Override
    public int getCellCount() {
        return Math.max(0, frameSource.get().getInstVarBySymbol("@cells").arrVal.length - 1);
    }

    @Override
    public String[] getCellProps() {
        return new String[] {
                TXDB.get("visible"),
                TXDB.get("cellId"),
                TXDB.get("x"),
                TXDB.get("y"),
                TXDB.get("scale"),
                TXDB.get("toneR"),
                TXDB.get("toneG"),
                TXDB.get("toneB"),
                TXDB.get("toneGrey"),
                TXDB.get("transparency")
        };
    }

    @Override
    public void drawCellSelectionIndicator(int i, int opx, int opy, IGrInDriver igd) {
        RubyIO cell = frameSource.get().getInstVarBySymbol("@cells").arrVal[i + 1];
        int x = (int) cell.getInstVarBySymbol("@x").fixnumVal;
        int y = (int) cell.getInstVarBySymbol("@y").fixnumVal;
        igd.blitImage(36, 0, 32, 32, opx + x - 16, opy + y - 16, AppMain.layerTabs);
    }

    @Override
    public void drawCell(int i, int opx, int opy, IGrInDriver igd) {
        RubyIO cell = frameSource.get().getInstVarBySymbol("@cells").arrVal[i + 1];
        if (cell.getInstVarBySymbol("@visible").type == 'F')
            return;
        int op = (int) cell.getInstVarBySymbol("@transparency").fixnumVal;
        op *= 255;
        op /= 100;
        op = 255 - op;
        IGrInDriver.IImage img = cache.getFramesetCache(false, false, op);
        int x = (int) cell.getInstVarBySymbol("@x").fixnumVal;
        int y = (int) cell.getInstVarBySymbol("@y").fixnumVal;
        int cid = (int) cell.getInstVarBySymbol("@cell_id").fixnumVal;
        int sc = (int) cell.getInstVarBySymbol("@scale").fixnumVal;
        int sz = cache.getScaledImageIconSize(sc);
        x = opx + x - (sz / 2);
        y = opy + y - (sz / 2);
        if (sc == 100) {
            igd.blitImage((cid % 5) * 96, (cid / 5) * 96, 96, 96, x, y, img);
        } else {
            igd.blitScaledImage((cid % 5) * 96, (cid / 5) * 96, 96, 96, x, y, sz, sz, img);
        }
    }

    @Override
    public IGrInDriver.IImage getBackground() {
        return null;
    }
}

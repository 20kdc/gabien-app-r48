/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.schema.specialized.genpos.backend;

import gabien.IGrDriver;
import gabien.IImage;
import gabien.ui.IFunction;
import gabien.ui.ISupplier;
import gabien.ui.Rect;
import r48.AppMain;
import r48.ArrayUtils;
import r48.RubyIO;
import r48.dbs.TXDB;
import r48.schema.BooleanSchemaElement;
import r48.schema.SchemaElement;
import r48.schema.integers.IntegerSchemaElement;
import r48.schema.specialized.SpritesheetCoreSchemaElement;
import r48.schema.specialized.genpos.FixnumGenposTweeningProp;
import r48.schema.specialized.genpos.IGenposFrame;
import r48.schema.specialized.genpos.IGenposTweeningProp;
import r48.schema.util.SchemaPath;
import r48.ui.dialog.ISpritesheetProvider;

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
        if (i == 1)
            se = new SpritesheetCoreSchemaElement("#A", 0, new IFunction<RubyIO, RubyIO>() {
                @Override
                public RubyIO apply(RubyIO rubyIO) {
                    return rubyIO;
                }
            }, new IFunction<RubyIO, ISpritesheetProvider>() {
                @Override
                public ISpritesheetProvider apply(final RubyIO rubyIO) {
                    return new ISpritesheetProvider() {
                        @Override
                        public int itemWidth() {
                            return cache.spriteSize;
                        }

                        @Override
                        public int itemHeight() {
                            return cache.spriteSize;
                        }

                        @Override
                        public int itemCount() {
                            return 5 * 5;
                        }

                        @Override
                        public int mapValToIdx(long itemVal) {
                            return (int) itemVal;
                        }

                        @Override
                        public long mapIdxToVal(int idx) {
                            return idx;
                        }

                        @Override
                        public void drawItem(long t, int x, int y, int spriteScale, IGrDriver igd) {
                            int tx = (int) t % 5;
                            int ty = (int) t / 5;
                            igd.clearRect(255, 0, 255, x, y, cache.spriteSize, cache.spriteSize);
                            igd.blitScaledImage(tx * cache.spriteSize, ty * cache.spriteSize, cache.spriteSize, cache.spriteSize, x, y, cache.spriteSize * spriteScale, cache.spriteSize * spriteScale, cache.getFramesetCache(false, false, 255));
                        }
                    };
                }
            });
        return memberPath.newWindow(se, member.getInstVarBySymbol(trueIVars[i]));
    }

    @Override
    public IGenposTweeningProp getCellPropTweening(int ct, int i) {
        if ((i < 1) || (i > 9))
            return null;
        return new FixnumGenposTweeningProp(getCellProp(ct, i).targetElement);
    }

    @Override
    public void moveCell(int ct, IFunction<Integer, Integer> x, IFunction<Integer, Integer> y) {
        RubyIO cell = frameSource.get().getInstVarBySymbol("@cells").arrVal[ct + 1];
        cell.getInstVarBySymbol("@x").fixnumVal = x.apply((int) cell.getInstVarBySymbol("@x").fixnumVal);
        cell.getInstVarBySymbol("@y").fixnumVal = y.apply((int) cell.getInstVarBySymbol("@y").fixnumVal);
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
    public Rect getCellSelectionIndicator(int i) {
        RubyIO cell = frameSource.get().getInstVarBySymbol("@cells").arrVal[i + 1];
        int x = (int) cell.getInstVarBySymbol("@x").fixnumVal;
        int y = (int) cell.getInstVarBySymbol("@y").fixnumVal;
        int sc = (int) cell.getInstVarBySymbol("@scale").fixnumVal;
        int sz = cache.getScaledImageIconSize(sc);
        return new Rect(x - (sz / 2), y - (sz / 2), sz, sz);
    }

    @Override
    public void drawCell(int i, int opx, int opy, IGrDriver igd) {
        RubyIO cell = frameSource.get().getInstVarBySymbol("@cells").arrVal[i + 1];
        if (cell.getInstVarBySymbol("@visible").type == 'F')
            return;
        int op = (int) cell.getInstVarBySymbol("@transparency").fixnumVal;
        op *= 255;
        op /= 100;
        op = 255 - op;
        IImage img = cache.getFramesetCache(false, false, op);
        int x = (int) cell.getInstVarBySymbol("@x").fixnumVal;
        int y = (int) cell.getInstVarBySymbol("@y").fixnumVal;
        int cid = (int) cell.getInstVarBySymbol("@cell_id").fixnumVal;
        int sc = (int) cell.getInstVarBySymbol("@scale").fixnumVal;
        int sz = cache.getScaledImageIconSize(sc);
        x = opx + x - (sz / 2);
        y = opy + y - (sz / 2);
        if (sc == 100) {
            igd.blitImage((cid % 5) * cache.spriteSize, (cid / 5) * cache.spriteSize, cache.spriteSize, cache.spriteSize, x, y, img);
        } else {
            igd.blitScaledImage((cid % 5) * cache.spriteSize, (cid / 5) * cache.spriteSize, cache.spriteSize, cache.spriteSize, x, y, sz, sz, img);
        }
    }

    @Override
    public IImage getBackground() {
        return null;
    }
}

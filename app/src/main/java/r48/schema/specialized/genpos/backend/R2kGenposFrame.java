/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.schema.specialized.genpos.backend;

import gabien.IGrDriver;
import gabien.IImage;
import gabien.uslx.append.*;
import gabien.ui.Rect;
import r48.App;
import r48.RubyIO;
import r48.io.data.IRIO;
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
public class R2kGenposFrame extends App.Svc implements IGenposFrame {
    public ISupplier<IRIO> frameSource;
    public SpriteCache cache;
    public SchemaPath rootPath;
    public Runnable updateNotify;

    public R2kGenposFrame(App app, SpriteCache spriteCache, SchemaPath path, Runnable updater) {
        super(app);
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
        IRIO rio = frameSource.get().getIVar("@cells").addAElem(i2);
        SchemaPath.setDefaultValue(rio, app.sdb.getSDBEntry("RPG::Animation::Cell"), new RubyIO().setFX(i2));
        updateNotify.run();
    }

    @Override
    public void deleteCell(int i2) {
        frameSource.get().getIVar("@cells").rmAElem(i2);
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
        IRIO member = frameSource.get().getIVar("@cells").getAElem(ct + 1);
        SchemaElement se = new IntegerSchemaElement(app, 0);
        if (i == 0)
            se = new BooleanSchemaElement(app, false);
        if (i == 1)
            se = new SpritesheetCoreSchemaElement(app, "#A", 0, new IFunction<IRIO, IRIO>() {
                @Override
                public IRIO apply(IRIO rubyIO) {
                    return rubyIO;
                }
            }, new IFunction<IRIO, ISpritesheetProvider>() {
                @Override
                public ISpritesheetProvider apply(final IRIO rubyIO) {
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
        return memberPath.newWindow(se, member.getIVar(trueIVars[i]));
    }

    @Override
    public IGenposTweeningProp getCellPropTweening(int ct, int i) {
        if ((i < 1) || (i > 9))
            return null;
        return new FixnumGenposTweeningProp(getCellProp(ct, i).targetElement);
    }

    @Override
    public void moveCell(int ct, IFunction<Integer, Integer> x, IFunction<Integer, Integer> y) {
        IRIO cell = frameSource.get().getIVar("@cells").getAElem(ct + 1);
        cell.getIVar("@x").setFX(x.apply((int) cell.getIVar("@x").getFX()));
        cell.getIVar("@y").setFX(y.apply((int) cell.getIVar("@y").getFX()));
        updateNotify.run();
    }

    @Override
    public int getCellCount() {
        return Math.max(0, frameSource.get().getIVar("@cells").getALen() - 1);
    }

    @Override
    public String[] getCellProps() {
        return new String[] {
                app.ts("visible"),
                app.ts("cellId"),
                app.ts("x"),
                app.ts("y"),
                app.ts("scale"),
                app.ts("toneR"),
                app.ts("toneG"),
                app.ts("toneB"),
                app.ts("toneGrey"),
                app.ts("transparency")
        };
    }

    @Override
    public Rect getCellSelectionIndicator(int i) {
        IRIO cell = frameSource.get().getIVar("@cells").getAElem(i + 1);
        int x = (int) cell.getIVar("@x").getFX();
        int y = (int) cell.getIVar("@y").getFX();
        int sc = (int) cell.getIVar("@scale").getFX();
        int sz = cache.getScaledImageIconSize(sc);
        return new Rect(x - (sz / 2), y - (sz / 2), sz, sz);
    }

    @Override
    public void drawCell(int i, int opx, int opy, IGrDriver igd) {
        IRIO cell = frameSource.get().getIVar("@cells").getAElem(i + 1);
        if (cell.getIVar("@visible").getType() == 'F')
            return;
        int op = (int) cell.getIVar("@transparency").getFX();
        op *= 255;
        op /= 100;
        op = 255 - op;
        IImage img = cache.getFramesetCache(false, false, op);
        int x = (int) cell.getIVar("@x").getFX();
        int y = (int) cell.getIVar("@y").getFX();
        int cid = (int) cell.getIVar("@cell_id").getFX();
        int sc = (int) cell.getIVar("@scale").getFX();
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
